/**
 * 
 */
package net.frontlinesms.email.pop;

import java.util.Date;

/**
 * Class that processes messages received from a pop email account.
 * 
 * @author Alex
 */
public interface PopMessageProcessor {
	public void processPopMessage(String sender, Date messageSentDate, String subject, String messageText);
}
