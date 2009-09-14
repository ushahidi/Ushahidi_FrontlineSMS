/**
 * 
 */
package net.frontlinesms.smsdevice;

import java.util.LinkedHashMap;

import net.frontlinesms.data.domain.SmsInternetServiceSettings;

/**
 * Service allowing sending and/or receiving of SMS messages over an internet connection.
 * @author Alex
 */
public interface SmsInternetService extends SmsDevice {
	/**
	 * Gets an identifier for this instance of {@link SmsInternetService}.  Usually this
	 * will be the username used to login with the provider, or a similar identifer on
	 * the service.
	 */
	public String getIdentifier();

	/**
	 * Gets the settings attached to this {@link SmsInternetService} instance.
	 * @return
	 */
	public SmsInternetServiceSettings getSettings();

	/**
	 * Initialize the service using the supplied properties.
	 * TODO If this service is currently running, we should handle that, possibly by restarting it,
	 * or throwing an exception to notify the user.
	 * @param newProperties
	 */
	public void init(SmsInternetServiceSettings settings);
	
	/**
	 * Checks if the service is currently connected.
	 * TODO could rename this isLive().
	 * @return
	 */
	public boolean isConnected();
	
	/** Starts this service. */
	public void startThisThing(); // FIXME rename method
	
	/** Re-connects this service. */
	public void restartThisThing(); // FIXME rename this method
	
	/** Stop this service from running */
	public void stopThisThing(); // FIXME rename this method
	
	/** Check if this service is encrypted using SSL. */
	public boolean isEncrypted();
	
	/** Gets the MSISDN that numbers sent from this service will appear to be from. */
	public String getMsisdn();
	
	/**
	 * Get the properties structure for this class.
	 * TODO should probably be called getDefaultProperties()...
	 */
	public LinkedHashMap<String, Object> getPropertiesStructure();
}
