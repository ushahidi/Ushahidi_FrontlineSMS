/**
 * 
 */
package org.smslib.sms;

import org.smslib.IncomingBinarySmsTestData;
import org.smslib.IncomingTextSmsTestData;
import org.smslib.sms.content.BinarySmsMessageContent;
import org.smslib.sms.content.Gsm7bitTextSmsMessageContent;
import org.smslib.sms.content.Ucs2TextSmsMessageContent;

import net.frontlinesms.junit.BaseTestCase;

/**
 * Tests for {@link SmsDeliverPdu}.
 * @author Alex
 */
public class SmsDeliverPduTest extends BaseTestCase {
//> STATIC CONSTANTS
	/** Bad PDUs that should not decode correctly. */
	private static final String[] BAD_PDUS = {
		"30000101343437373636353534343435000101333337373838363635353232000000000000000000001248656C6C6F2066726F6D20534D505053696D",
		"000000000000000000000000000000000548656c6c6f",
		"0000000000000000000000000000000000",
		"0000000000000000000000000000000049626c616b646a686673646b6668736b6c64666a6864736b666a6768646b666a67686466676b6864666b6a646b6a6768646b6a6768646b6a6768646b666a676864666b6a676864666b67",
	};
	
//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> ACCESSORS
	
//> TEST METHODS
	/** Test {@link SmsDeliverPdu#getFromHex(String)} against the PDUs in {@link #BAD_PDUS}. */
	public void testBadPduDecoding() {
		for(String hexPdu : BAD_PDUS) {
			try {
				log.trace("Attempting to decode bad PDU: " + hexPdu);
				@SuppressWarnings("unused")
				SmsDeliverPdu pdu = SmsDeliverPdu.getFromHex(hexPdu);
				fail("Decoding should have thrown a " + PduDecodeException.class.getSimpleName() + " for pdu: [" + hexPdu + "]");
			} catch (PduDecodeException ex) {
				/* this exception is expected */
				log.trace("Caught expected exception decoding bad pdu.", ex);
			}
			
		}
	}
	
	/**
	 * Test messages in {@link IncomingBinarySmsTestData#BINARY_MESSAGES}
	 * @throws Exception 
	 */
	public void testSinglepartBinaryMessages() throws Exception {
		for(IncomingBinarySmsTestData testData : IncomingBinarySmsTestData.BINARY_MESSAGES) {
			SmsDeliverPdu pdu = SmsDeliverPdu.getFromHex(testData.getMessagePdu());
			SmsMessageContent messageContent = pdu.getUserData().getMessage();
			assertEquals("Message content was not the expected type.", BinarySmsMessageContent.class, messageContent.getClass());
			BinarySmsMessageContent binaryContent = (BinarySmsMessageContent) messageContent;
			assertEquals("Message content decoded incorrectly.", testData.getMessageBinary(), binaryContent.getContent());
		}
	}

	/**
	 * Test messages in {@link IncomingTextSmsTestData#GSM7BIT_MESSAGES}
	 * @throws Exception 
	 */
	public void testSinglepartGsm7bitMessages() throws Exception {
		for(IncomingTextSmsTestData testData : IncomingTextSmsTestData.GSM7BIT_MESSAGES) {
			SmsDeliverPdu pdu = SmsDeliverPdu.getFromHex(testData.getMessagePdu());
			SmsMessageContent messageContent = pdu.getUserData().getMessage();
			assertEquals("Message content was not the expected type.", Gsm7bitTextSmsMessageContent.class, messageContent.getClass());
			Gsm7bitTextSmsMessageContent textContent = (Gsm7bitTextSmsMessageContent) messageContent;
			assertEquals("Message content decoded incorrectly.", testData.getMessageText(), textContent.getContent());
		}
	}

	/**
	 * Test messages in {@link IncomingTextSmsTestData#MULTIPART_GSM7BIT_MESSAGES}
	 * @throws Exception 
	 */
	public void testMultipartGsm7bitMessages() throws Exception {
		for(IncomingTextSmsTestData testData : IncomingTextSmsTestData.MULTIPART_GSM7BIT_MESSAGES) {
			String totalMessageText = "";
			for(String messagePartPdu : testData.getMessagePdus()) {
				SmsDeliverPdu pdu = SmsDeliverPdu.getFromHex(messagePartPdu);
				SmsMessageContent messageContent = pdu.getUserData().getMessage();
				assertEquals("Message content was not the expected type.", Gsm7bitTextSmsMessageContent.class, messageContent.getClass());
				Gsm7bitTextSmsMessageContent textContent = (Gsm7bitTextSmsMessageContent) messageContent;
				totalMessageText += textContent.getContent();
			}
			assertEquals("Message content decoded incorrectly.", testData.getMessageText(), totalMessageText);
		}
	}

	/**
	 * Test messages in {@link IncomingTextSmsTestData#UCS2_MESSAGES}
	 * @throws Exception 
	 */
	public void testSinglepartUcs2Messages() throws Exception {
		for(IncomingTextSmsTestData testData : IncomingTextSmsTestData.UCS2_MESSAGES) {
			SmsDeliverPdu pdu = SmsDeliverPdu.getFromHex(testData.getMessagePdu());
			SmsMessageContent messageContent = pdu.getUserData().getMessage();
			assertEquals("Message content was not the expected type.", Ucs2TextSmsMessageContent.class, messageContent.getClass());
			Ucs2TextSmsMessageContent textContent = (Ucs2TextSmsMessageContent) messageContent;
			assertEquals("Message content decoded incorrectly.", testData.getMessageText(), textContent.getContent());
		}
	}

	/**
	 * Test messages in {@link IncomingTextSmsTestData#MULTIPART_UCS2_MESSAGES}
	 * @throws Exception 
	 */
	public void testMultipartUcs2Messages() throws Exception {
		for(IncomingTextSmsTestData testData : IncomingTextSmsTestData.MULTIPART_GSM7BIT_MESSAGES) {
			String totalMessageText = "";
			for(String messagePartPdu : testData.getMessagePdus()) {
				SmsDeliverPdu pdu = SmsDeliverPdu.getFromHex(messagePartPdu);
				SmsMessageContent messageContent = pdu.getUserData().getMessage();
				assertEquals("Message content was not the expected type.", Gsm7bitTextSmsMessageContent.class, messageContent.getClass());
				Gsm7bitTextSmsMessageContent textContent = (Gsm7bitTextSmsMessageContent) messageContent;
				totalMessageText += textContent.getContent();
			}
			assertEquals("Message content decoded incorrectly.", testData.getMessageText(), totalMessageText);
		}
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
