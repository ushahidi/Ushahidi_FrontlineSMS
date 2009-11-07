/**
 * 
 */
package org.smslib;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * The current version of {@link COutgoingMessage} works a treat.  This class will
 * generate some test data from it in order to make sure that future versions of it
 * work equally well.
 * 
 * @author Alex
 */
public class COutgoingMessageTestGenerator {
	//> Message constants.  These are mostly for convenience.
	private static final String[] TEXTS = {
		"Hello, here is a short test message.",
		"Here is an sms text message that contains precisely one hundred and sixty characters, and should therefore fit (un)comfortably inside a single SMS text message.",
		//,
	};
	
	private static final short[] PORTS = {
		0, 100, 16000
	};
	
	private static final String[] MSISDNS = {
		"07890246735",
		"+447890123456"
	};
	
	public static final int[] MP_REF_NO = {0, 55, 255};
	/**
	 * SMS Lib originally only supported international SMSC numbers, so that is what we provide here.
	 */
	public static final String[] SMSC_NUMBER = {"", "+441234567890"};

	public static final File TEST_DIRECTORY = new File("src/test/resources/org/smslib/");

	private static final boolean TEST_7_BIT = true;
	private static final boolean TEST_8_BIT = true;
	
	public static void main(String[] args) throws IOException {
		int i = 0;

		if(TEST_7_BIT) {
			// 7 bit messages
			for(String messageText : TEXTS) {
				for(short sourcePort : PORTS) {
					for(short destinationPort : PORTS) {
						for(String recipient : MSISDNS) {
							COutgoingMessage outgoingMessage = new COutgoingMessage(recipient, messageText);
							outgoingMessage.setSourcePort(sourcePort);
							outgoingMessage.setDestinationPort(destinationPort);
							for(String smscNumber : SMSC_NUMBER) {
								for(int mpRefNo : MP_REF_NO) {
									TestMessage mess = new TestMessage(outgoingMessage, smscNumber, mpRefNo);
									mess.saveToFile(getTestFile(++i));
								}
							}
						}
					}
				}
			}
		}
		
		if(TEST_8_BIT) {
			// 8 bit messages
			Random random = new Random();
			for (int j = 0; j < 32; j++) {
				byte[] bytes = new byte[Math.abs(random.nextInt(400))];
				random.nextBytes(bytes);
				for(short sourcePort : PORTS) {
					for(short destinationPort : PORTS) {
						for(String recipient : MSISDNS) {
							COutgoingMessage outgoingMessage = new COutgoingMessage(recipient, bytes);
							outgoingMessage.setSourcePort(sourcePort);
							outgoingMessage.setDestinationPort(destinationPort);
							for(String smscNumber : SMSC_NUMBER) {
								for(int mpRefNo : MP_REF_NO) {
									TestMessage mess = new TestMessage(outgoingMessage, smscNumber, mpRefNo);
									mess.saveToFile(getTestFile(++i));
								}
							}
						}
					}
				}
			}
		}
		
		System.out.println("Generated test files succesfully at path: " + TEST_DIRECTORY.getAbsolutePath());
	}
	
	private static File getTestFile(int testNumber) {
		return new File(TEST_DIRECTORY, COutgoingMessageTest.class.getSimpleName() + "." + getTestNumberAsString(testNumber) + ".testmessage");
	}
	
	private static final String getTestNumberAsString(int testNumber) {
		final int GUMBO = 100000;
		if(testNumber > GUMBO/10) throw new RuntimeException("Can't create a safe number as big as " + testNumber);
		return Integer.toString(GUMBO + testNumber).substring(1);
	}

}
