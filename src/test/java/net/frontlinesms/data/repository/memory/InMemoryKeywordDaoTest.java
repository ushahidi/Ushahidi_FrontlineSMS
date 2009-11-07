/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import net.frontlinesms.data.repository.ReusableKeywordDaoTest;
import net.frontlinesms.data.repository.memory.InMemoryKeywordDao;

/**
 * Tests for in-memory implementation of {@link InMemoryKeywordDao}
 * @author Alex
 */
public class InMemoryKeywordDaoTest extends ReusableKeywordDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.setDao(new InMemoryKeywordDao());
	}
}
