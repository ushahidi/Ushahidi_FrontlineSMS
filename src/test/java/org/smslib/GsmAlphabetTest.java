/**
 * 
 */
package org.smslib;

import java.util.HashSet;
import java.util.Random;

import junit.framework.ComparisonFailure;

import net.frontlinesms.hex.HexUtils;
import net.frontlinesms.junit.BaseTestCase;

/**
 * Test to make sure that the new implementation {@link GsmAlphabet} matches the old
 * implementation {@link ReferenceGsmAlphabet}.
 * @author Alex
 */
public class GsmAlphabetTest extends BaseTestCase {
	private Character[] gsm7bitCharacters;
	
	/**
	 * Tests the method {@link ReferenceGsmAlphabet#bytesToString(byte[])}.
	 */
	public void testBytesToString() {
		/** RNG, initialised to a fixed number. */
		Random randy = new Random(0);
		
		for(int i=0; i<512; ++i) {
			byte[] testBytes = new byte[randy.nextInt(420)];
			randy.nextBytes(testBytes);
			
			// make sure all bytes are 7-bit values
			for (int j = 0; j < testBytes.length; j++) {
				testBytes[j] = (byte)(testBytes[j] & 0x7F);
				if(testBytes[j] == 0x1B) {
					if(j == testBytes.length - 1) {
						// no room for extended char, so just chuck in a random @
						testBytes[j] = 0;
					} else {
						testBytes[j+1] = HexUtils.decode(ReferenceGsmAlphabet.extBytes[randy.nextInt(ReferenceGsmAlphabet.extBytes.length)])[1];
					}
				}
			}
			
			testBytesToString(testBytes);
		}
	}
	
	private void testBytesToString(byte[] bytes) {
		String cString = ReferenceGsmAlphabet.bytesToString(bytes);
		String myString = GsmAlphabet.bytesToString(bytes);
		try {
			myAssertEquals("Generated strings not equal.",
					myString,
					cString);
		} catch(ComparisonFailure c) {
			System.out.println("cString  : " + cString);
			System.out.println("myString : " + myString);
			System.out.println("bytes    : " + new String(bytes));
			int minLen = Math.min(cString.length(), myString.length());
			for (int i = 0; i < minLen; i++) {
				if(cString.charAt(i)!=myString.charAt(i)) {
					System.out.println("Naughty Character Found:");
					System.out.println("  c : " + cString.charAt(i) + "\t" + ((int)cString.charAt(i)));
					System.out.println("  my: " + myString.charAt(i) + "\t" + ((int)myString.charAt(i)));
					break;
				}
			}
			throw c;
		}
	}
	
	/**
	 * TODO implement test for {@link GsmAlphabet#encode(String, int)}
	 */
	public void testEncode() {
	}
	
	/**
	 * N.B. this test now fails because the reference implementation has been corrected.
	 */
	public void testPduToText() {
		/** RNG, initialised to a fixed number. */
		Random randy = new Random(0);
		
		for(int i=0; i<512; ++i) {
			StringBuilder bob = new StringBuilder();
			for (int j=(randy.nextInt(400)&(~1)); j>0; --j) {
				bob.append(HexUtils.encode(randy.nextInt(), 1));
			}
			testPduToText(bob.toString());
		}
	}
	
	/**
	 * FIXME rename this test in line with renamed {@link GsmAlphabet#pduToText(String)} rename.
	 * @param pdu
	 */
	private void testPduToText(String pdu) {
		byte[] nonRefBytes = GsmAlphabet.pduToText(pdu);
		StringBuilder nonRefPdu = new StringBuilder(nonRefBytes.length);
		for(byte b : nonRefBytes) nonRefPdu.append((char)b);
		String referencePdu = ReferenceGsmAlphabet.pduToText(pdu);
		
		String nonRefPduString = nonRefPdu.toString();
		for (int i = 0; i < referencePdu.length(); i++) {
			assertEquals("Character " + i + "was different - expected '"+referencePdu+"' but was '"+nonRefPduString+"'", referencePdu.charAt(i), nonRefPdu.charAt(i));
		}
		// Cunningly ignore the first character - the new implementation is correct, but the old one
		// was very nearly correct!
		// TODO update this testPduToText method, and the reference implementation, to the correct implementation.
	}
	
	public void testStringToBytes() {
		/** RNG, initialised to a fixed number. */
		Random randy = new Random(0);
		
		for(int i=0; i<512; ++i) {
			testStringToBytes(generateGsmString(randy, randy.nextInt(250)));
		}
	}
	
	private void testStringToBytes(String text) {
		byte[] refBytes = new byte[400];
		int cBytesLength = ReferenceGsmAlphabet.stringToBytes(text, refBytes);
		byte[] finalRefBytes = new byte[cBytesLength];
		System.arraycopy(refBytes, 0, finalRefBytes, 0, cBytesLength);
		assertEquals("", finalRefBytes, GsmAlphabet.stringToBytes(text));
	}
	
	private String generateGsmString(Random randy, int length) {
		StringBuilder bob = new StringBuilder(length);
		for(int i=length; i>0; --i) bob.append(gsm7bitCharacters[randy.nextInt(gsm7bitCharacters.length)]);
		return bob.toString();
	}
	
	@Override
	protected void setUp() throws Exception {
		HashSet<Character> gsm7bitCharacters = new HashSet<Character>();
		for(char c : ReferenceGsmAlphabet.EXTENDED_ALPHABET) gsm7bitCharacters.add(c);
		for(char c : ReferenceGsmAlphabet.STANDARD_ALPHABET) gsm7bitCharacters.add(c);
		this.gsm7bitCharacters = gsm7bitCharacters.toArray(new Character[0]); 
	}
	
	public void testSplitText() {
		assertEquals("Empty text should be split into one part (unported).", 1, GsmAlphabet.splitText("", false).length);
		assertEquals("Empty text should be split into one part (ported).", 1, GsmAlphabet.splitText("", true).length);
	}
}
