/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.domain.SmsModemSettings;
import net.frontlinesms.data.repository.SmsModemSettingsDao;

/**
 * Hibernate implementation of {@link SmsModemSettingsDao}
 * @author Alex
 */
public class HibernateSmsModemSettingsDao extends BaseHibernateDao<SmsModemSettings> implements SmsModemSettingsDao {
	/** Create instance of this class */
	public HibernateSmsModemSettingsDao() {
		super(SmsModemSettings.class);
	}

	/** @see SmsModemSettingsDao#getSmsModemSettings(String) */
	public SmsModemSettings getSmsModemSettings(String serial) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(SmsModemSettings.FIELD_SERIAL, serial));
		return super.getUnique(criteria);
	}

	/** @see SmsModemSettingsDao#saveSmsModemSettings(SmsModemSettings) */
	public void saveSmsModemSettings(SmsModemSettings settings) {
		super.saveWithoutDuplicateHandling(settings);
	}

	/** @see SmsModemSettingsDao#updateSmsModemSettings(SmsModemSettings) */
	public void updateSmsModemSettings(SmsModemSettings settings) {
		super.updateWithoutDuplicateHandling(settings);
	}

}
