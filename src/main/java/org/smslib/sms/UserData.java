package org.smslib.sms;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.smslib.sms.content.BinarySmsMessageContent;
import org.smslib.sms.content.Gsm7bitTextSmsMessageContent;
import org.smslib.sms.content.Ucs2TextSmsMessageContent;

/**
 * The UD part of an SMS message
 * @author Alex
 */
public class UserData implements PduComponent {
	
//> INSTANCE PROPERTIES
	/** Logging object */
	private Logger log = Logger.getLogger(this.getClass());
	/** The UDH.  This is optional */
	private UserDataHeader header;
	/** The message content of the message */
	private SmsMessageContent message;
	

//> SmsComponent METHODS
	/** @see PduComponent#toBinary() */
	public byte[] toBinary() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if(this.header != null) out.write(header.toBinary());
			out.write(message.toBinary());
			return out.toByteArray();
		} catch (IOException e) {
			// This should never happen
			log.warn(e);
			return null;
		}
	}
	
//> ACCESSOR METHODS
	/** @return #header */
	public UserDataHeader getHeader() {
		return header;
	}
	/** @param header new value for {@link #header} */
	public void setHeader(UserDataHeader header) {
		this.header = header;
	}
	/** @return {@link #message} */
	public SmsMessageContent getMessage() {
		return message;
	}
	/** @param message new value for {@link #message} */
	public void setMessage(SmsMessageContent message) {
		this.message = message;
	}
	
	/** Converts this object to a string suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"header:'" + this.header + "', " +
				"message:'" + this.message + "'" +
				"]";
	}
	
//> STATIC FACTORY METHODS
	/**
	 * @param in 
	 * @param messageEncoding the message encoding used, from {@link SmsMessageEncoding}}
	 * @param hasUdh <code>true</code> if there is a {@link UserDataHeader} present; <code>false</code> otherwise.
	 * @param userDataLength the User-Data-Length, as read from the PDU
	 * @return a new {@link UserData}, as deciphered from the supplied stream
	 * @throws PduDecodeException 
	 * @throws IOException 
	 */
	public static UserData getFromStream(PduInputStream in, SmsMessageEncoding messageEncoding, boolean hasUdh, int userDataLength) throws IOException, PduDecodeException {
		UserData userData = new UserData();

		int udhLength = 0;
		if(hasUdh) {
			UserDataHeader header = UserDataHeader.getFromStream(in);
			userData.setHeader(header);
			udhLength = header.getLength();
		}
		
		/** For GSM 7-bit encoded messages, we need to know the number of septets comprising the actual message. */
		int msSeptetCount = 0; 
		/** The length in octets of the data contained in the MS */
		int payloadLength;
		if(messageEncoding == SmsMessageEncoding.GSM_7BIT) {
			// For 7-bit GSM alphabet messages, the TP-UDL is the number of characters in the MS
			// plus the number of octets in the UDH, including the UDH's length octet.
			int totalOctets = (int)Math.ceil((userDataLength * 7) / 8.0);
			payloadLength = totalOctets - udhLength;
			int udhSeptets = (int)Math.ceil((udhLength * 8) / 7.0);
			msSeptetCount = userDataLength - udhSeptets;
		} else {
			// For binary and UCS-2 messages, the TP-UDL is the total number of octets in the UD
			payloadLength = userDataLength - udhLength;
		}
		byte[] udWithoutHeader = new byte[payloadLength];
		in.readFully(udWithoutHeader);
		
		try {
			in.read();
			throw new PduDecodeException("There were unexpected bytes at the end of this message.");
		} catch(EOFException ex) { /* Stream ends where we expected it to. */ }
		
		userData.setMessage(createEmmEss(messageEncoding, udWithoutHeader, udhLength, msSeptetCount));
		
		return userData;
	}
	
	/**
	 * Creates a new {@link SmsMessageContent} from the supplied binary UD content
	 * @param messageEncoding
	 * @param udWithoutHeader the content of the UD, not including the UD-Header
	 * @param udhLength
	 * @param msSeptetCount
	 * @return message content of the supplied UD, decoded in the appropriate form
	 */
	private static SmsMessageContent createEmmEss(SmsMessageEncoding messageEncoding, byte[] udWithoutHeader, int udhLength, int msSeptetCount) {
		if(messageEncoding == SmsMessageEncoding.GSM_7BIT) {
			// The position of the MS data actually depends on the length of the UDH, for some reason
			return Gsm7bitTextSmsMessageContent.getFromMs(udWithoutHeader, udhLength, msSeptetCount);
		} else if(messageEncoding == SmsMessageEncoding.UCS2) {
			return Ucs2TextSmsMessageContent.getFromMs(udWithoutHeader);
		} else {
			// Treat custom message encoding as binary 
			return BinarySmsMessageContent.getFromMs(udWithoutHeader);
		}
	}
}
