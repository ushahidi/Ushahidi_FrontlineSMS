/**
 * 
 */
package net.frontlinesms.listener;

import net.frontlinesms.data.domain.Message;

/**
 * Listener triggered when new {@link Message} objects have been saved for incoming messages.
 * @author Alex
 */
public interface IncomingMessageListener {
	public void incomingMessageEvent(Message message);
}
