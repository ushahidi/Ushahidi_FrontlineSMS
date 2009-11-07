/**
 * 
 */
package org.smslib.sms;

/**
 * The "Message" part of an SMS, also known as the MS.
 * @author Alex
 */
public interface SmsMessageContent extends PduComponent {
	/** @return the content of the message */
	public Object getContent();
}
