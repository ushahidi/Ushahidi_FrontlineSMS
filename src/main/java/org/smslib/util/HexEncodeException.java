/**
 * 
 */
package org.smslib.util;

/**
 * Exception thrown when encoding a hex string.
 * 
 * @author Alex
 */
@SuppressWarnings("serial")
public class HexEncodeException extends IllegalArgumentException {
	/** @see Exception#Exception(String) */
	public HexEncodeException(String message) {
		super(message);
	}
}
