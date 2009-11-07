/**
 * 
 */
package net.frontlinesms.plugins.forms.request;

/**
 * A decoded incoming forms message.
 * @author Alex
 */
public abstract class FormsRequestDescription {
	
//> INSTANCE PROPERTIES
	/** Port that this contact should be sent forms on. */
	private Integer smsPort;
	
//> ACCESSOR METHODS
	/**
	 * @param smsPort the smsPort to set
	 */
	public void setSmsPort(int smsPort) {
		this.smsPort = smsPort;
	}
	/**
	 * @return the smsPort
	 */
	public Integer getSmsPort() {
		return smsPort;
	}
}
