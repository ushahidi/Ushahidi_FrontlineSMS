/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.SmsInternetServiceSettings;
import net.frontlinesms.data.repository.SmsInternetServiceSettingsDao;

/**
 * In-memory implementation of {@link SmsInternetServiceSettingsDao}.
 * @author Alex
 */
public class InMemorySmsInternetServiceSettingsDao implements SmsInternetServiceSettingsDao {
	/** All settings saved in the data source */
	private final HashSet<SmsInternetServiceSettings> allSettings = new HashSet<SmsInternetServiceSettings>();
	
	/** @see net.frontlinesms.data.repository.SmsInternetServiceSettingsDao#getSmsInternetServiceAccounts() */
	public Collection<SmsInternetServiceSettings> getSmsInternetServiceAccounts() {
		return Collections.unmodifiableCollection(this.allSettings);
	}

	/** @see SmsInternetServiceSettingsDao#deleteSmsInternetServiceSettings(SmsInternetServiceSettings) */
	public void deleteSmsInternetServiceSettings(SmsInternetServiceSettings settings) {
		this.allSettings.remove(settings);
		
	}

	/** @see SmsInternetServiceSettingsDao#saveSmsInternetServiceSettings(SmsInternetServiceSettings) */
	public void saveSmsInternetServiceSettings(SmsInternetServiceSettings settings) throws DuplicateKeyException {
		this.allSettings.add(settings);
	}

	/** @see SmsInternetServiceSettingsDao#updateSmsInternetServiceSettings(SmsInternetServiceSettings) */
	public void updateSmsInternetServiceSettings(SmsInternetServiceSettings settings) {
		/* Nothing to do to update */
	}
}
