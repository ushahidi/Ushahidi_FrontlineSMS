/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import net.frontlinesms.data.repository.ReusableContactDaoTest;
import net.frontlinesms.data.repository.memory.InMemoryContactDao;

/**
 * @author Alex
 *
 */
public class InMemoryContactDaoTest extends ReusableContactDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.dao = new InMemoryContactDao();
	}
}
