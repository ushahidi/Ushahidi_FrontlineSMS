/**
 * 
 */
package net.frontlinesms.encoding;

import java.io.IOException;

/**
 * @author Alex
 *
 */
public class Base64Utils {
	/**
	 * Decode data encoded as a Base64 string.
	 * @param base64string
	 * @return the data that was base 64 encoded
	 */
	public static byte[] decode(String base64string) {
		try {
			return Base64Codec.decode(base64string);
		} catch (IOException e) {
			// This should never occur
			return null;
		}
	}
	
	/**
	 * Encode the supplied data into a base64 string.
	 * @param data
	 * @return a base 64 string, without linebreaks
	 */
	public static String encode(byte[] data) {
		return Base64Codec.encodeBytes(data).trim();
	}
}
