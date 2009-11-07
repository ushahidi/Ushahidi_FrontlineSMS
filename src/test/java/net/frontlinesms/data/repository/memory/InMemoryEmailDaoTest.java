/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import net.frontlinesms.data.repository.ReusableEmailDaoTest;
import net.frontlinesms.data.repository.memory.InMemoryEmailDao;

/**
 * Tests for in-memory implementation of {@link InMemoryEmailDao}
 * @author Alex
 */
public class InMemoryEmailDaoTest extends ReusableEmailDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.setDao(new InMemoryEmailDao());
		super.setEmailAccountDao(new InMemoryEmailAccountDao());
	}
}
