/**
 * 
 */
package org.smslib;

/**
 * Exception thrown when an attempt is made to encode an SMS using illegal parameters.
 * @author Alex Anderson
 */
@SuppressWarnings("serial")
public class IllegalSmsEncodingException extends RuntimeException {

	public IllegalSmsEncodingException(String message) {
		super(message);
	}

}
