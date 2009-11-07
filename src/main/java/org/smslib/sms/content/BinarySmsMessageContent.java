/**
 * 
 */
package org.smslib.sms.content;


import org.smslib.sms.SmsMessageContent;
import org.smslib.util.HexUtils;

/**
 * @author Alex
 *
 */
public class BinarySmsMessageContent implements SmsMessageContent {
	
//> INSTANCE PROPERTIES
	/** The binary content of this message */
	private final byte[] content;
	
//> CONSTRUCTOR
	/**
	 * Create a new instance of {@link BinarySmsMessageContent}
	 * @param messageContent value for {@link #content}
	 */
	public BinarySmsMessageContent(byte[] messageContent) {
		this.content = messageContent;
	}
	
//> ACCESSORS
	/** @return {@link #content} */
	public byte[] getContent() {
		return content;
	}

	/** Converts this object to a string suitable for logging. */
	@Override
	public String toString() {
		return this.getClass() + "[" +
				"content:'" + (this.content == null ? null : HexUtils.encode(this.content)) + "'" + 
				"]";
	}
	
//> SmsContent METHODS
	/** @see org.smslib.sms.PduComponent#toBinary() */
	public byte[] toBinary() {
		return this.content;
	}

//> STATIC FACTORY METHODS
	/**
	 * @param udWithoutHeader The user data, not including the UD-Header.  This byte[] is referenced directly by the new {@link BinarySmsMessageContent}.
	 * @return a new instance of {@link BinarySmsMessageContent}, with the supplied payload as the message content
	 */
	public static BinarySmsMessageContent getFromMs(byte[] udWithoutHeader) {
		return new BinarySmsMessageContent(udWithoutHeader);
	}

}
