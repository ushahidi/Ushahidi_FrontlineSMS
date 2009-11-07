/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import net.frontlinesms.data.repository.ReusableKeywordActionDaoTest;
import net.frontlinesms.data.repository.memory.InMemoryKeywordActionDao;

/**
 * Tests for in-memory implementation of {@link InMemoryKeywordActionDao}
 * @author Alex
 */
public class InMemoryKeywordActionDaoTest extends ReusableKeywordActionDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.setDao(new InMemoryKeywordActionDao());
	}
}
