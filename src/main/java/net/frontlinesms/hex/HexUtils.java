/**
 * 
 */
package net.frontlinesms.hex;

/**
 * Utilities for encoding and decoding hexadecimal strings and byte arrays.
 * 
 * @author Alex
 */
public class HexUtils {
	/** Characters used in a hexadecimal String */
	private static final String HEX_CHARS = "0123456789ABCDEF";
	
	/**
	 * Convert the supplied number to hexadecimal.
	 * @param num
	 * @return
	 */
	public static final String encode(long num, int charCount) {
		if(charCount > 16) throw new HexEncodeException("Char count is too large: " + charCount);
		char[] bob = new char[charCount];
		
		while(--charCount >= 0) {
			bob[charCount] = HEX_CHARS.charAt((int)(num & 0xF));
			num >>>= 4;
		}
		
		return new String(bob);
	}
	
	/**
	 * Convert a byte[] into a string of hexadecimal.
	 * @param bytes
	 * @return
	 */
	public static final String encode(byte[] bytes) {
		char[] bob = new char[bytes.length << 1];
		int i = bytes.length;
		while(--i >= 0) {
			byte b = bytes[i];
			int bobIndex = i << 1;
			bob[bobIndex  ] = HEX_CHARS.charAt((b>>4) & 0xF);
			bob[bobIndex+1] = HEX_CHARS.charAt(b & 0xF);
		}
		return new String(bob);
	}
	
	/**
	 * Convert a hex String into a byte[].
	 * @param hex
	 * @return
	 */
	public static final byte[] decode(String hex) {
		int len = hex.length();
		if(len % 2 != 0) throw new HexDecodeException("Supplied hex string's length was odd ('"+hex+"': "+len+")");
		hex = hex.toUpperCase();
		byte[] bytes = new byte[len>>1];
		for(int i=bytes.length-1; i>=0; --i) {
			int idx = i << 1;
			bytes[i] = (byte)(((getNibbleValue(hex.charAt(idx)) << 4) | getNibbleValue(hex.charAt(idx + 1))) & 0xFF);
		}
		return bytes;
	}
	
	/**
	 * Gets the 4-bit value of a hexadecimal character.
	 * @param hexChar a character found in {@link #HEX_CHARS}
	 * @return
	 */
	private static final byte getNibbleValue(int hexChar) {
		int value = HEX_CHARS.indexOf(hexChar);
		if(value == -1) throw new HexDecodeException("Not a valid hex character: '"+(char)hexChar+"'");
		return (byte)value;
	}
	
	public static void main(String[] args) {
		for(long j=256; j<512; j+= 1) {
			for(int i=1; i<=8; ++i) {
				System.out.println(j + " (" + i + ") : " + encode(j, i));
			}
		}
	}
}
