/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import net.frontlinesms.data.repository.ReusableSmsModemSettingsDaoTest;
import net.frontlinesms.data.repository.SmsModemSettingsDao;
import net.frontlinesms.data.repository.memory.InMemorySmsModemSettingsDao;

/**
 * Tests for in-memory implementation of {@link SmsModemSettingsDao}
 * @author Alex
 */
public class InMemorySmsModemSettingsDaoTest extends ReusableSmsModemSettingsDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.setDao(new InMemorySmsModemSettingsDao());
	}
}
