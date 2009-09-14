/**
 * (c) 2009 Alex Anderson, Masabi Ltd.
 */
package org.smslib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import net.frontlinesms.hex.HexUtils;

import org.smslib.CMessage.MessageEncoding;

/**
 * Utilities class for generating and reading SMS Transfer protocol data units (TPDUs).
 * 
 * Methods and constants in this class were coded following the ETSI specification RTS/TSGC-0123040v830.
 * 
 * @author Alex Anderson
 */
public final class TpduUtils {
	/** Maximum size, in octets, of a single PDU */
	static final int MAX_PDU_SIZE = 140;

	/** Use 8-bit or 16-bit concat for outgoing messages.  If no other UDH is present. */
	public static final boolean CONCAT_USE_16_BIT = false;
	
//> [TP-MTI: TP-Message-Type-Indicator] Parameter describing the message type.
	// bits 1-0 of the first byte of the TPDU: xxxxxxXX
	/** Mask to extract MTI from the first byte of a TPDU */
	static final int TP_MTI_MASK = 0x3;
	/** 2-bit value indicating an MO message is of type SMS-DELIVER-REPORT */
	static final int TP_MTI_MO_DELIVER_REPORT = 0x0;
	/** 2-bit value indicating an MO message is of type SMS-SUBMIT */
	static final int TP_MTI_MO_SUBMIT = 0x1;
	/** 2-bit value indicating an MO message is of type SMS-STATUS-REPORT */
	static final int TP_MTI_MO_STATUS_REPORT = 0x2;
	/** 2-bit value indicating an MT message is of type SMS-DELIVER */
	static final int TP_MTI_MT_DELIVER = 0x0;
	/** 2-bit value indicating an MT message is of type SMS-SUBMIT-REPORT */
	static final int TP_MTI_MT_SUBMIT_REPORT = 0x1;
	/** 2-bit value indicating an MT message is of type SMS-COMMAND */
	static final int TP_MTI_MT_COMMAND = 0x2;
//> [TP-VPF: TP-Validity-Period-Format] Parameter indicating whether or not the TP-VP field is present.
	/** Shift to insert a VPF value */
	private static final int TP_VPF_SHIFT = 3;
	/** Mask to extract VPF from the first byte of the TPDU */
	static final int TP_VPF_MASK = (3 << TP_VPF_SHIFT);
	static final int TP_VPF_NOT_PRESENT = 0;
	static final int TP_VPF_ENHANCED = 1 << TP_VPF_SHIFT;
	static final int TP_VPF_RELATIVE = 2 << TP_VPF_SHIFT;
	static final int TP_VPF_ABSOLUTE = 3 << TP_VPF_SHIFT;
//> [TP-SRR: TP-Status-Report-Request] Parameter indicating if the MS is requesting a status report
	/** Flag indicating if the MS is requesting a status report.  Only used for messages of type {@link #TP_MTI_MO_SUBMIT}. */
	static final int TP_SRR_FLAG = 1 << 5;
//> [TP-UDHI: TP-User-Data-Header-Indicator] Parameter indicating that the TP-UD field contains a Header.
	/** Flag indicating the presence of the UDH in this message */
	static final int TP_UDHI = 1 << 6;
//> [TP-DCS: TP-Data-Coding-Scheme] Parameter identifying the coding scheme within the TP-User-Data.
	/** Shift to get or add character set to the TP-DCS */
	private static final int TP_DCS_CHARSET_SHIFT = 2;
	/** TP-DCS character set is 2 bits at xxxx??xx */
	static final int TP_DCS_CHARSET_MASK = 3 << TP_DCS_CHARSET_SHIFT;
	/** TP-DCS GSM 7 bit default alphabet */
	static final int TP_DCS_CHARSET_GSM_7_BIT = 0;
	/** TP-DCS 8 bit data */
	static final int TP_DCS_CHARSET_8_BIT_DATA = 1 << TP_DCS_CHARSET_SHIFT;
	/** TP-DCS UCS2 (16bit) */
	static final int TP_DCS_CHARSET_UCS2 = 2 << TP_DCS_CHARSET_SHIFT;
//> [TP-UDH: User-Data-Header]
	/** TP-UDH [IEI: Information-Element-Identifier] Application port addressing scheme, 16 bit address. */
	static final int TP_UDH_IEI_APP_PORTING_16BIT = 0x05;
	/** TP-UDH [Length of IE: Length of Information-Element] Length, in octets, of data following {@link #TP_UDH_IEI_APP_PORTING_16BIT} */
	static final int TP_UDH_IEI_APP_PORTING_16BIT_LENGTH = 4;
	/** TP-UDH [IEI: Information-Element-Identifier] Concatenated short message, 16-bit reference number. */
	static final int TP_UDH_IEI_CONCAT_SMS_16BIT = 0x08;
	/** TP-UDH [Length of IE: Length of Information-Element] Length, in octets, of data following {@link #TP_UDH_IEI_CONCAT_SMS_16BIT} */
	static final int TP_UDH_IEI_CONCAT_SMS_16BIT_LENGTH = 4;
	/** TP-UDH [IEI: Information-Element-Identifier] Concatenated short message, 8-bit reference number. */
	static final int TP_UDH_IEI_CONCAT_SMS_8BIT = 0x00;
	/** TP-UDH [Length of IE: Length of Information-Element] Length, in octets, of data following {@link #TP_UDH_IEI_CONCAT_SMS_8BIT} */
	static final int TP_UDH_IEI_CONCAT_SMS_8BIT_LENGTH = 3;
	/** TP-UDH [IEI: Information-Element-Identifier] Concatenated short message, 8-bit reference number. */
	static final int TP_UDH_IEI_WIRELESS_MESSAGE_CONTROL_PROTOCOL = 0x09;
	
