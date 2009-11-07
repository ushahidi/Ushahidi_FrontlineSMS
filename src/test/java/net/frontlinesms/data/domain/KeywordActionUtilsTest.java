/**
 * 
 */
package net.frontlinesms.data.domain;

import net.frontlinesms.data.domain.KeywordAction.KeywordUtils;
import net.frontlinesms.junit.BaseTestCase;

/**
 * @author Alex
 *
 */
public class KeywordActionUtilsTest extends BaseTestCase {
	
//> TEST CONSTANTS
	
	/** A keyword action for the keyword "SHOUT" */
	private static final KeywordAction KEYWORD_ACTION_SHOUT = createAction("SHOUT");
	
	/** An example message format which includes all the different substitutable values. */
	private static final String COMPREHENSIVE_MESSAGE_FORMAT = "You received a message from ${sender_name} (phone number ${sender_number}).  She said:\n${message_content}\nFor your records, the SMS message reference was: ${sms_id}.";
	
	/** Test data for checking if keyword is correctly stripped from a string. */
	private String [][] REMOVE_KEYWORD = {
		/* keyword, messageText, messageTextWithoutKeyword */
			{"", "", ""},
			{"simple", "simple", ""},
			{"simple", "sIMPle", ""},
			{"simple", "simple ", ""},
			{"simple", "simPlE ", ""},
			{"simple", "simple message containing a keyword called simple", "message containing a keyword called simple"},
			{"simple", "sImple message containing a keyword called simple", "message containing a keyword called simple"},
			{"simple", "sAMPle message containing a keyword called simple", "sAMPle message containing a keyword called simple"},
			{"simple", "saMple message containing a keyword called simple", "saMple message containing a keyword called simple"},
			{"two part", "two part", ""},
			{"two part", "tWo pARt", ""},
			{"two part", "two part ", ""},
			{"two part", "two paRt ", ""},
			{"two part", "two partmessage", "two partmessage"},
			{"two part", "two parTMesSage", "two parTMesSage"},
			{"two part", "two part message", "message"},
			{"two part", "Two part meSSage", "meSSage"},
			{"two part", "two part  message", " message"},
			{"two part", "two PARt  message", " message"},
			{"simple", "simple\rwith line break", "with line break"},
			{"simple", "SIMple\rwith line break", "with line break"},
			{"simple", "simple\nwith newline", "with newline"},
			{"simple", "sIMPLE\nwith newline", "with newline"},
			{"simple", "simple \nwith space and newline.  perhaps we should ditch both space AND newline?", "\nwith space and newline.  perhaps we should ditch both space AND newline?"},
			{"simple", "siMplE \nwith space and newline.  perhaps we should ditch both space AND newline?", "\nwith space and newline.  perhaps we should ditch both space AND newline?"},
			{"simple", "simple\r\nwith CR and newline.  perhaps we should ditch both CR AND newline?", "\nwith CR and newline.  perhaps we should ditch both CR AND newline?"},
			{"simple", "sIMPLe\r\nwith CR and newline.  perhaps we should ditch both CR AND newline?", "\nwith CR and newline.  perhaps we should ditch both CR AND newline?"},
	};

	/** A person's name used in tests */
	private static final String TEST_NAME = "Billy Jean";
	/** An MSISDN used in tests */
	private static final String TEST_MSISDN = "+447890123456";
	
