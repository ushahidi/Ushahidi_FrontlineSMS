/**
 * 
 */
package net.frontlinesms.email.pop;

import javax.mail.Message;

/**
 * Class that processes messages received from a pop email account.
 * @author Alex
 */
public interface PopMessageProcessor {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> ACCESSORS

//> INSTANCE METHODS
	/**
	 * Process an incoming email message 
	 * @param message the message to process
	 */
	public void processPopMessage(Message message);

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
