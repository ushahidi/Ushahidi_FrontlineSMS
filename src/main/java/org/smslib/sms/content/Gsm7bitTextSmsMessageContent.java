/**
 * 
 */
package org.smslib.sms.content;

import org.smslib.util.GsmAlphabet;

/**
 * @author Alex
 *
 */
public class Gsm7bitTextSmsMessageContent implements TextSmsMessageContent {
	
//> INSTANCE PROPERTIES
	/** The text content of this message */
	private final String messageContent;

//> CONSTRUCTORS
	/**
	 * Create a new {@link Gsm7bitTextSmsMessageContent}.
	 * @param messageContent
	 */
	public Gsm7bitTextSmsMessageContent(String messageContent) {
		// TODO check characters are valid
		// TODO check message length is reasonable (really?  we don't know if there's a header...)
		this.messageContent = messageContent;
	}
	
//> ACCESSORS
	/** @see org.smslib.sms.content.TextSmsMessageContent#getContent() */
	public String getContent() {
		return messageContent;
	}
	
	/** Converts this object to a string suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"content:'" + this.messageContent + "'" + 
				"]";
	}

//> SmsContent METHODS
	/** @see org.smslib.sms.PduComponent#toBinary() */
	public byte[] toBinary() {
		throw new IllegalStateException("NYI");
	}

//> STATIC FACTORY METHODS
	/**
	 * @param udWithoutHeader
	 * @param udhLength
	 * @param msSeptetCount
	 * @return a text message using the {@link GsmAlphabet}, decoded from the supplied data
	 */
	public static Gsm7bitTextSmsMessageContent getFromMs(byte[] udWithoutHeader, int udhLength, int msSeptetCount) {
		int skipBit = GsmAlphabet.calculateBitSkip(udhLength);
		String messageContent = GsmAlphabet.bytesToString(GsmAlphabet.octetStream2septetStream(udWithoutHeader, skipBit, msSeptetCount));
		return new Gsm7bitTextSmsMessageContent(messageContent);
	}
}