	/** Test data for checking that data formatting is applied correctly */
	private static final Object[][] FORMAT_TEXT = {
		/* formattedText, unformattedText, urlEncode, action, senderMsisdn, senderDisplayName, incomingMessageText, refNo */
		{"Here is message format that never changes.", "Here is message format that never changes.", true, createAction(""), TEST_MSISDN, TEST_NAME, "Here is the message content.", null},
		{"Here is message format that never changes.", "Here is message format that never changes.", false, createAction(""), TEST_MSISDN, TEST_NAME, "Here is the message content.", 69},
		{"Here is message format that never changes.", "Here is message format that never changes.", true, createAction(""), TEST_MSISDN, TEST_NAME, "Here is the message content.", null},
		{"Here is message format that never changes.", "Here is message format that never changes.", false, createAction(""), TEST_MSISDN, TEST_NAME, "Here is the message content.", 69},
		{"You received a message from Billy Jean (phone number +447890123456).  She said:\nHello there, billy jean here!  hope u r good!\nFor your records, the SMS message reference was: 69.",
				COMPREHENSIVE_MESSAGE_FORMAT, false, KEYWORD_ACTION_SHOUT, TEST_MSISDN, TEST_NAME, "SHOUT Hello there, billy jean here!  hope u r good!", 69},
		{"You received a message from Billy Jean (phone number +447890123456).  She said:\nHello there, billy jean here!  hope u r good!\nFor your records, the SMS message reference was: 71.",
				COMPREHENSIVE_MESSAGE_FORMAT, false, KEYWORD_ACTION_SHOUT, TEST_MSISDN, TEST_NAME, "SHOUT Hello there, billy jean here!  hope u r good!", 71},
		{"http://localhost/process/Billy+Jean/%2B447890123456/DOIT/Hello+there%2C+here%27s+the+message+content./69/", "http://localhost/process/${sender_name}/${sender_number}/${keyword}/${message_content}/${sms_id}/", true, createAction("doit"), TEST_MSISDN, TEST_NAME, "DOIT Hello there, here's the message content.", 69},
	};
	
	/**
	 * Test method {@link KeywordAction.KeywordUtils#removeKeyword(String, String)}.
	 */
	public void testRemoveKeyword() {
		for(String[] testData : REMOVE_KEYWORD) {
			String keyword = testData[0];
			String fullMessageText = testData[1];
			String removed = KeywordAction.KeywordUtils.removeKeyword(fullMessageText, keyword);
			assertEquals("Keyword '"+keyword+"' was stripped incorrectly from '"+fullMessageText+"'.", testData[2], removed);
		}
	}
	
	/**
	 * Test method {@link KeywordAction.KeywordUtils#extractKeyword(String, String)}.
	 */
	public void testExtractKeyword() {
		for(String[] testData : REMOVE_KEYWORD) {
			String keyword = testData[0];
			String fullMessageText = testData[1];
			String extractedKeyword = KeywordAction.KeywordUtils.extractKeyword(fullMessageText, keyword);
			if(extractedKeyword == null) {
				assertEquals("Keyword not removed, so message should be left intact.", testData[2], fullMessageText);
			} else {
				assertEquals("Message text does not actually start with the keyword '"+keyword+"'.", fullMessageText.substring(0, keyword.length()), extractedKeyword);
			}
		}
	}
	
	/**
	 * Test method {@link KeywordAction.KeywordUtils#formatText(String, boolean, KeywordAction, String, String, String, Integer)}.
	 */
	public void testFormatText() {
		for(Object[] testData : FORMAT_TEXT) {
			String expectedText = (String) testData[0];
			String unformattedText = (String) testData[1];
			boolean urlEncode = (Boolean) testData[2];
			KeywordAction action = (KeywordAction) testData[3];
			String senderMsisdn = (String) testData[4];
			String senderDisplayName = (String) testData[5];
			String incomingMessageText = (String) testData[6];
			Integer refNo = (Integer) testData[7];
			testFormatText(expectedText, unformattedText, urlEncode, action, senderMsisdn, senderDisplayName, incomingMessageText, refNo);
		}
	}

	/**
	 * Helper method for testing if supplied text is formatted in the expected way.
	 * @param expectedText
	 * @param unformattedText
	 * @param urlEncode
	 * @param action
	 * @param senderMsisdn
	 * @param senderDisplayName
	 * @param incomingMessageText
	 * @param refNo
	 */
	private void testFormatText(String expectedText, String unformattedText, boolean urlEncode, KeywordAction action, String senderMsisdn, String senderDisplayName, String incomingMessageText, Integer refNo) {
		String formattedText = KeywordUtils.formatText(unformattedText, urlEncode, action, senderMsisdn, senderDisplayName, incomingMessageText, refNo);
		assertEquals("Formatted text did not appear as expected.", expectedText, formattedText);
	}
	
//> STATIC HELPER METHODS
	/**
	 * Helper method for creating simple test {@link KeywordAction}s.
	 * @param keyword the keyword string to apply to the keyword
	 * @return a survey action
	 */
	private static KeywordAction createAction(String keyword) {
		Keyword kw = new Keyword(null, keyword, "a test keyword");
		return KeywordAction.createSurveyAction(kw, -1, -1);
	}
}
