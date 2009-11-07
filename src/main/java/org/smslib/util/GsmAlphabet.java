// SMSLib for Java
// An open-source API Library for sending and receiving SMS via a GSM modem.
// Copyright (C) 2002-2007, Thanasis Delenikas, Athens/GREECE
// Copyright (C) 2009, Alex Anderson, Masabi, Kiwanja
// Web Site: http://www.smslib.org
//
// SMSLib is distributed under the LGPL license.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

package org.smslib.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;



/**
 * Following this spec: ETSI TS 123 038 V8.2.0 (2008-10)
 * 
 * TODO add single shift table: Turkish
 * TODO add single shift table: Spanish
 * TODO add single shift table: Portuguese
 * TODO add locking shift table: Turkish
 * TODO add locking shift table: Portuguese
 * 
 * TODO possibly add Cyrillic character remapping like that available for Greek à la http://www.translit.ru/
 * 
 * TODO Add detection to check if shift tables are required rather than using UCS-2 encoding on a message.
 * 
 * @author Alex
 */
public class GsmAlphabet {
	/**
	 * 7-bit GSM Alphabet value which escapes to alphabet extension or national language single shift table.
	 * "A receiving entity which does not understand the meaning of this escape mechanism shall display it as a space character."
	 */
	private static final byte ESCAPE_SHIFT = 0x1B;

