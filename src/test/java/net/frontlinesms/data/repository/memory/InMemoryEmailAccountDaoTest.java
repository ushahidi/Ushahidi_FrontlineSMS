/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import net.frontlinesms.data.repository.ReusableEmailAccountDaoTest;
import net.frontlinesms.data.repository.memory.InMemoryEmailAccountDao;

/**
 * Tests for in-memory implementation of {@link InMemoryEmailAccountDao}
 * @author Alex
 */
public class InMemoryEmailAccountDaoTest extends ReusableEmailAccountDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.setDao(new InMemoryEmailAccountDao());
	}
}
