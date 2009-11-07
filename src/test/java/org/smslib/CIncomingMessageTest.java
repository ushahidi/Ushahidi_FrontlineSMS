/**
 * 
 */
package org.smslib;

import org.smslib.util.TpduUtils;

import net.frontlinesms.junit.BaseTestCase;

/**
 * Tests messages created using {@link TpduUtils} against {@link CIncomingMessage}.
 * 
 * @author Alex
 *
 */
public class CIncomingMessageTest extends BaseTestCase {
	
	/**
	 * Test decoding of 7bit GSM messages with intentionally bad content.
	 * @throws Throwable
	 */
	public void testbad7bitGsmMessages() throws Throwable {
		String goodPdu = IncomingTextSmsTestData.GSM7BIT_MESSAGES[0].getMessagePdu();
		String pdu = goodPdu.substring(0, goodPdu.length()-2);
		try {
			new CIncomingMessage(pdu, 0, "");
			fail("Expected MessageDecodeException.");
		} catch(MessageDecodeException ex) {}
	}
	
	/**
	 * Test the list of GSM 7-bit encoded messages in {@link IncomingTextSmsTestData#GSM7BIT_MESSAGES}.
	 * @throws Throwable
	 */
	public void test7bitGsmMessages() throws Throwable {
		for (IncomingTextSmsTestData testData : IncomingTextSmsTestData.GSM7BIT_MESSAGES) {
			String pdu = testData.getMessagePdu();
			String expectedText = testData.getMessageText();
			log.info("Testing message " + pdu + "'"+expectedText+"'");
			CIncomingMessage message = new CIncomingMessage(pdu, 0, "");
			assertEquals("GSM 7-bit Message not decoded by old implementation.", expectedText, message.getText());
		}
	}
	
	/**
	 * Test decoding of {@link IncomingTextSmsTestData#MULTIPART_GSM7BIT_MESSAGES}.
	 * @throws Throwable
	 */
	public void testMultipart7bitGsmMessages() throws Throwable {
		for (IncomingTextSmsTestData testData : IncomingTextSmsTestData.MULTIPART_GSM7BIT_MESSAGES) {
			String concatenatedMessageText = "";
			for(String pdu : testData.getMessagePdus()) {
				CIncomingMessage message = new CIncomingMessage(pdu, 0, "");
				concatenatedMessageText += message.getText();
			}
			assertEquals("Multipart message text was not constructed correctly.", testData.getMessageText(), concatenatedMessageText);
		}
	}
	
	/**
	 * Tests unicode messages on the new implementation.
	 * @throws Throwable 
	 */
	public void testUnicodeMessages() throws Throwable {
		for (int i = 0; i < IncomingTextSmsTestData.UCS2_MESSAGES.length; i++) {
			IncomingTextSmsTestData testData = IncomingTextSmsTestData.UCS2_MESSAGES[i];
			String pdu = testData.getMessagePdu();
			String expectedText = testData.getMessageText();
			CIncomingMessage newImplementation = new CIncomingMessage(pdu, 0, null);
			assertEquals("Simple UCS-2 Message not decoded by new implementation.", expectedText, newImplementation.getText());
		}
	}
	
	/**
	 * Test decoding of {@link IncomingTextSmsTestData#MULTIPART_GSM7BIT_MESSAGES}.
	 * @throws Throwable
	 */
	public void testMultipartUcs2Messages() throws Throwable {
		for (IncomingTextSmsTestData testData : IncomingTextSmsTestData.MULTIPART_UCS2_MESSAGES) {
			String concatenatedMessageText = "";
			for(String pdu : testData.getMessagePdus()) {
				CIncomingMessage message = new CIncomingMessage(pdu, 0, "");
				concatenatedMessageText += message.getText();
			}
			assertEquals("Multipart message text was not constructed correctly.", testData.getMessageText(), concatenatedMessageText);
		}
	}
	
	/**
	 * Test some unicode messages that have been slightly mangled.
	 * @throws Throwable
	 */
	public void testBadUnicodeMessages() throws Throwable {
		String messagePdu = IncomingTextSmsTestData.UCS2_MESSAGES[0].getMessagePdu();
		String badHexLength = messagePdu.substring(0, messagePdu.length()-1);
		try {
			new CIncomingMessage(badHexLength, 0, null);
			fail("Expected MessageDecodeException");
		} catch(MessageDecodeException ex) {}

		String badStart2 = messagePdu.substring(2);
		try {
			new CIncomingMessage(badStart2, 0, null);
			fail("Expected MessageDecodeException");
		} catch(MessageDecodeException ex) {}
	}
	
	/**
	 * Tests decoding of messages in {@link IncomingBinarySmsTestData#BINARY_MESSAGES}.
	 * @throws Throwable
	 */
	public void test8bitMessages() throws Throwable {
		for(IncomingBinarySmsTestData testData : IncomingBinarySmsTestData.BINARY_MESSAGES) {
			String pdu = testData.getMessagePdu();
			byte[] expectedBinary = testData.getMessageBinary();
			CIncomingMessage message = new CIncomingMessage(pdu, 0, "");
			assertEquals("Binary message not decoded by new implementation.", expectedBinary, message.getBinary());
		}
	}
}