package net.frontlinesms.junit;

import java.util.Date;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

/**
 * Extension of basic junit {@link TestCase} to add extra functionality, such as deep array comparison.
 * @author Alex
 */
public abstract class BaseTestCase extends TestCase {
	
	protected void assertEquals(String message, byte[] expected, byte[] actual) {
		assertEquals(message + " (different lengths)", expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			assertEquals(message + "(error found at position " + i + ")", expected[i], actual[i]);
		}
	}

	protected void assertEquals(String message, Object[] expected, Object[] actual) {
		assertEquals(message + " (different lengths)", expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			assertEquals(message + "(error found at position " + i + ")", expected[i], actual[i]);
		}
	}
	
	protected void myAssertEquals(String message, String expected, String actual) {
		try {
			assertEquals(message, expected, actual);
		} catch(AssertionFailedError f) {
			int minLen = Math.min(expected.length(), actual.length());
			for(int i=0; i<minLen; ++i) {
				if(expected.charAt(i) != actual.charAt(i)) throw new ComparisonFailure("Strings differ from character " + i, expected, actual);
			}
			throw f;
		}
	}
	
	protected void assertEquals(String message, Date expected, Date actual) {
		assertEquals(message, expected.getTime(), actual.getTime());	
	}
	
	protected void assertEquals(Date expected, Date actual) {
		assertEquals(expected.getTime(), actual.getTime());
	}
}