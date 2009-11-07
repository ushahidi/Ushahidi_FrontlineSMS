package org.smslib.sms.content;

import org.smslib.sms.SmsMessageContent;

/**
 * {@link SmsMessageContent} containing a text message.
 * @author Alex
 */
public interface TextSmsMessageContent extends SmsMessageContent {

//> ACCESSORS
	/** @return the text content of this message */
	public String getContent();

}