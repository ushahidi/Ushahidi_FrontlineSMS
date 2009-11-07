/**
 * 
 */
package net.frontlinesms.plugins.httptrigger;

/**
 * @author Alex
 *
 */
public interface HttpTriggerEventListener {
	/**
	 * Adds a new item to the log. 
	 * @param message message to log
	 * TODO this should actually use an enumerated list of different events
	 */
	public void log(String message);

	/**
	 * Sends an SMS to the requested number.
	 * @param toPhoneNumber The number to send the SMS to
	 * @param message The message to send in the SMS
	 */
	public void sendSms(String toPhoneNumber, String message);
}
