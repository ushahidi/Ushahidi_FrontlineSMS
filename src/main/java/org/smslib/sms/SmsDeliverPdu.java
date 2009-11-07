/**
 * 
 */
package org.smslib.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.smslib.sms.content.BinarySmsMessageContent;
import org.smslib.sms.content.Gsm7bitTextSmsMessageContent;
import org.smslib.sms.content.Ucs2TextSmsMessageContent;
import org.smslib.util.TpduUtils;

/**
 * Pdu for SMS of type SMS-DELIVER - "conveying a short message from the SC to the MS".
 * @author Alex
 */
public class SmsDeliverPdu implements PduComponent {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/** The number of the SMS Centre that this message was sent by, or <code>null</code> if this was not set. */
	private final String smscNumber;
	/** [TP-OA: TP-Originating-Address] Address of the originating SME. */
	private final String originator;
	/** [TP-PID: TP-Protocol-Identifier] Parameter identifying the above layer protocol, if any. */
	private final int protocolIdentifier;
	/** [TP-SCTS: TP-Service-Centre-Time-Stamp] Parameter identifying time when the SC received the message, expressed as a java timestamp. */
	private final long serviceCentreTimestamp;
	/**
	 * [TP-UD: TP-User Data]
	 * The TP-User-Data field may comprise just the short message itself or a Header in addition to the short message depending upon the setting of TP-UDHI.
	 */
	private final UserData userData;

//> CONSTRUCTORS
	/**
	 * @param smscNumber
	 * @param originator
	 * @param protocolIdentifier
	 * @param serviceCentreTimestamp
	 * @param userData
	 */
	private SmsDeliverPdu(String smscNumber, String originator, int protocolIdentifier, long serviceCentreTimestamp, UserData userData) {
		this.smscNumber = smscNumber;
		this.originator = originator;
		this.protocolIdentifier = protocolIdentifier;
		this.serviceCentreTimestamp = serviceCentreTimestamp;
		this.userData = userData;
	}

//> ACCESSORS
	/** @return {@link #originator} */
	public String getOriginator() {
		return this.originator;
	}
	
	/** @return {@link #userData} */
	public UserData getUserData() {
		return this.userData;
	}

	/** @return {@link #serviceCentreTimestamp} */
	public long getServiceCentreTimestamp() {
		return this.serviceCentreTimestamp;
	}

	/** @return the {@link SmsMessageEncoding} of the {@link SmsMessageContent} */
	public SmsMessageEncoding getMessageEncoding() {
		SmsMessageContent messageContent = this.userData.getMessage();
		if (messageContent instanceof BinarySmsMessageContent) {
			return SmsMessageEncoding.BINARY_8BIT;
		} else if (messageContent instanceof Gsm7bitTextSmsMessageContent) {
			return SmsMessageEncoding.GSM_7BIT;
		} else if (messageContent instanceof Ucs2TextSmsMessageContent) {
			return SmsMessageEncoding.UCS2;
		} else {
			return SmsMessageEncoding.EncCustom;
		}
	}
	
	/** Converts this object to a String suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"smscNumber:'" + this.smscNumber + "', " +
				"smscNumber:'" + this.protocolIdentifier + "', " +
				"smscNumber:'" + this.serviceCentreTimestamp + "', " +
				"smscNumber:'" + this.originator + "', " +
				"userData:'" + this.userData + "'" + 
				"]";
	}

//> PduComponent METHODS
	/** @see PduComponent#toBinary() */
	public byte[] toBinary() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			// Write the SMSC number
			if(this.smscNumber != null) {
				out.write(TpduUtils.encodeMsisdnAsAddressField(this.smscNumber, true));
			}
			
			// TODO Write byte zero
			
			// Write the originator
			out.write(TpduUtils.encodeMsisdnAsAddressField(this.originator, false));
			
			// Write the PID
			out.write(this.protocolIdentifier);
			
			// TODO Write the DCS
			
			// TODO Write the SCTS
			
			// TODO Write the UDL
			
			// Write the UD
			out.write(this.userData.toBinary());
			
			return out.toByteArray();
		} catch(IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES
	/**
	 * @param hexEncodedPdu The PDU as a hexadecimal string
	 * @return The {@link SmsDeliverPdu} described by the provded PDU. 
	 * @throws PduDecodeException if there was a problem decoding the pdu
	 */
	public static final SmsDeliverPdu getFromHex(String hexEncodedPdu) throws PduDecodeException {
		try {
			return getFromStream(new PduInputStream(hexEncodedPdu));
		} catch (IOException ex) {
			throw new PduDecodeException(ex);
		}
	}
	
	/**
	 * Decodes an {@link SmsDeliverPdu} from the supplied {@link PduInputStream}.
	 * @param in
	 * @return the {@link SmsDeliverPdu} read from the input stream
	 * @throws IOException
	 * @throws PduDecodeException
	 */
	private static final SmsDeliverPdu getFromStream(PduInputStream in) throws IOException, PduDecodeException {
		/** The number of the SMS center.  Not a very interesting thing to know, but still necessary to remove it from the front of the PDU */
		String smscNumber = TpduUtils.decodeMsisdnFromAddressField(in, true);
		
		// get the front byte, which identifies message content
		byte byteZero = (byte)in.read();
		boolean hasUdh = TpduUtils.hasUdh(byteZero);

		/** [TP-OA: TP-Originating-Address] Address of the originating SME. */
		String originator = TpduUtils.decodeMsisdnFromAddressField(in, false);

		/** [TP-PID: TP-Protocol-Identifier] Parameter identifying the above layer protocol, if any. */
		int protocolIdentifier = in.read();

		/** [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data. */
		int dataCodingScheme = in.read();
		SmsMessageEncoding messageEncoding = TpduUtils.getMessageEncoding(dataCodingScheme);
		
		/** [TP-SCTS: TP-Service-Centre-Time-Stamp] Parameter identifying time when the SC received the message. */
		long serviceCentreTimeStamp = TpduUtils.decodeServiceCentreTimeStamp(in);
		
		/** [TP-UDL: TP-User-Data-Length] Length of the UD, specific to the encoding. */
		int userDataLength = in.read();
		
		/** [TP-UD: TP-User Data] */
		UserData userData = UserData.getFromStream(in, messageEncoding, hasUdh, userDataLength);
		
		return new SmsDeliverPdu(smscNumber, originator, protocolIdentifier, serviceCentreTimeStamp, userData);
	}

//> STATIC HELPER METHODS
}