	/**
	 * If set <code>true</code>, Greek characters not found in the standard 7-bit GSM
	 * alphabet will be remapped to other characters which are in this alphabet.
	 * Ultimately, I think this will be a matter of taste for Greek users, so this should
	 * probably be an optional argument on this class or its methods.
	 */
	private static final boolean REMAP_GREEK_CHARACTERS = false;
	/**
	 * This array maps Greek characters that look the same as Latin characters
	 * to these corresponding Latin characters. 
	 */
	private static final char[][] ALPHABET_REMAPPING_GREEK = {
			{ '\u0386', '\u0041' }, // GREEK CAPITAL LETTER ALPHA WITH TONOS
			{ '\u0388', '\u0045' }, // GREEK CAPITAL LETTER EPSILON WITH TONOS
			{ '\u0389', '\u0048' }, // GREEK CAPITAL LETTER ETA WITH TONOS
			{ '\u038A', '\u0049' }, // GREEK CAPITAL LETTER IOTA WITH TONOS
			{ '\u038C', '\u004F' }, // GREEK CAPITAL LETTER OMICRON WITH TONOS
			{ '\u038E', '\u0059' }, // GREEK CAPITAL LETTER UPSILON WITH TONOS
			{ '\u038F', '\u03A9' }, // GREEK CAPITAL LETTER OMEGA WITH TONOS
			{ '\u0390', '\u0049' }, // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
			{ '\u0391', '\u0041' }, // GREEK CAPITAL LETTER ALPHA
			{ '\u0392', '\u0042' }, // GREEK CAPITAL LETTER BETA
			{ '\u0393', '\u0393' }, // GREEK CAPITAL LETTER GAMMA
			{ '\u0394', '\u0394' }, // GREEK CAPITAL LETTER DELTA
			{ '\u0395', '\u0045' }, // GREEK CAPITAL LETTER EPSILON
			{ '\u0396', '\u005A' }, // GREEK CAPITAL LETTER ZETA
			{ '\u0397', '\u0048' }, // GREEK CAPITAL LETTER ETA
			{ '\u0398', '\u0398' }, // GREEK CAPITAL LETTER THETA
			{ '\u0399', '\u0049' }, // GREEK CAPITAL LETTER IOTA
			{ '\u039A', '\u004B' }, // GREEK CAPITAL LETTER KAPPA
			{ '\u039B', '\u039B' }, // GREEK CAPITAL LETTER LAMDA
			{ '\u039C', '\u004D' }, // GREEK CAPITAL LETTER MU
			{ '\u039D', '\u004E' }, // GREEK CAPITAL LETTER NU
			{ '\u039E', '\u039E' }, // GREEK CAPITAL LETTER XI
			{ '\u039F', '\u004F' }, // GREEK CAPITAL LETTER OMICRON
			{ '\u03A0', '\u03A0' }, // GREEK CAPITAL LETTER PI
			{ '\u03A1', '\u0050' }, // GREEK CAPITAL LETTER RHO
			{ '\u03A3', '\u03A3' }, // GREEK CAPITAL LETTER SIGMA
			{ '\u03A4', '\u0054' }, // GREEK CAPITAL LETTER TAU
			{ '\u03A5', '\u0059' }, // GREEK CAPITAL LETTER UPSILON
			{ '\u03A6', '\u03A6' }, // GREEK CAPITAL LETTER PHI
			{ '\u03A7', '\u0058' }, // GREEK CAPITAL LETTER CHI
			{ '\u03A8', '\u03A8' }, // GREEK CAPITAL LETTER PSI
			{ '\u03A9', '\u03A9' }, // GREEK CAPITAL LETTER OMEGA
			{ '\u03AA', '\u0049' }, // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA
			{ '\u03AB', '\u0059' }, // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA
			{ '\u03AC', '\u0041' }, // GREEK SMALL LETTER ALPHA WITH TONOS
			{ '\u03AD', '\u0045' }, // GREEK SMALL LETTER EPSILON WITH TONOS
			{ '\u03AE', '\u0048' }, // GREEK SMALL LETTER ETA WITH TONOS
			{ '\u03AF', '\u0049' }, // GREEK SMALL LETTER IOTA WITH TONOS
			{ '\u03B0', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
			{ '\u03B1', '\u0041' }, // GREEK SMALL LETTER ALPHA
			{ '\u03B2', '\u0042' }, // GREEK SMALL LETTER BETA
			{ '\u03B3', '\u0393' }, // GREEK SMALL LETTER GAMMA
			{ '\u03B4', '\u0394' }, // GREEK SMALL LETTER DELTA
			{ '\u03B5', '\u0045' }, // GREEK SMALL LETTER EPSILON
			{ '\u03B6', '\u005A' }, // GREEK SMALL LETTER ZETA
			{ '\u03B7', '\u0048' }, // GREEK SMALL LETTER ETA
			{ '\u03B8', '\u0398' }, // GREEK SMALL LETTER THETA
			{ '\u03B9', '\u0049' }, // GREEK SMALL LETTER IOTA
			{ '\u03BA', '\u004B' }, // GREEK SMALL LETTER KAPPA
			{ '\u03BB', '\u039B' }, // GREEK SMALL LETTER LAMDA
			{ '\u03BC', '\u004D' }, // GREEK SMALL LETTER MU
			{ '\u03BD', '\u004E' }, // GREEK SMALL LETTER NU
			{ '\u03BE', '\u039E' }, // GREEK SMALL LETTER XI
			{ '\u03BF', '\u004F' }, // GREEK SMALL LETTER OMICRON
			{ '\u03C0', '\u03A0' }, // GREEK SMALL LETTER PI
			{ '\u03C1', '\u0050' }, // GREEK SMALL LETTER RHO
			{ '\u03C2', '\u03A3' }, // GREEK SMALL LETTER FINAL SIGMA
			{ '\u03C3', '\u03A3' }, // GREEK SMALL LETTER SIGMA
			{ '\u03C4', '\u0054' }, // GREEK SMALL LETTER TAU
			{ '\u03C5', '\u0059' }, // GREEK SMALL LETTER UPSILON
			{ '\u03C6', '\u03A6' }, // GREEK SMALL LETTER PHI
			{ '\u03C7', '\u0058' }, // GREEK SMALL LETTER CHI
			{ '\u03C8', '\u03A8' }, // GREEK SMALL LETTER PSI
			{ '\u03C9', '\u03A9' }, // GREEK SMALL LETTER OMEGA
			{ '\u03CA', '\u0049' }, // GREEK SMALL LETTER IOTA WITH DIALYTIKA
			{ '\u03CB', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA
			{ '\u03CC', '\u004F' }, // GREEK SMALL LETTER OMICRON WITH TONOS
			{ '\u03CD', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH TONOS
			{ '\u03CE', '\u03A9' } // GREEK SMALL LETTER OMEGA WITH TONOS
	};

	/**
	 * Characters available in the GSM 7 bit default alphabet extension table.
	 * The position in this array maps a character directly to it's 7-bit GSM value contained in {@link #EXTENDED_ALPHABET_BYTES}.
	 */
	private static final char[] EXTENDED_ALPHABET = {
			'\u000c', // FORM FEED
			'\u005e', // CIRCUMFLEX ACCENT
			'\u007b', // LEFT CURLY BRACKET
			'\u007d', // RIGHT CURLY BRACKET
			'\\', // REVERSE SOLIDUS
			'\u005b', // LEFT SQUARE BRACKET
			'\u007e', // TILDE
			'\u005d', // RIGHT SQUARE BRACKET
			'\u007c', // VERTICAL LINES
			'\u20ac', // EURO SIGN
	};
	/** String representation of {@link #EXTENDED_ALPHABET} */
	private static final String EXTENDED_ALPHABET_STRING = new String(EXTENDED_ALPHABET);

	/**
	 * Codes mapping from default alphabet extension table values to {@link #EXTENDED_ALPHABET} characters.
	 * A value's position in this array maps a 7-bit GSM alphabet value directly to its Java <code>char</code> value in {@link #EXTENDED_ALPHABET}.
	 */
	private static final byte[] EXTENDED_ALPHABET_BYTES = {
			0x0a, // FORM FEED
			0x14, // CIRCUMFLEX ACCENT
			0x28, // LEFT CURLY BRACKET
			0x29, // RIGHT CURLY BRACKET
			0x2f, // REVERSE SOLIDUS
			0x3c, // LEFT SQUARE BRACKET
			0x3d, // TILDE
			0x3e, // RIGHT SQUARE BRACKET
			0x40, // VERTICAL LINES
			0x65, // EURO SIGN
	};
	
	/**
	 * Standard GSM 7-bit Alphabet.
	 */
	private static final char[] STANDARD_ALPHABET = {
			'\u0040', // COMMERCIAL AT
			'\u00A3', // POUND SIGN
			'\u0024', // DOLLAR SIGN
			'\u00A5', // YEN SIGN
			'\u00E8', // LATIN SMALL LETTER E WITH GRAVE
			'\u00E9', // LATIN SMALL LETTER E WITH ACUTE
			'\u00F9', // LATIN SMALL LETTER U WITH GRAVE
			'\u00EC', // LATIN SMALL LETTER I WITH GRAVE
			'\u00F2', // LATIN SMALL LETTER O WITH GRAVE
			'\u00C7', // LATIN CAPITAL LETTER C WITH CEDILLA (from GSM spec, errata for SMS Lib. bjdw Nov 2008)
			'\n', // LINE FEED
			'\u00D8', // LATIN CAPITAL LETTER O WITH STROKE
			'\u00F8', // LATIN SMALL LETTER O WITH STROKE
			'\r', // CARRIAGE RETURN
			'\u00C5', // LATIN CAPITAL LETTER A WITH RING ABOVE
			'\u00E5', // LATIN SMALL LETTER A WITH RING ABOVE
			'\u0394', // GREEK CAPITAL LETTER DELTA
			'\u005F', // LOW LINE
			'\u03A6', // GREEK CAPITAL LETTER PHI
			'\u0393', // GREEK CAPITAL LETTER GAMMA
			'\u039B', // GREEK CAPITAL LETTER LAMDA
			'\u03A9', // GREEK CAPITAL LETTER OMEGA
			'\u03A0', // GREEK CAPITAL LETTER PI
			'\u03A8', // GREEK CAPITAL LETTER PSI
			'\u03A3', // GREEK CAPITAL LETTER SIGMA
			'\u0398', // GREEK CAPITAL LETTER THETA
			'\u039E', // GREEK CAPITAL LETTER XI
			'\u00A0', // Escape to alphabet extension or national language single shift table.  "A receiving entity which does not understand the meaning of this escape mechanism shall display it as a space character."
			'\u00C6', // LATIN CAPITAL LETTER AE
			'\u00E6', // LATIN SMALL LETTER AE
			'\u00DF', // LATIN SMALL LETTER SHARP S (German)
			'\u00C9', // LATIN CAPITAL LETTER E WITH ACUTE
			'\u0020', // SPACE
			'\u0021', // EXCLAMATION MARK
			'\u0022', // QUOTATION MARK
			'\u0023', // NUMBER SIGN
			'\u00A4', // CURRENCY SIGN
			'\u0025', // PERCENT SIGN
			'\u0026', // AMPERSAND
			'\'', // APOSTROPHE
			'\u0028', // LEFT PARENTHESIS
			'\u0029', // RIGHT PARENTHESIS
			'\u002A', // ASTERISK
			'\u002B', // PLUS SIGN
			'\u002C', // COMMA
			'\u002D', // HYPHEN-MINUS
			'\u002E', // FULL STOP
			'\u002F', // SOLIDUS
			'\u0030', // DIGIT ZERO
			'\u0031', // DIGIT ONE
			'\u0032', // DIGIT TWO
			'\u0033', // DIGIT THREE
			'\u0034', // DIGIT FOUR
			'\u0035', // DIGIT FIVE
			'\u0036', // DIGIT SIX
			'\u0037', // DIGIT SEVEN
			'\u0038', // DIGIT EIGHT
			'\u0039', // DIGIT NINE
			'\u003A', // COLON
			'\u003B', // SEMICOLON
			'\u003C', // LESS-THAN SIGN
			'\u003D', // EQUALS SIGN
			'\u003E', // GREATER-THAN SIGN
			'\u003F', // QUESTION MARK
			'\u00A1', // INVERTED EXCLAMATION MARK
			'\u0041', // LATIN CAPITAL LETTER A
			'\u0042', // LATIN CAPITAL LETTER B
			'\u0043', // LATIN CAPITAL LETTER C
			'\u0044', // LATIN CAPITAL LETTER D
			'\u0045', // LATIN CAPITAL LETTER E
			'\u0046', // LATIN CAPITAL LETTER F
			'\u0047', // LATIN CAPITAL LETTER G
			'\u0048', // LATIN CAPITAL LETTER H
			'\u0049', // LATIN CAPITAL LETTER I
			'\u004A', // LATIN CAPITAL LETTER J
			'\u004B', // LATIN CAPITAL LETTER K
			'\u004C', // LATIN CAPITAL LETTER L
			'\u004D', // LATIN CAPITAL LETTER M
			'\u004E', // LATIN CAPITAL LETTER N
			'\u004F', // LATIN CAPITAL LETTER O
			'\u0050', // LATIN CAPITAL LETTER P
			'\u0051', // LATIN CAPITAL LETTER Q
			'\u0052', // LATIN CAPITAL LETTER R
			'\u0053', // LATIN CAPITAL LETTER S
			'\u0054', // LATIN CAPITAL LETTER T
			'\u0055', // LATIN CAPITAL LETTER U
			'\u0056', // LATIN CAPITAL LETTER V
			'\u0057', // LATIN CAPITAL LETTER W
			'\u0058', // LATIN CAPITAL LETTER X
			'\u0059', // LATIN CAPITAL LETTER Y
			'\u005A', // LATIN CAPITAL LETTER Z
			'\u00C4', // LATIN CAPITAL LETTER A WITH DIAERESIS
			'\u00D6', // LATIN CAPITAL LETTER O WITH DIAERESIS
			'\u00D1', // LATIN CAPITAL LETTER N WITH TILDE
			'\u00DC', // LATIN CAPITAL LETTER U WITH DIAERESIS
			'\u00A7', // SECTION SIGN
			'\u00BF', // INVERTED QUESTION MARK
			'\u0061', // LATIN SMALL LETTER A
			'\u0062', // LATIN SMALL LETTER B
			'\u0063', // LATIN SMALL LETTER C
			'\u0064', // LATIN SMALL LETTER D
			'\u0065', // LATIN SMALL LETTER E
			'\u0066', // LATIN SMALL LETTER F
			'\u0067', // LATIN SMALL LETTER G
			'\u0068', // LATIN SMALL LETTER H
			'\u0069', // LATIN SMALL LETTER I
			'\u006A', // LATIN SMALL LETTER J
			'\u006B', // LATIN SMALL LETTER K
			'\u006C', // LATIN SMALL LETTER L
			'\u006D', // LATIN SMALL LETTER M
			'\u006E', // LATIN SMALL LETTER N
			'\u006F', // LATIN SMALL LETTER O
			'\u0070', // LATIN SMALL LETTER P
			'\u0071', // LATIN SMALL LETTER Q
			'\u0072', // LATIN SMALL LETTER R
			'\u0073', // LATIN SMALL LETTER S
			'\u0074', // LATIN SMALL LETTER T
			'\u0075', // LATIN SMALL LETTER U
			'\u0076', // LATIN SMALL LETTER V
			'\u0077', // LATIN SMALL LETTER W
			'\u0078', // LATIN SMALL LETTER X
			'\u0079', // LATIN SMALL LETTER Y
			'\u007A', // LATIN SMALL LETTER Z
			'\u00E4', // LATIN SMALL LETTER A WITH DIAERESIS
			'\u00F6', // LATIN SMALL LETTER O WITH DIAERESIS
			'\u00F1', // LATIN SMALL LETTER N WITH TILDE
			'\u00FC', // LATIN SMALL LETTER U WITH DIAERESIS
			'\u00E0', // LATIN SMALL LETTER A WITH GRAVE
	};
	/** String representation of {@link #STANDARD_ALPHABET} */
	private static final String STANDARD_ALPHABET_STRING = new String(STANDARD_ALPHABET);
	
	/**
	 * Checks a String to see if the characters in it are all valid 7-bit GSM characters.
	 * @param text
	 * @return <code>true</code> if all characters in the supplied text are valid 7-bit GSM; <code>false</code> otherwise.
	 */
	public static boolean areAllCharactersValidGSM(String text) {
		for(char c : text.toCharArray()) {
			if(STANDARD_ALPHABET_STRING.indexOf(c) == -1 && EXTENDED_ALPHABET_STRING.indexOf(c) == -1)
				return false;
		}
		return true;
	}
	
	/**
	 * Removes non-GSM 7bit alphabet characters in a string, replacing with spaces as per the spec.
	 * @param unescaped An unescaped string.
	 * @return The same string, with spaces in place of invalid characters.
	 */
	public static String convertInvalidGSMCharactersToSpaces(String unescaped)
	{
		// perform GSM charset escaping by converting to 7bit values in a byte array, then converting back to a string...
		//  - note the most elegant solution, but means we don't have to reinvent wheel
		return bytesToString(stringToBytes(unescaped));
	}


	/**
	 * FIXME rename this method gsmSeptets2string or similar
	 * Converts a byte[] containing 7-bit GSM alphabet values (1 value per byte) into a String
	 * containing the characters that the supplied byte[] represents.
	 * @param bytes
	 * @return
	 */
	public static String bytesToString(byte[] bytes) {
		StringBuffer text = new StringBuffer(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			if(bytes[i] == ESCAPE_SHIFT) {
				text.append(getExtendedChar(bytes[++i]));
			} else text.append(STANDARD_ALPHABET[bytes[i]]);
		}
		return text.toString();
	}
	
	/**
	 * Gets the character value for an extended byte value found in a 7-bit GSM string.  The byte
	 * code provided to this method is preceded by {@value #ESCAPE_SHIFT} in the 7-bit GSM string.
	 * @param extendedByteCode
	 * @return
	 */
	private static final char getExtendedChar(byte extendedByteCode) {
		for (int j = 0; j < EXTENDED_ALPHABET_BYTES.length; j++) {
			if (EXTENDED_ALPHABET_BYTES[j] == extendedByteCode) {
				return EXTENDED_ALPHABET[j];
			}
		}
		throw new IllegalArgumentException("Unrecognized extended byte value: " + extendedByteCode);
	}
	
	/**
	 * Maps a Greek character to an equivalent from the GSM 7-bit standard alphabet.  If the
	 * Greek character is not contained in {@link #ALPHABET_REMAPPING_GREEK}, a space character is returned.
	 * TODO this should only be used if remapping has been requested, i suspect.
	 * @param greekChar
	 * @return
	 */
	private static final char greek2gsm7bit(char greekChar) {
		for (int i = 0; i < ALPHABET_REMAPPING_GREEK.length; i++) {
			if(ALPHABET_REMAPPING_GREEK[i][0] == greekChar) return ALPHABET_REMAPPING_GREEK[i][1];
		}
		return ' ';
	}

	
	/**
	 * FIXME rename this method octetStream2septetStream or similar
	 * Convert a stream of septets read as octets into a byte array containing the 7-bit
	 * values from the octet stream.
	 * FIXME old method, please remove
	 * @param encodedOctetStream octet stream encoded as a hexadecimal string.
	 * @return
	 */
	static byte[] pduToText(String encodedOctetStream) {
		if(encodedOctetStream.length() == 0) return new byte[0];
		return octetStream2septetStream(HexUtils.decode(encodedOctetStream), 0);
	}
	
	/**
	 * Convert a stream of septets read as octets into a byte array containing the 7-bit
	 * values from the octet stream.
	 * @param octets
	 * @param bitSkip
	 * FIXME pass the septet length in here, so if there is a spare septet at the end of the octet, we can ignore that
	 * @return
	 */
	static byte[] octetStream2septetStream(byte[] octets, int bitSkip) {
		return octetStream2septetStream(octets, bitSkip, ((8 * octets.length) - bitSkip) / 7);
	}
	
	public static byte[] octetStream2septetStream(byte[] octets, int bitSkip, int septetCount) {
		byte[] septets = new byte[septetCount];
		for(int newIndex=septets.length-1; newIndex>=0; --newIndex) {
			for(int bit=6; bit>=0; --bit) {
				int oldBitIndex = ((newIndex * 7) + bit) + bitSkip;
				if((octets[oldBitIndex >>> 3] & (1 << (oldBitIndex & 7))) != 0)
					septets[newIndex] |= (1 << bit);
			}
		}
		
		return septets;		
	}

	/**
	 * Encodes the supplied text with the 7-bit GSM character set, and places
	 * the encoded text in the supplied byte array.  Each septet takes
	 * up one byte of the generated array.
	 * @param text
	 * @return
	 */
	public static byte[] stringToBytes(String text) {
		int textLength = text.length();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(textLength);
		for (int i = 0; i < textLength; i++) {
			char ch = text.charAt(i);
			// Try finding this character in the standard 7-bit GSM alphabet
			int idx = STANDARD_ALPHABET_STRING.indexOf(ch);
			if(idx != -1) {
				// We've found the character, so write it.
				baos.write(idx);
				continue;
			}
			
			// Try finding this character in the extended 7-bit GSM alphabet
			idx = EXTENDED_ALPHABET_STRING.indexOf(ch);
			if(idx != -1) {
				baos.write(ESCAPE_SHIFT);
				baos.write(EXTENDED_ALPHABET_BYTES[idx]);
				continue;
			}
			
			// If this is a greek character, we can map it to a similar Latin character
			if(REMAP_GREEK_CHARACTERS) {
				char greekChar = greek2gsm7bit(ch);
				if(greekChar != ' ') {
					baos.write(greekChar);
					continue;
				}
			}
			
			// We don't recognise this character, so just write a space!
			baos.write(' ');
		}
		return baos.toByteArray();
	}

	/**
	 * Splits text into the parts that can fit into each section of a GSM message.
	 * @param messageText The text we would like to split up.
	 * @param udhLength The length of the UDH.  Usually this method is called twice
	 *                   - first assuming no concat needed (so UDH length does not include it),
	 *                    and then again if concat is actually needed with the longer UDH length. 
	 * @return The message, split appropriately for this UDH length, making sure no escaped characters split over message boundaries.
	 */
	public static String[] splitText(String messageText, boolean isPorted)
	{
		// hope we don't actuaslly need to concatenate, so split with a UDH that has no concat block
		String[] messageParts = splitText(messageText, TpduUtils.getUDHSize(true, isPorted, false));
		// re-split the message with a header that includes concat info, if we do actually need it
		if(messageParts.length > 1)
			messageParts = GsmAlphabet.splitText(messageText, TpduUtils.getUDHSize(true, isPorted, true));
		return messageParts;
	}
	
	/**
	 * Splits text into the parts that can fit into each section of a GSM message.
	 * @param text The text we would like to split up.
	 * @param udhLength The length of the UDH.  Usually this method is called twice (eg. by {@link #splitText(String, boolean)})
	 *                   - first assuming no concat needed (so UDH length does not include it),
	 *                    and then again if concat is actually needed with the longer UDH length. 
	 * @return The message, split appropriately for this UDH length, making sure no escaped characters split over message boundaries.  Splitting an empty string returns one empty part.
	 */
	static String[] splitText(String text, int udhLength) {
		if(text.length() == 0) return new String[]{""};
		
		ArrayList<String> strings = new ArrayList<String>();
		
		/** The number of bits at the start of this text that will be left blank. */
		int skipBits = calculateBitSkip(udhLength);
		/** Maximum length of the ecoded text, in bits. */
		int maxLength = (TpduUtils.MAX_PDU_SIZE - udhLength) * 8;
		/** Bits we've used so far. */
		int bitsUsed = skipBits;
		
		StringBuilder bob = new StringBuilder();
		
		// Iterate over every character in the string, calculating its character
		// code and making sure there is enough space to add it to the current string.
		// If there is not enough space, we must shunt it into the next string.
		for (int charIndex = 0; charIndex < text.length(); charIndex++) {
			char ch = text.charAt(charIndex);
			
			int characterSize = GsmAlphabet.getCharSize(ch);
			
			// Check we have enough space 
			if(bitsUsed + characterSize > maxLength) {
				strings.add(bob.toString());
				bob.delete(0, Integer.MAX_VALUE);
				bitsUsed = skipBits;
			}
			
			bob.append(ch);
			bitsUsed += characterSize;
		}
		if(bob.length() > 0) strings.add(bob.toString());
		
		return strings.toArray(new String[strings.size()]);
	}
	
	/**
	 * Gets the length, in bits, of a piece of text when encoded with this alphabet.
	 * @param text
	 * @return
	 */
	static int getEncodedLength(String text) {
		int length = 0;
		for(char c : text.toCharArray()) length += getCharSize(c);
		return length;
	}

	/**
	 * Gets the size, in bits, that this character will occupy when encoded in the GSM alphabet.
	 * @param ch
	 * @return
	 */
	private static int getCharSize(char ch) {
		// Try finding this character in the standard 7-bit GSM alphabet
		int idx = STANDARD_ALPHABET_STRING.indexOf(ch);
		if(idx != -1) {
			// Standard characters are a single septet
			return 7;
		}
		
		// Try finding this character in the extended 7-bit GSM alphabet
		idx = EXTENDED_ALPHABET_STRING.indexOf(ch);
		if(idx != -1) {
			// Extended characters take up 2 septets.
			return 14;
		}
		
		// Unknown characters are encoded as a space character, which is found in the standard alphabet
		return 7;
	}

	/**
	 * Encodes the supplied text with the 7-bit GSM character set, and then packs this data into
	 * an octet stream.
	 * @param text
	 * @param udhLength The length in octets of this message's UDH, including the UDH's length octet.
	 * @return
	 */
	public static byte[] encode(String text, int udhLength) {
		if(text.length() == 0) return new byte[0];
		int skipBits = calculateBitSkip(udhLength);
		byte[] septets = stringToBytes(text);
		return septetStream2octetStream(septets, skipBits);
	}
	
	/**
	 * Convert a list of septet values into an octet stream, with a number of empty bits at the start.
	 * @param septets
	 * @param skipBits
	 * @return
	 */
	static byte[] septetStream2octetStream(byte[] septets, int skipBits) {
		int octetLength = (int) Math.ceil(((septets.length * 7) + skipBits) / 8.0);
		byte[] octets = new byte[octetLength];
		
		for (int i = 0; i < septets.length; i++) {
			for (int j = 0; j < 7; j++) {
				if ((septets[i] & (1 << j)) != 0) {
					int bitIndex = (i * 7) + j + skipBits;
					octets[bitIndex >>> 3] |= 1 << (bitIndex & 7);
				}
			}
		}
		
		return octets;
		
	}
	
	/**
	 * Calculates the number of empty bits to prefix encoded 7-bit GSM text
	 * The position of the MS data actually depends on the length of the UDH, for some reason.
	 * This number should be between 0 and 6.
	 * TODO rename this fill bits
	 * @param udhLength Length in octets of the User-Data-Header
	 * @return
	 */
	public static int calculateBitSkip(int udhLength) {
		int skip = ((udhLength << 3) % 7);
		if(skip != 0) {
			skip = 7 - skip;
		}
		return skip;
	}
}