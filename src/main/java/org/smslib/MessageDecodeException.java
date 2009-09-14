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

	MessageDecodeException(String message) {
		super(message);
	}

	MessageDecodeException(String message, Throwable cause) {
		super(message, cause);
	}

}
