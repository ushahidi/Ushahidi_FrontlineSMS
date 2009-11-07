/**
 * 
 */
package org.smslib.sms.content;

import org.smslib.util.TpduUtils;

/**
 * @author Alex
 *
 */
public class Ucs2TextSmsMessageContent implements TextSmsMessageContent {
	
//> INSTANCE PROPERTIES
	/** The text content of this message */
	private final String content;

//> CONSTRUCTORS
	/**
	 * Create a new {@link Ucs2TextSmsMessageContent}.
	 * @param messageContent
	 */
	public Ucs2TextSmsMessageContent(String messageContent) {
		// TODO check characters are valid
		// TODO check message length is reasonable (really?  we don't know if there's a header...)
		this.content = messageContent;
	}
	
//> ACCESSORS
	/** @see org.smslib.sms.content.TextSmsMessageContent#getContent() */
	public String getContent() {
		return content;
	}
	
	/** Converts this object to a string suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"content:'" + this.content + "'" + 
				"]";
	}

//> SmsContent METHODS
	/** @see org.smslib.sms.PduComponent#toBinary() */
	public byte[] toBinary() {
		throw new IllegalStateException("NYI");
	}

//> STATIC FACTORY METHODS
	/**
	 * @param udWithoutHeader The user data, not including the UD-Header
	 * @return a {@link Ucs2TextSmsMessageContent} with the message content set as the decoded UD that was supplied
	 */
	public static Ucs2TextSmsMessageContent getFromMs(byte[] udWithoutHeader) {
		String messageContent = TpduUtils.decodeUcs2Text(udWithoutHeader);
		return new Ucs2TextSmsMessageContent(messageContent);
	}

}
