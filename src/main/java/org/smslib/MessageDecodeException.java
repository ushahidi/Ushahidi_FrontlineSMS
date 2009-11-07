/**
 * 
 */
package org.smslib;

/**
 * Exception thrown when there was a problem decoding an SMS message.
 * @author Alex
 */
@SuppressWarnings("serial")
public class MessageDecodeException extends Exception {

	/** @see Exception#Exception(String) */
	public MessageDecodeException(String message) {
		super(message);
	}

	/** @see Exception#Exception(String, Throwable) */
	public MessageDecodeException(String message, Throwable cause) {
		super(message, cause);
	}

}
