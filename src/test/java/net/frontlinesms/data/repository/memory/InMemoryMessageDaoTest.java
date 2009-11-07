/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import net.frontlinesms.data.repository.ReusableMessageDaoTest;
import net.frontlinesms.data.repository.memory.InMemoryMessageDao;

/**
 * Tests for in-memory implementation of {@link InMemoryMessageDao}
 * @author Alex
 */
public class InMemoryMessageDaoTest extends ReusableMessageDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.setDao(new InMemoryMessageDao());
	}
}
