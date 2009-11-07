/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import net.frontlinesms.data.repository.ReusableGroupDaoTest;
import net.frontlinesms.data.repository.memory.InMemoryGroupDao;

/**
 * Tests for in-memory implementation of {@link InMemoryGroupDao}
 * @author Alex
 */
public class InMemoryGroupDaoTest extends ReusableGroupDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.setDao(new InMemoryGroupDao());
	}
}
