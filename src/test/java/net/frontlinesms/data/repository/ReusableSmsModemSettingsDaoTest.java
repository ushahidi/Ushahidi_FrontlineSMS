/**
 * 
 */
package net.frontlinesms.data.repository;

import net.frontlinesms.data.domain.SmsModemSettings;
import net.frontlinesms.junit.ReusableTestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Base test class for testing {@link SmsModemSettingsDao}
 * @author Alex
 */
public abstract class ReusableSmsModemSettingsDaoTest extends ReusableTestCase<SmsModemSettings> {
//> CONSTANTS
	private static final String SERIAL_ONE = "Serial ONE";
	
	private static final String SERIAL_TWO = "Serial TWO";
	
//> INSTANCE PROPERTIES
	/** Instance of this DAO implementation we are testing. */
	private SmsModemSettingsDao dao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(SmsModemSettingsDao dao) {
		this.dao = dao;
	}
	
	@Override
	public void tearDown() throws Exception {
		this.dao = null;
	}
	
	/**
	 * Test everything all at once!
	 */
	public void test() {
		SmsModemSettings settingsOne = new SmsModemSettings(SERIAL_ONE, true, false, true, false);
		
		assertNull(dao.getSmsModemSettings(SERIAL_ONE));
		
		dao.saveSmsModemSettings(settingsOne);
		
		assertEquals(settingsOne, dao.getSmsModemSettings(SERIAL_ONE));

		SmsModemSettings settingsTwo = new SmsModemSettings(SERIAL_TWO, false, true, false, true);
		
		assertNull(dao.getSmsModemSettings(SERIAL_TWO));
		
		dao.saveSmsModemSettings(settingsTwo);

		assertEquals(settingsOne, dao.getSmsModemSettings(SERIAL_ONE));
		assertEquals(settingsTwo, dao.getSmsModemSettings(SERIAL_TWO));

		SmsModemSettings settingsOneFetched = dao.getSmsModemSettings(SERIAL_ONE);
		assertEquals(settingsOne, settingsOneFetched);
		SmsModemSettings settingsTwoFetched = dao.getSmsModemSettings(SERIAL_TWO);
		assertEquals(settingsTwo, settingsTwoFetched);

		assertTrue(settingsOne.useForSending());
		settingsOne.setUseForSending(false);
		dao.updateSmsModemSettings(settingsOne);
		settingsOne = dao.getSmsModemSettings(SERIAL_ONE);
		assertFalse(settingsOne.useForSending());
	}
}