	/**
	 * Array containing the characters allowable in the GSM Semi-Octet format.
	 * The position in this array represents the semi-octet's value for that
	 * character.  Characters not found in this array are not valid.
	 * Used by {@link #toSemiOctets(String)}.
	 */
	private static String SEMI_OCTET = new String(new char[]{
		/*Digits: */		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		/*Special chars: */	'*', '#', 'a', 'b', 'c',
		/*Space character*/ ' '});

//> [ToA: Type-of-Address]
	/** Type-of-Address field should always have it's top bit set.  That's just the spec. */
	private static final byte TOA_TOP_BIT = (byte)(1 << 7);
	
//> [ToA Npi: Type-of-Address Numbering-plan-identification]
	/** [Type-of-Address Numbering-plan-identification ISDN/telephone numbering plan (E.164 [17]/E.163[18])] */
	private static final int TOA_NPI_ISDN_TELEPHONE = 1;
	
//> [ToA Ton: Type-of-Address Type-of-number]
	/** Shift to insert the 3 bits of Type-of-number into a Type-of-Address */
	private static final int TOA_TON_SHIFT = 4;
	/** Mask to extract Type-of-number from Type-of-Address */
	private static final int TOA_TON_MASK = 7 << TOA_TON_SHIFT;
	/** 
	 * [Type-of-Address Type-of-number Unknown]
	 * "Unknown" is used when the user or network has no a priori information about the numbering plan. In this case, the 
	 * Address-Value field is organized according to the network dialling plan, e.g. prefix or escape digits might be present.
	 */ 
	private static final int TOA_TON_UNKNOWN = 0;
	/**
	 * [Type-of-Address Type-of-number International number]
	 * The international format shall be accepted also when the message is destined to a recipient in the same country
	 * as the MSC or as the SGSN.
	 */
	private static final int TOA_TON_INTERNATIONAL = 1 << TOA_TON_SHIFT;
	/**
	 * [Type-of-Address Type-of-number National number]
	 * Prefix or escape digits shall not be included.
	 */
	private static final int TOA_TON_NATIONAL = 2 << TOA_TON_SHIFT;
	/**
	 * [Type-of-Address Type-of-number Network specific number]
	 * "Network specific number" is used to indicate administration/service number specific to the serving network, e.g.
	 * used to access an operator.
	 */
	private static final int TOA_TON_NETWORK_SPECIFIC = 3 << TOA_TON_SHIFT;
	/**
	 * [Type-of-Address Type-of-number Subscriber number]
	 * "Subscriber number" is used when a specific short number representation is stored in one or more SCs as part of
	 * a higher layer application. (Note that "Subscriber number" shall only be used in connection with the proper PID
	 * referring to this application).
	 */
	private static final int TOA_TON_SUBSCRIBER = 4 << TOA_TON_SHIFT;
	/**
	 * [Type-of-Address Type-of-number Alphanumeric]
	 * coded according to 3GPP TS 23.038 [9] GSM 7-bit default alphabet
	 */
	private static final int TOA_TON_ALPHANUMERIC = 5 << TOA_TON_SHIFT;
	/**
	 * [Type-of-Address Type-of-number Abbreviated number]
	 */
	private static final int TOA_TON_ABBREVIATED = 6 << TOA_TON_SHIFT;
	
	/**
	 * Generates a relative validity period octet.
	 * [TP-VP: TP-Validity-Period] Parameter identifying the time from where the message is no longer valid.
	 * The representation of time is as follows:
	 * <pre>
			Validity period value												TP-VP value 
			(TP-VP + 1) x 5 minutes (i.e. 5 minutes intervals up to 12 hours)	0 to 143
			12 hours + ((TP-VP -143) x 30 minutes)								144 to 167
			(TP-VP - 166) x 1 day												168 to 196
			(TP-VP - 192) x 1 week												197 to 255
	 </pre>
	 * If no validity period is requested, the maximum will be requested.
	 * @param validityPeriod in hours
	 * @return int whose least-significant 8 bits contains the encoded TP-VP octet
	 */
	static int getRelativeVP(int validityPeriod) {
		if(validityPeriod <= 0) return 0xFF; // maximum possible
		if(validityPeriod <= 12)
			return (validityPeriod * 12) - 1;
		if(validityPeriod <= 24) 
			return ((validityPeriod - 12) * 2) + 143;
		if(validityPeriod <= 720) 
			return (validityPeriod / 24) + 166;
		return (validityPeriod / 168) + 192;
	}

	
	/**
	 * Encodes an address field for use in a PDU.
	 * Encodes as specified in in 3GPP TS 24.011 [13] and 3GPP TS 29.002 [15].
	 * This method uses:
	 * <li>Type-of-number = "Unknown"</li>
	 * <li>Type-of-number =
	 *     <li>Private numbering plan (addresses supplied in international format, i.e. with leading <pre>+</pre> character.</li>
	 *     <li>National numbering plan (other addresses).</li>
	 * </li>
	 *     
	 * @param address The MSISDN to encode
	 * @param isSmscNumber The encoding is slightly different for an SMSC address to an address used in other contexts.
	 * @return an encoded address
	 * @throws NullPointerException if <code>address</code> is <code>null</code>
	 */
	static byte[] encodeMsisdnAsAddressField(String address, boolean isSmscNumber) throws NullPointerException {
		// It's valid to not supply the SMSC number.  In this case, we just write a 0-value byte to the front of the
		// PDU.  This is what's happening here.
		if(isSmscNumber && address.length() == 0) {
			return new byte[1];
		}
		
		boolean isInternationalNumber = address.charAt(0) == '+';
		// Encode the MSISDN
		if(isInternationalNumber) address = address.substring(1);
		if(address.length() > 20) throw new IllegalSmsEncodingException("The maximum length of an address field is 12 octets, so the address itself must be 20 characters or less.");
		byte[] encodedAddress = toSemiOctets(address);
		
		// Now create a new array with space for the address length and Type-of-Address field on the front.
		byte[] data = new byte[encodedAddress.length + 2];
		System.arraycopy(encodedAddress, 0, data, 2, encodedAddress.length);
		
		// Add the Address-Length
		int reportedLength;
		if(isSmscNumber) {
			// For SMSC numbers, this length is the number of octets following the length byte
			// This includes the Type-of-address.
			// adding 1 to address.length() removes possible rounding error
			reportedLength = 1/*Type-of-address byte*/ + ((address.length()+1)>>1);
		} else {
			// For non-SMSC numbers, The Address-Length field is an integer representation of the
			// number of useful semi-octets within the Address-Value field, i.e. excludes any semi
			// octet containing only fill bits. 
			// FIXME this length should exclude any space characters found in the address.  A better fix might be to strip these! 
			reportedLength = address.length();
		}
		data[0] = (byte)reportedLength;
		
		// Add the Type-of-Address field
		// Top bit is always set
		data[1] = TOA_TOP_BIT;
		// Bottom 4 bits is Numbering Plan Identification.  We always use ISDN/telephone numbering plan
		data[1] |= TOA_NPI_ISDN_TELEPHONE;
		// Add the Type-of-number.  Here, we use TON_UNKNOWN rather than national when the number isn't international.  It seems to work better.
		data[1] |= (isInternationalNumber ? TOA_TON_INTERNATIONAL : TOA_TON_UNKNOWN);
		return data;
	}
	
