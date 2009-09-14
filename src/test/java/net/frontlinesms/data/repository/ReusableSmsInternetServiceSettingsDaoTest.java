/**
 * 
 */
package net.frontlinesms.data.repository;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.SmsInternetServiceSettings;
import net.frontlinesms.junit.ReusableTestCase;
import net.frontlinesms.smsdevice.ClickatellInternetService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Base test class for testing {@link SmsInternetServiceSettingsDao}
 * @author Alex
 */
public abstract class ReusableSmsInternetServiceSettingsDaoTest extends ReusableTestCase<SmsInternetServiceSettings> {
	/** Instance of this DAO implementation we are testing. */
	private SmsInternetServiceSettingsDao dao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(SmsInternetServiceSettingsDao dao) {
		this.dao = dao;
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.dao = null;
	}
	
	/**
	 * Test everything all at once!
	 * @throws DuplicateKeyException 
	 */
	public void test() throws DuplicateKeyException {
		assertEquals(0, dao.getSmsInternetServiceAccounts().size());
		
		ClickatellInternetService clickatell = new ClickatellInternetService();
		SmsInternetServiceSettings settings = new SmsInternetServiceSettings(clickatell);
		
		dao.saveSmsInternetServiceSettings(settings);

		assertEquals(1, dao.getSmsInternetServiceAccounts().size());
		
		dao.deleteSmsInternetServiceSettings(settings);
		
		assertEquals(0, dao.getSmsInternetServiceAccounts().size());
	}
}
