/**
 * 
 */
package net.frontlinesms.data.repository;

import java.util.Collection;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.*;

/**
 * Data Access Object interface for {@link SmsInternetServiceSettings}.
 * @author Alex
 */
public interface SmsInternetServiceSettingsDao {
	/**
	 * Saves {@link SmsInternetServiceSettings} to the data source 
	 * @param settings settings to save
	 * @throws DuplicateKeyException
	 */
	public void saveSmsInternetServiceSettings(SmsInternetServiceSettings settings) throws DuplicateKeyException;

	/** @return all {@link SmsInternetServiceSettings} */
	public Collection<SmsInternetServiceSettings> getSmsInternetServiceAccounts();
	
	/**
	 * Deletes {@link SmsInternetServiceSettings} from the data source
	 * @param settings settings to delete
	 */
	public void deleteSmsInternetServiceSettings(SmsInternetServiceSettings settings);
}