	/**
	 * Reverses the operation of {@link #encodeMsisdnAsAddressField(String, boolean)} - reads
	 * an encoded MSISDN from the octet stream of an SMS message's PDU.
	 * 
	 * For a normal Address field which encodes a number in semi-octet format, the "Address-Length
	 * field is an integer representation of the number of useful semi-octets within the Address-Value
	 * field, i.e. excludes any semi octet containing only fill bits."  This means that we do not
	 * decrement our length counter for fill bits.  When decoding an SMSC number, we ALWAYS count these
	 * fill semi-octets.
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	static String decodeMsisdnFromAddressField(InputStream in, boolean isSmscNumber) throws IOException {
		int addressLength = in.read();
		if(addressLength == 0) return "";
		
		if(isSmscNumber) addressLength = (addressLength - 1) << 1;

		// In case we need to prefix '+' for int'l number
		StringBuilder bob = new StringBuilder(addressLength + 1);
		
		// Check byte1 to see if this number is international
		int byte1 = in.read();
		
		switch(byte1 & TOA_TON_MASK) {
		case TOA_TON_ALPHANUMERIC:
			// addressLength is the number of semi-octets this address takes up.  We need to
			// remove a number of WHOLE octets from the input stream, and pass these to be
			// decoded using the standard 7-bit GSM alphabet.
			byte[] addressBytes = new byte[addressLength >> 1];
			for(int i=0; i<addressBytes.length; ++i) addressBytes[i] = (byte)in.read();
			bob.append(GsmAlphabet.bytesToString(GsmAlphabet.octetStream2septetStream(addressBytes, 0)));
			break;
		case TOA_TON_INTERNATIONAL:
			/* Treat as a normal number, but with a + prefix */
			bob.append('+');
		case TOA_TON_ABBREVIATED:
			/* Deocde as non-prefixed number */
		case TOA_TON_NETWORK_SPECIFIC:
			/* Deocde as non-prefixed number */
		case TOA_TON_SUBSCRIBER:
			/* Deocde as non-prefixed number */
		case TOA_TON_UNKNOWN:
			// From our test data from users, treating UNKNOWN as a non-prefixed seems to be good behaviour
		case TOA_TON_NATIONAL:
			// National number: "Prefix or escape digits shall not be included."
			while(addressLength > 0) {
				int addressByte = in.read();
				char digit;
				digit = SEMI_OCTET.charAt(addressByte & 0xF);
				if(isSmscNumber || digit != ' ') {
					if(digit != ' ') bob.append(digit);
					--addressLength;
				}
				digit = SEMI_OCTET.charAt((addressByte>>>4) & 0xF);
				if(isSmscNumber || digit != ' ') {
					if(digit != ' ') bob.append(digit);
					--addressLength;
				}
			}
			break;
		}
		return bob.toString();
	}
	
	/**
	 * Generates the DCS octet for a standard SMS message with non-custom encoding.
	 * [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data.
	 * @param encoding
	 * @return
	 */
	static final int getDcsByte(int encoding) {
		assert(encoding != MessageEncoding.EncCustom) : "This method should not be used for custom encoding.";
		int dcs = 0;
		// Get the bottom 4 bits:
		if(encoding == MessageEncoding.Enc8Bit) {
			dcs |= TP_DCS_CHARSET_8_BIT_DATA;
		} else if(encoding == MessageEncoding.EncUcs2) {
			dcs |= TP_DCS_CHARSET_UCS2;
		} else {
			// Assume encoding == MessageEncoding.Enc7Bit.
			dcs |= TP_DCS_CHARSET_GSM_7_BIT;
		}
		
		// If "flash sms" were required, code to set a message as flash would go here.
		
		return dcs;
	}
	
	/**
	 * Extracts the {@link MessageEncoding} type from the Data-CodingScheme byte of an SMS message.
	 * [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data.
	 * @param dcsByte
	 * @return
	 */
	static final int getMessageEncoding(int dcsByte) {
		int encodingQuintet = dcsByte & TP_DCS_CHARSET_MASK;
		if(encodingQuintet == TP_DCS_CHARSET_8_BIT_DATA)
			return MessageEncoding.Enc8Bit;
		if(encodingQuintet == TP_DCS_CHARSET_UCS2)
			return MessageEncoding.EncUcs2;
		// Assume encoding == MessageEncoding.Enc7Bit.
		else return MessageEncoding.Enc7Bit;
	}
	
	/**
	 * Generates the front byte of a Mobile-Originated (MO) sms message.  The least-significant 8 bits
	 * of the returned int are the desired byte.
	 * @param tpMti Message-Type-Indicator
	 * @return
	 */
	static int getByteZero(int tpMti, boolean requiresUdh, boolean requestStatusReport) {		
		if(tpMti != (tpMti & TP_MTI_MASK)) throw new IllegalSmsEncodingException("Illegal SMS type (TP-MTI): " + tpMti);
		if(requiresUdh) tpMti |= TP_UDHI;
		if(requestStatusReport) tpMti |= TP_SRR_FLAG;
		// All VPFs are relative here
		tpMti |= TP_VPF_RELATIVE;
		return tpMti; 
	}
	
	/**
	 * Converts a digit String into GSM semi-octet format.  This is basically BCD,
	 * but with the following caveats:
	 * <li>If the input string is of odd length, the lower 4 bits of the last byte of the output should take value 0xF</li>
	 * <li>The following non-decimal characters are also allowed, and are shown here with their bit mappings:<pre>
	 * 		char	bits	hex
	 * 		*		1010	0xA
	 * 		#		1011	0xB
	 * 		a		1100	0xC
	 * 		b		1101	0xD
	 * 		c		1110	0xE
	 * </pre>This is embodied in {@link #SEMI_OCTET}.</li>
	 * As other instances of 0xF in a BCD should be IGNORED according to the spec, I have represented this as a space.
	 * @param s
	 * @return
	 */
	static byte[] toSemiOctets(String s) {
		int len = s.length();
		if((len & 1) == 1) {
			s += " ";
			++len;
		}
		len >>= 1;
		byte[] bcd = new byte[len];
		while(--len >= 0) {
			int loNibble = SEMI_OCTET.indexOf(s.charAt(len << 1));
			int hiNibble = SEMI_OCTET.indexOf(s.charAt((len << 1) + 1));
			bcd[len] = (byte)((hiNibble << 4) | loNibble);
		}
		return bcd;
	}
	
	/**
	 * Encode text for inclusion in a PDU.
	 * @param messageEncoding
	 * @param text
	 * @param dataCodingScheme
	 * @return
	 * @deprecated This method shouldn't be used due to the special splitting required for both 7-bit GSM messages and UCS2 messages.
	 */
	static byte[] encodeText(int messageEncoding, String text, int dataCodingScheme) {
		switch (messageEncoding) {
			case MessageEncoding.Enc7Bit:
				throw new IllegalArgumentException("Text should be encoded using GsmAlphabet encoding.");
			case MessageEncoding.Enc8Bit:
				throw new RuntimeException("Should not be calling encodeText on a binary message.");
			case MessageEncoding.EncUcs2:
				return encodeUcs2Text(text);
			case MessageEncoding.EncCustom:
				if ((dataCodingScheme & 0x04) == 0) {
					throw new IllegalArgumentException("Text should be encoded using GsmAlphabet encoding.");
				} else {
					byte[] data = new byte[text.length()];
					for (int i = data.length-1; i >= 0; --i) {
						data[i] = (byte)text.charAt(i);
					}
					return data;
				}
			default: throw new IllegalSmsEncodingException("Unrecognized message encoding: " + messageEncoding);
		}
	}
	
	/**
	 * Encodes a java {@link String} as a byte[] containing UCS2 character codes.
	 * SMS UCS-2 encoding is identical to Java UCS-2 encoding.  Unfortunately, since Java5, there has
	 * been support for UTF-16 characters as well.  For now, this is ignored.
	 * @param text
	 * @return
	 */
	public static byte[] encodeUcs2Text(String text) {
		byte[] bytes = new byte[text.length() << 1];
		for(int i=text.length()-1; i>=0; --i) {
			char c = text.charAt(i);
			bytes[ i << 1     ] = (byte)(c >> 8);	// top 8 bits
			bytes[(i << 1) + 1] = (byte) c;			// bottom 8 bits
		}
		return bytes;
	}
	
	/**
	 * Decodes a UCS2 octet stream.  Should undo the actions of {@link #encodeUcs2Text(String)}.
	 * SMS UCS-2 encoding is identical to Java UCS-2 encoding.  Unfortunately, since Java5, there has
	 * been support for UTF-16 characters as well.  For now, this is ignored.
	 * @param encodedText
	 * @return
	 */
	public static String decodeUcs2Text(byte[] encodedText) {
		StringBuilder bob = new StringBuilder(encodedText.length >> 1);
		for(int i=0; i<encodedText.length; i+=2) {
			char c = (char)(encodedText[i + 1] & 0xFF);
			c |= encodedText[i] << 8;
			bob.append(c);
		}
		return bob.toString();
	}
	
	/**
	 * Calculates the number of separate SMS messages needed to transmit a piece of data.
	 * @param payloadLength The length, in octets, of the message payload, after encoding.
	 * @param isPorted
	 * @param isMultipart
	 * @return
	 */
	public static int getMessagesNeeded_8bit(int payloadLength, boolean isPorted) {
		// First, we assume that this message is SINGLE part.  Can we fit it
		// all in one message?  If so, we do.  If not, we then recalculate the
		// UDH size bearing in mind the message is multipart.
		int udhSizeSinglepart = getUDHSize(true, isPorted, false);
		if(payloadLength + udhSizeSinglepart <= MAX_PDU_SIZE) {
			return 1;
		} else {
			int udhSizeMultipart = getUDHSize(true, isPorted, true);
			int maxUD = MAX_PDU_SIZE - udhSizeMultipart;
			return (payloadLength + maxUD - 1) / maxUD;
		}
	}
	
	/**
	 * Splits text to be sent in a UCS2 SMS message.  In a UCS-2 encoded message,
	 * each character of text takes up 2 octets in the message.  A single character
	 * may not be split across the boundary of a multipart message.
	 * @param text The tetx to check.
	 * @param isPorted True if there are ports to include in a UDH.
	 * @return The message, split into separate parts.
	 */
	public static String[] splitText_ucs2(String text, boolean isPorted) {
		// First, we assume that this message is SINGLE part.  Can we fit it
		// all in one message?  If so, we do.  If not, we then recalculate the
		// UDH size bearing in mind the message is multipart.
		int splitCount = getMessagesNeeded_ucs2(text, isPorted);
		if (splitCount==1) {
			return new String[]{text};
		} else {
			int udhSizeMultipart = getUDHSize(true, isPorted, true);
			int charactersPerMessage = (MAX_PDU_SIZE - udhSizeMultipart) >> 1;
			String[] textParts = new String[splitCount];
			for (int i = 0; i < textParts.length; i++) {
				int beginIndex = i * charactersPerMessage;
				int endIndex = Math.min(beginIndex + charactersPerMessage, text.length());
				textParts[i] = text.substring(beginIndex, endIndex);
			}
			return textParts;
		}
	}
	
	/**
	 * Works out how to split text to be sent in a UCS2 SMS message.  In a UCS-2 encoded message,
	 * each character of text takes up 2 octets in the message.  A single character
	 * may not be split across the boundary of a multipart message.
	 * @param text The text to check for splitting needs.
	 * @param isPorted True if there are ports to include in a UDH.
	 * @return Number of messages that will be required to carry this text.
	 */
	public static int getMessagesNeeded_ucs2(String text, boolean isPorted)
	{
		// First, we assume that this message is SINGLE part.  Can we fit it
		// all in one message?  If so, we do.  If not, we then recalculate the
		// UDH size bearing in mind the message is multipart.
		int udhSizeSinglepart = getUDHSize(true, isPorted, false);
		/** The length of our text in octets. */
		int textLengthOctets = text.length() << 1;
		if(textLengthOctets + udhSizeSinglepart <= MAX_PDU_SIZE)	return 1;

		// we know we have to split, but by how much?
		int charactersPerMessage = (MAX_PDU_SIZE - getUDHSize(true, isPorted, true)) >> 1;
		return (int)Math.ceil((text.length()*1.0) / charactersPerMessage);
	}

	/**
	 * Calculates the size, in octets, of the data in a UDH, given some basic details of its contents.
	 * @param includeLength <code>true</code> if the size should include the length octet itself
	 * @param isPorted <code>true</code> if this message is being sent to or from a specific port.
	 * @param requiresConcat <code>true</code> if this message will be split into multiple parts when sent
	 * @return
	 */
	static final int getUDHSize(boolean includeLength, boolean isPorted, boolean requiresConcat) {
		if(!isPorted && !requiresConcat) return 0;
		int length = 0;
		if(includeLength) ++length;
		if (isPorted) {
			length += 2 /* 1 octet for IEI-Type, 1 byte for IEI-Length */
					+ TP_UDH_IEI_APP_PORTING_16BIT_LENGTH;
		}
		if (requiresConcat) {
			if(CONCAT_USE_16_BIT) {
				length += 2 /* 1 octet for IEI-Type, 1 byte for IEI-Length */
						+ TP_UDH_IEI_CONCAT_SMS_16BIT_LENGTH;
			} else {
				length += 2 /* 1 octet for IEI-Type, 1 byte for IEI-Length */
						+ TP_UDH_IEI_CONCAT_SMS_8BIT_LENGTH;
			}
		}
		return length;
	}
	
	/**
	 * Generate the UD-Header of an SMS message.  N.B. This header is optional,
	 * so this method shouldn't be called before checking it is necessary.
	 * TODO rename mpRefNo
	 * @return
	 */
	public static byte[] generateUDH(int partNumber, int totalParts, int multipartReferenceNumber, int sourcePort, int destinationPort) {
		ByteArrayOutputStream udh = new ByteArrayOutputStream();

		/** [TP-UDHL: User-Data-Header-Length] Leave a placeholder for the UDH-Length */
		udh.write(0);
		
		// If only one port is set, we set the other to 0000
		if (sourcePort != 0 || destinationPort != 0) {
			if(sourcePort != (sourcePort & 0xFFFF)) throw new IllegalArgumentException("Supplied source port was outside 16-bit range: " + sourcePort);
			if(destinationPort != (destinationPort & 0xFFFF)) throw new IllegalArgumentException("Supplied destination port was outside 16-bit range: " + destinationPort);
			// N.B. Could use 8-bit concat here if port numbers were low enough.
			/** Use Application Port Addressing 16 bit address */
			udh.write(TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT);
			udh.write(TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT_LENGTH);
			udh.write((destinationPort >>> 8) & 0xFF);
			udh.write(destinationPort & 0xFF);
			udh.write((sourcePort >>> 8) & 0xFF);
			udh.write(sourcePort & 0xFF);
		}

		if (totalParts != 1) {
			if(partNumber < 1 || partNumber > totalParts) throw new IllegalArgumentException("Illegal part number for multipart message: part " + partNumber + " of " + totalParts);
			if(totalParts != (totalParts & 0xFF)) throw new IllegalArgumentException("Illegal number of message parts: " + totalParts);
			if(multipartReferenceNumber != (multipartReferenceNumber & 0xFFFF)) throw new IllegalArgumentException("Multipart Reference Number outside valid range: " + multipartReferenceNumber);

			if(CONCAT_USE_16_BIT) {
				udh.write(TpduUtils.TP_UDH_IEI_CONCAT_SMS_16BIT);
				udh.write(TpduUtils.TP_UDH_IEI_CONCAT_SMS_16BIT_LENGTH);
				// Write the multipart reference number as 16 bits
				udh.write((multipartReferenceNumber >>> 8) & 0xFF);
				udh.write(multipartReferenceNumber & 0xFF);
			} else {
				// Try 8-bi concat
				udh.write(TpduUtils.TP_UDH_IEI_CONCAT_SMS_8BIT);
				udh.write(TpduUtils.TP_UDH_IEI_CONCAT_SMS_8BIT_LENGTH);
				// Write the multipart reference number as 8 bits
				udh.write(multipartReferenceNumber & 0xFF);
			}
			
			
			// write the number of messages needed as one octet (valid values: 1-255)
			udh.write(totalParts);
			// write the message part number as one octet (valid values: 1-255)
			udh.write(partNumber);
		}
		
		byte[] udhContent = udh.toByteArray();

		/** [TP-UDHL: User-Data-Header-Length] One octet containing the length, in octets, of the UDH, not including the length itself */
		int udhLength = udhContent.length - 1;
		udhContent[0] = (byte) udhLength;
		
		return udhContent;
	}

	/**
	 * Gets the data content (SM) of the UD (i.e. UD without UDH) of the specified part of a message.
	 * 
	 * TODO this should return a byte[]
	 * 
	 * FIXME this will fail on UCS2 messages whose UDH header length is odd (or is it even???)
	 * 
	 * @param partNo
	 * @param udhLength
	 * @param encodedText Text encoded using {@link #encodeText(int, String, int)}
	 * @return
	 * @deprecated This method is ONLY suitable for splitting binary messages.  UCS2 and GSM 7-bit messages do not allow letters to be split over separate messages.
	 */
	public static String extractPayload(String encodedText, int partNo, int udhLength) {
		int partSize = MAX_PDU_SIZE - udhLength;
		partSize *= 2;
		if (((partSize * (partNo - 1)) + partSize) > encodedText.length()) return encodedText.substring(partSize * (partNo - 1));
		else return encodedText.substring(partSize * (partNo - 1), (partSize * (partNo - 1)) + partSize);
	}

	/**
	 * Generate the PDUs for a standard text SMS message, with no SMSC Number,
	 * maximum message validity, no special TP-Protocol-Identifier, a concat reference number of
	 * zero, and source and destination ports of zero.
	 * 
	 * The character encoding of the text is set to {@link MessageEncoding#Enc7Bit} unless there are characters
	 * outside of this alphabet.  If that is the case, {@link MessageEncoding#EncUcs2} is used.
	 * 
	 * @param messageText
	 * @param recipientMsisdn
	 * @param requestStatusReport
	 * @return
	 */
	public static String[] generatePdus(String messageText, String recipientMsisdn, boolean requestStatusReport) {
		if(GsmAlphabet.areAllCharactersValidGSM(messageText)) {
			return generatePdus_gsm7bit(messageText, "",
					recipientMsisdn, 0, 0, 0, requestStatusReport, 0, 0, getDcsByte(MessageEncoding.Enc7Bit));
		} else {
			return generatePdus_ucs2(messageText, "",
					recipientMsisdn, 0, 0, 0, requestStatusReport, 0, 0, getDcsByte(MessageEncoding.EncUcs2));
		}
	}
	
	/**
	 * Splits an octet stream into the separate parts that would be the payloads of
	 * concatenated SMS messages.
	 * @param messageBinary
	 * @param sourcePort
	 * @param destinationPort
	 * @return Supplied byte[] split into parts.  These parts should reconcatenate to form the original byte array.  Supplying a zero-length byte[] should return a single, empty part.
	 */
	public static byte[][] getPayloads(byte[] messageBinary, int sourcePort, int destinationPort) {
		if(messageBinary.length == 0) return new byte[][]{new byte[0]};
		
		boolean isPorted = sourcePort > 0 || destinationPort > 0;
		int totalParts = getMessagesNeeded_8bit(messageBinary.length, isPorted);
		byte[][] payloadParts = new byte[totalParts][];
		int udhLength = getUDHSize(true, isPorted, totalParts > 1);
		String encodedText = HexUtils.encode(messageBinary);
		for (int i = 0; i < payloadParts.length; i++) {
			payloadParts[i] = HexUtils.decode(extractPayload(encodedText, i+1, udhLength));
		}
		return payloadParts;
	}
	
	/**
	 * Generates the TPDUs for a binary message.  The TPDUs are returned as hex-encoded binary strings.
	 * @param smscNumber
	 * @param concatReferenceNumber Reference number to embed in multipart message parts' UDH
	 * @param validityPeriod Validity period of this message, in hours.  Maximum validity will be requested if this is set to zero.
	 * @param protocolIdentifier 
	 * @param dataCodingScheme 
	 * @return
	 */
	static String[] generatePdus_8bit(byte[] messageBinary, String smscNumber, String recipientMsisdn, int concatReferenceNumber, int sourcePort, int destinationPort, boolean requestStatusReport, int validityPeriod, int protocolIdentifier, int dataCodingScheme) {
		String encodedText = HexUtils.encode(messageBinary);
	
		boolean isPorted = sourcePort > 0 || destinationPort > 0;
		final int totalParts = getMessagesNeeded_8bit(messageBinary.length, isPorted);
		boolean isMultipart = totalParts > 1;
		
		boolean requiresUdh = isMultipart||isPorted;
		
		String[] pdus = new String[totalParts];
		
		try {
			for(int partNumber=1; partNumber<=totalParts; ++partNumber) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
	
				out.write(encodeMsisdnAsAddressField(smscNumber, true));
				
				// get the front byte, which identifies message content
				out.write(getByteZero(TP_MTI_MO_SUBMIT, requiresUdh, requestStatusReport));
				
				// Message reference.  Always zero here. 
				/** [TP-MR: TP-Message-Reference] Parameter identifying the SMS-SUBMIT. */ 
				out.write(0);
	
				// Add the recipient's MSISDN
				/** [TP-DA: TP-Destination-Address] Address of the destination SME. */
				out.write(encodeMsisdnAsAddressField(recipientMsisdn, false));
				
				/** [TP-PID: TP-Protocol-Identifier] Parameter identifying the above layer protocol, if any. */
				out.write(protocolIdentifier);
	
				/** [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data. */
				out.write(dataCodingScheme);
	
				/**
				 * [TP-VP: TP-Validity-Period] Parameter identifying the time from where the message is no longer valid.
				 * Here, this is always relative.
				 */
				out.write(getRelativeVP(validityPeriod));
				
				// Build the UD
				
				// First build the UDH
				/** Total length of the udh, including the UDHL.  Do not confuse with udhBytes.length, which is the length of the UDH's content */
				int udhTotalLength = getUDHSize(true, isPorted, isMultipart);
	
				String udWithoutHeader = extractPayload(encodedText, partNumber, udhTotalLength);
				/** The length, in octets, of the user data, including the header. */
				int dataLen = (udWithoutHeader.length() >> 1) + udhTotalLength;
	
				/** [TP-UDL: TP-User-Data-Length] Length of the UD, specific to the encoding. */
				out.write(dataLen);
				if(requiresUdh) {
					/** Now write the octet content of the UDH */
					out.write(generateUDH(partNumber, totalParts, concatReferenceNumber, sourcePort, destinationPort));
				}
				
				pdus[partNumber-1] = HexUtils.encode(out.toByteArray()) + udWithoutHeader;
			}
			return pdus;
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Generates the TPDUs for a UCS-2-encoded text message.  The TPDUs are returned as hex-encoded binary strings.
	 * @param smscNumber
	 * @param concatReferenceNumber Reference number to embed in multipart message parts' UDH
	 * @param validityPeriod Validity period of this message, in hours.  Maximum validity will be requested if this is set to zero.
	 * @param protocolIdentifier 
	 * @param dataCodingScheme 
	 * @return
	 */
	static String[] generatePdus_ucs2(String messageText, String smscNumber, String recipientMsisdn, int concatReferenceNumber, int sourcePort, int destinationPort, boolean requestStatusReport, int validityPeriod, int protocolIdentifier, int dataCodingScheme) {
		boolean isPorted = sourcePort > 0 || destinationPort > 0;
		String[] messageParts = splitText_ucs2(messageText, isPorted);
		final int totalParts = messageParts.length;
		boolean isMultipart = totalParts > 1;
		
		boolean requiresUdh = isMultipart||isPorted;
		
		String[] pdus = new String[totalParts];
		
		try {
			for(int partNumber=1; partNumber<=totalParts; ++partNumber) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
	
				out.write(encodeMsisdnAsAddressField(smscNumber, true));
				
				// get the front byte, which identifies message content
				out.write(getByteZero(TP_MTI_MO_SUBMIT, requiresUdh, requestStatusReport));
				
				// Message reference.  Always zero here. 
				/** [TP-MR: TP-Message-Reference] Parameter identifying the SMS-SUBMIT. */ 
				out.write(0);
	
				// Add the recipient's MSISDN
				/** [TP-DA: TP-Destination-Address] Address of the destination SME. */
				out.write(encodeMsisdnAsAddressField(recipientMsisdn, false));
				
				/** [TP-PID: TP-Protocol-Identifier] Parameter identifying the above layer protocol, if any. */
				out.write(protocolIdentifier);
	
				/** [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data. */
				out.write(dataCodingScheme);
	
				/**
				 * [TP-VP: TP-Validity-Period] Parameter identifying the time from where the message is no longer valid.
				 * Here, this is always relative.
				 */
				out.write(getRelativeVP(validityPeriod));
				
				// Build the UD
				
				// First build the UDH
				/** Total length of the udh, including the UDHL.  Do not confuse with udhBytes.length, which is the length of the UDH's content */
				int udhTotalLength = getUDHSize(true, isPorted, isMultipart);
	
				byte[] encodedText = encodeUcs2Text(messageParts[partNumber-1]);
				
				/** The length, in octets, of the user data, including the header. */
				int dataLen = encodedText.length + udhTotalLength;
	
				/** [TP-UDL: TP-User-Data-Length] Length of the UD, specific to the encoding. */
				out.write(dataLen);
				if(requiresUdh) {
					/** Now write the octet content of the UDH */
					out.write(generateUDH(partNumber, totalParts, concatReferenceNumber, sourcePort, destinationPort));
				}
				
				/** Add the MS to the PDU */
				out.write(encodedText);
				
				pdus[partNumber-1] = HexUtils.encode(out.toByteArray());
			}
			return pdus;
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Decodes the 7-byte TP-Service-Centre-Time-Stamp parameter, which identifies the time when the SC received a message.
	 * [TP-SCTS: TP-Service-Centre-Time-Stamp] Parameter identifying time when the SC received the message.
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	public static long decodeServiceCentreTimeStamp(InputStream in) throws IOException {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT")); 
		cal.set(Calendar.YEAR, decodeSemiOctetNumber(in.read()) + 2000);
		cal.set(Calendar.MONTH, decodeSemiOctetNumber(in.read()) - 1);
		cal.set(Calendar.DAY_OF_MONTH, decodeSemiOctetNumber(in.read()));
		cal.set(Calendar.HOUR_OF_DAY, decodeSemiOctetNumber(in.read()));
		cal.set(Calendar.MINUTE, decodeSemiOctetNumber(in.read()));
		cal.set(Calendar.SECOND, decodeSemiOctetNumber(in.read()));
		cal.set(Calendar.MILLISECOND, 0);
		/**
		 * Number of minutes different from GMT that this time is.
		 * 
		 * The Time Zone indicates the difference, expressed in quarters of an hour, between the local
		 * time and GMT. In the first of the two semi-octets, the first bit (bit 3 of the seventh octet
		 * of the TP-Service-Centre-Time-Stamp field) represents the algebraic sign of this difference
		 * (0: positive, 1: negative).
		 */
//		int timeZoneDifference = 15 * decodeSemiOctetNumber(in.read());
		in.read(); // FIXME get the timezone difference, and use it!
		// TODO do something useful with the timezone
		return cal.getTimeInMillis();
	}
	
	/**
	 * 
	 * @param twoSemiOctets byte containing two semi-octet values.
	 * @return Integer representation of the number encoded in the supplied octet as two semi-octets.
	 */
	private static final int decodeSemiOctetNumber(int twoSemiOctets) {
		return Integer.parseInt(decodeSemiOctetChars(twoSemiOctets));
	}

	/**
	 * 
	 * @param twoSemiOctets byte containing two semi-octet values.
	 * @return String representation of the number encoded in the supplied octet.  The string will be trimmed.
	 */
	private static final String decodeSemiOctetChars(int twoSemiOctets) {
		StringBuilder bob = new StringBuilder();
		char c;
		c = SEMI_OCTET.charAt( twoSemiOctets        & 0xF);
		if(c != ' ') bob.append(c);
		c = SEMI_OCTET.charAt((twoSemiOctets >>> 4) & 0xF);
		if(c != ' ') bob.append(c);
		return bob.toString();
	}
	
	// TODO add javadoc
	static String[] generatePdus_gsm7bit(String messageText, String smscNumber, String recipientMsisdn, int concatReferenceNumber, int sourcePort, int destinationPort, boolean requestStatusReport, int validityPeriod, int protocolIdentifier, int dataCodingScheme) {
		boolean isPorted = sourcePort > 0 || destinationPort > 0;
		String[] messageParts = GsmAlphabet.splitText(messageText, isPorted);
		
		final int totalParts = messageParts.length;
		boolean isMultipart = totalParts > 1;
		boolean requiresUdh = isMultipart||isPorted;
		
		/** PDUs that make up this message, encoded as hexadecimal strings. */
		String[] pdus = new String[totalParts];
		
		try {
			for(int partNumber=1; partNumber<=totalParts; ++partNumber) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
	
				out.write(encodeMsisdnAsAddressField(smscNumber, true));
				
				// get the front byte, which identifies message content
				out.write(getByteZero(TP_MTI_MO_SUBMIT, requiresUdh, requestStatusReport));
				
				// Message reference.  Always zero here. 
				/** [TP-MR: TP-Message-Reference] Parameter identifying the SMS-SUBMIT. */ 
				out.write(0);
	
				// Add the recipient's MSISDN
				/** [TP-DA: TP-Destination-Address] Address of the destination SME. */
				out.write(encodeMsisdnAsAddressField(recipientMsisdn, false));
				
				/** [TP-PID: TP-Protocol-Identifier] Parameter identifying the above layer protocol, if any. */
				out.write(protocolIdentifier);
	
				/** [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data. */
				out.write(dataCodingScheme);
	
				/**
				 * [TP-VP: TP-Validity-Period] Parameter identifying the time from where the message is no longer valid.
				 * Here, this is always relative.
				 */
				out.write(getRelativeVP(validityPeriod));
				
				// Build the UD
				
				// First build the UDH
				/** Total length of the udh, including the UDHL.  Do not confuse with udhBytes.length, which is the length of the UDH's content */
				int udhTotalLength = getUDHSize(true, isPorted, isMultipart);

				byte[] encodedMessageSeptets = GsmAlphabet.stringToBytes(messageParts[partNumber-1]);
				/** Encode the message text using the standard 7-bit GSM alphabet. */
				int skipBits = GsmAlphabet.calculateBitSkip(udhTotalLength);
				byte[] encodedMessageText = GsmAlphabet.septetStream2octetStream(encodedMessageSeptets, skipBits);
				
				/**
				 * [TP-UDL: TP-User-Data-Length] Length of the UD, specific to the encoding.
				 * For a 7-bit GSM charset message, this is the number of septets in the UD.
				 */
				int udLength = (int)Math.ceil(((udhTotalLength * 8) + (encodedMessageSeptets.length * 7) + skipBits) / 7.0);
				out.write(udLength);
				
				if(requiresUdh) {
					/** Now write the octet content of the UDH */
					out.write(generateUDH(partNumber, totalParts, concatReferenceNumber, sourcePort, destinationPort));
				}
				
				out.write(encodedMessageText);
				
				pdus[partNumber-1] = HexUtils.encode(out.toByteArray());
			}
			return pdus;
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
