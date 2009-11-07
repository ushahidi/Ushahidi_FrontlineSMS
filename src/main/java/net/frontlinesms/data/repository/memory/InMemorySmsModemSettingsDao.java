/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import java.util.HashMap;

import net.frontlinesms.data.domain.SmsModemSettings;
import net.frontlinesms.data.repository.SmsModemSettingsDao;

/**
 * In-memory implementation of {@link SmsModemSettingsDao}
 * @author Alex
 */
public class InMemorySmsModemSettingsDao implements SmsModemSettingsDao {
	/** All settings saved in the system */
	private HashMap<String, SmsModemSettings> settings = new HashMap<String, SmsModemSettings>();
	
	/** @see SmsModemSettingsDao#getSmsModemSettings(java.lang.String) */
	public SmsModemSettings getSmsModemSettings(String serial) {
		return this.settings.get(serial);
	}

	/** @see SmsModemSettingsDao#saveSmsModemSettings(SmsModemSettings) */
	public void saveSmsModemSettings(SmsModemSettings settings) {
		this.settings.put(settings.getSerial(), settings);
	}

	/** @see SmsModemSettingsDao#updateSmsModemSettings(SmsModemSettings) */
	public void updateSmsModemSettings(SmsModemSettings settings) {
		// do nothing for in-memory DAO
	}
}
