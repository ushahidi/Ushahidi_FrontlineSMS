/**
 * 
 */
package net.frontlinesms.email;

import javax.mail.Message;

/**
 * Class that checks whether an email is from an acceptable source.
 * @author Alex
 */
public interface EmailFilter {
	/**
	 * Check whether the received email address is from an acceptable source, or if it
	 * likely to be spam.  The {@link Message} object is supplied here so that emails
	 * can be rejected e.g. if the From address is acceptable, but the ReplyTo is not.
	 * @param message An email address.
	 * @return <code>true</code> if the message is from an acceptable source, or false if it should be rejected.
	 */
	public boolean accept(Message message);
}
