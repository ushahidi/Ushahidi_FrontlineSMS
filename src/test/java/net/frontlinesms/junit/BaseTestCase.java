package net.frontlinesms.junit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.log4j.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

/**
 * Extension of basic junit {@link TestCase} to add extra functionality, such as deep array comparison.
 * @author Alex
 */
public abstract class BaseTestCase extends TestCase {
	
//> STATIC CONSTANTS
	/** The name of the directory to store temporary test files in */
	private static final File TEMPORARY_DATA_DIRECTORY = new File("test_temp");
	
//> INSTANCE PROPERTIES
	/** Logging object */
	protected final Logger log = Logger.getLogger(this.getClass());
	
//> COMMON SETUP / TEARDOWN METHODS
	/**
	 * Set up common test resources:
	 * <li>{@link #TEMPORARY_DATA_DIRECTORY}</li>
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TEMPORARY_DATA_DIRECTORY.mkdir();
	}
	
	/** Destroy common test resources created by {@link #setUp()}. */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		TEMPORARY_DATA_DIRECTORY.delete();
	}
	
//> EQUALS METHODS
	/**
	 * Compare the contents of 2 <code>byte[]</code>.
	 * @param expected The expexted array.
	 * @param actual The actual array found in the test.
	 */
	protected void assertEquals(byte[] expected, byte[] actual) {
		assertEqualsWithoutMessage();
	}
	
	/**
	 * Compare the contents of 2 <code>byte[]</code>.
	 * @param message The message to display if the two arrays are not equal in length and content.
	 * @param expected The expexted array.
	 * @param actual The actual array found in the test.
	 */
	protected void assertEquals(String message, byte[] expected, byte[] actual) {
		assertEquals(message + " (different lengths)", expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			assertEquals(message + "(error found at position " + i + ")", expected[i], actual[i]);
		}
	}

	/**
	 * Compare the contents of 2 <code>Object[]</code>.
	 * @param message The message to display if the two arrays are not equal in length and content.
	 * @param expected The expexted array.
	 * @param actual The actual array found in the test.
	 */
	protected void assertEquals(String message, Object[] expected, Object[] actual) {
		assertEquals(message + " (different lengths)", expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			assertEquals(message + "(error found at position " + i + ")", expected[i], actual[i]);
		}
	}
	
	/**
	 * Compares 2 {@link String}s and gives a detailed message if they are not equals.
	 * TODO please document this more clearly.
	 * @param message
	 * @param expected
	 * @param actual
	 */
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
	
	/**
	 * Calls {@link #assertEquals(long, long)} on the time in millis of each date object.
	 * TODO this may foolishly ignore timezones; investigate.
	 * @param message
	 * @param expected
	 * @param actual
	 */
	protected void assertEquals(String message, Date expected, Date actual) {
		assertEquals(message, expected.getTime(), actual.getTime());	
	}
	
	/**
	 * Compares the contents of two {@link InputStream}s.
	 * @param expected Stream of expected values
	 * @param actual Stream of actual values
	 */
	protected void assertEquals(InputStream expected, InputStream actual) {
		assertEqualsWithoutMessage();
	}
	
	/**
	 * Compares the contents of two {@link InputStream}s.
	 * @param message Message to display if the comparison fails
	 * @param expected Stream of expected values
	 * @param actual Stream of actual values
	 */
	protected void assertEquals(String message, InputStream expected, InputStream actual) {
		ByteArrayOutputStream expectedAsBAOS = new ByteArrayOutputStream();
		stream2stream(expected, expectedAsBAOS);

		ByteArrayOutputStream actualAsBAOS = new ByteArrayOutputStream();
		stream2stream(actual, actualAsBAOS);
		
		assertEquals(message, expectedAsBAOS.toByteArray(), actualAsBAOS.toByteArray());
	}
	
//> STATIC UTILITIES
	/**
	 * Creates a test output file in the temp test directory.
	 * @param fileName The name of the test output file.
	 * @return A temporary file in the temporary directory.
	 */
	protected File getOutputFile(String fileName) {
		return new File(TEMPORARY_DATA_DIRECTORY, fileName);
	}
	
	/**
	 * Reads the entire contents of an {@link OutputStream} and writes it to an {@link InputStream}.
	 * @param from
	 * @param to
	 */
	private static void stream2stream(InputStream from, OutputStream to) {
		int read;
		try {
			while((read = from.read()) != -1) {
				to.write(read);
			}
		} catch(IOException ex) { /* we've reached the end of the stream - time to return */ }
	}
	
	/**
	 * Method to call from assertEquals methods which have no message.  This is to enforce provision
	 * of a message, and prevent confusion when we have overridden {@link #assertEquals(Object, Object)}
	 */
	private static void assertEqualsWithoutMessage() {
		throw new IllegalStateException("assertEquals() should not be called without a message.");
	}
}