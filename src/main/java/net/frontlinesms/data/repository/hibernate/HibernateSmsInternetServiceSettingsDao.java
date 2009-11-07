/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.Collection;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.SmsInternetServiceSettings;
import net.frontlinesms.data.repository.SmsInternetServiceSettingsDao;

/**
 * @author Alex
 *
 */
public class HibernateSmsInternetServiceSettingsDao extends BaseHibernateDao<SmsInternetServiceSettings> implements SmsInternetServiceSettingsDao {
	/** Create instance of this class */
	public HibernateSmsInternetServiceSettingsDao() {
		super(SmsInternetServiceSettings.class);
	}

	/** @see SmsInternetServiceSettingsDao#deleteSmsInternetServiceSettings(SmsInternetServiceSettings) */
	public void deleteSmsInternetServiceSettings(SmsInternetServiceSettings settings) {
		super.delete(settings);
	}

	/** @see SmsInternetServiceSettingsDao#getSmsInternetServiceAccounts() */
	public Collection<SmsInternetServiceSettings> getSmsInternetServiceAccounts() {
		return super.getAll();
	}

	/** @see SmsInternetServiceSettingsDao#saveSmsInternetServiceSettings(SmsInternetServiceSettings) */
	public void saveSmsInternetServiceSettings(SmsInternetServiceSettings settings) throws DuplicateKeyException {
		super.save(settings);
	}

	/** @see SmsInternetServiceSettingsDao#updateSmsInternetServiceSettings(SmsInternetServiceSettings) */
	public void updateSmsInternetServiceSettings(SmsInternetServiceSettings settings) {
		super.updateWithoutDuplicateHandling(settings);
	}
}
