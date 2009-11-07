/**
 * 
 */
package org.smslib.util;

/**
 * Exception thrown when there was a problem decoding a hexadecimal String into its byte values.
 * 
 * @author Alex
 */
@SuppressWarnings("serial")
public class HexDecodeException extends IllegalArgumentException {
	/** @see Exception#Exception(String) */
	HexDecodeException(String message) {
		super(message);
	}
}
