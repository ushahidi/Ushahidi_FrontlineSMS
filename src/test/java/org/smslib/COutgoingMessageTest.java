package org.smslib;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.smslib.sms.SmsMessageEncoding;

import net.frontlinesms.junit.BaseTestCase;

import junit.framework.TestCase;

/**
 * {@link TestCase} around {@link AgaOutgoingMessage} to check the data it generates follows the GSM spec.
 * @author Alex
 */
public class COutgoingMessageTest extends BaseTestCase {
	
	private void testGeneratedMessage(SmsMessageEncoding encoding) throws Exception {
		Map<String, TestMessage> messages = loadTestMessages();
		int count = 0;
		for(String filename : messages.keySet()) {
			System.out.println("Testing " + ++count);
			try {
				TestMessage expectedMessage = messages.get(filename);
				
				if(expectedMessage.getEncoding() == encoding) {
					COutgoingMessage actualMessage;
					String recipient = expectedMessage.getRecipient();
					String text = expectedMessage.getText();
					byte[] binaryContent = expectedMessage.getBinaryContent();
					if(text != null && binaryContent != null) throw new IllegalArgumentException("Both text and binary have value!");
					if(text != null) actualMessage = new COutgoingMessage(recipient, text);
					else if(binaryContent != null) actualMessage = new COutgoingMessage(recipient, binaryContent);
					else throw new IllegalArgumentException("Both text and binary are null!");
					actualMessage.setMessageEncoding(expectedMessage.getEncoding());
					actualMessage.setDestinationPort(expectedMessage.getDestinationPort());
					actualMessage.setSourcePort(expectedMessage.getSourcePort());
					
					assertEquals(expectedMessage, new TestMessage(actualMessage, expectedMessage.getSmscNumber(), expectedMessage.getMpRefNo()));
				}
			} catch(Throwable t) {
				throw new Exception("Unhandled exception processing test: " + filename, t);
			}
		}
	}
	
	/**
	 * Test the files generated with {@link COutgoingMessageTestDataGenerator}. 
	 * @throws Exception 
	 * @throws IOException 
	 */
	public void testGeneratedMessages_8bit() throws Exception {
		//testGeneratedMessage(SmsMessageEncoding.BINARY_8BIT);
	}
	
	/**
	 * Test the files generated with {@link COutgoingMessageTestDataGenerator}. 
	 * @throws Exception 
	 * @throws IOException 
	 */
	public void testGeneratedMessages_ucs2() throws Exception {
		//testGeneratedMessage(SmsMessageEncoding.UCS2);
	}
	
	/**
	 * Test the files generated with {@link COutgoingMessageTestDataGenerator}. 
	 * @throws Exception 
	 * @throws IOException 
	 */
	public void testGeneratedMessages_gsm7bit() throws Exception {
		//testGeneratedMessage(SmsMessageEncoding.GSM_7BIT);
	}
	
	private void assertEquals(TestMessage expectedMessage, TestMessage actualMessage) {
		if(expectedMessage.getBinaryContent() == null) assertNull("Incorrect binary content.", actualMessage.getBinaryContent());
		else assertEquals("Incorrect binary content.", expectedMessage.getBinaryContent(), actualMessage.getBinaryContent());
		assertEquals("Incorrect destination port.", expectedMessage.getDestinationPort(), actualMessage.getDestinationPort());
		assertEquals("Incorrect encoding.", expectedMessage.getEncoding(), actualMessage.getEncoding());
		assertEquals("Incorrect parts count.", expectedMessage.getParts(), actualMessage.getParts());
		assertEquals("Incorrect recipient.", expectedMessage.getRecipient(), actualMessage.getRecipient());
		assertEquals("Incorrect source port.", expectedMessage.getSourcePort(), actualMessage.getSourcePort());
		assertEquals("Incorrect text content.", expectedMessage.getText(), actualMessage.getText());
		assertEquals("Incorrect message type.", expectedMessage.getType(), actualMessage.getType());
		
		assertEquals("Different PDU content.  This is likely because you are using different concatenation to re-encode the PDUs.  Expected: <" + pdusAsString(expectedMessage.getPdus()) + "> but was: <" + pdusAsString(actualMessage.getPdus()) + ">", expectedMessage.getPdus(), actualMessage.getPdus());
	}

	/**
	 * Converts a list of PDUs to a comma-separated string to enable ease of output
	 * @param pdus
	 * @return a comma separated list of hex-encoded PDUs
	 */
	private String pdusAsString(String[] pdus) {
		if(pdus.length == 0) return "";
		String s = "";
		for(String pdu : pdus) s += "," + pdu;
		return s.substring(1);
	}

	private static Map<String, TestMessage> loadTestMessages() throws IOException {
		HashMap<String, TestMessage> messages = new HashMap<String, TestMessage>();
		for(File f : COutgoingMessageTestGenerator.TEST_DIRECTORY.listFiles(new FileFilter() { public boolean accept(File pathname) { return pathname.getName().endsWith(".testmessage"); } })) {
			messages.put(f.getName(), TestMessage.getFromFile(f));
		}
		
		if(messages.size() == 0) {
			// No test data is available.  We throw an exception here to warn the user that the test data is required.
			throw new IllegalStateException("There is no test data available for these tests.  Please run: " + COutgoingMessageTestGenerator.class.getName() + ".main() to generate data.");
		}
		
		return messages;
	}
}
