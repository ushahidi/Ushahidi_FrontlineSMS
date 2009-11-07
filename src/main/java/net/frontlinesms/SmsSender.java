/**
 * 
 */
package net.frontlinesms;

import net.frontlinesms.data.domain.Message;

/**
 * Interface for SMS senders to implement.
 * @author Alex
 */
public interface SmsSender {
	/** Send a {@link Message}. */
	public void sendMessage(Message m);
}
