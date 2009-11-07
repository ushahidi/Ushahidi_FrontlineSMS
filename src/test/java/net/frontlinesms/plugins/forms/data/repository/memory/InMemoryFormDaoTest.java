package net.frontlinesms.plugins.forms.data.repository.memory;

import net.frontlinesms.plugins.forms.data.repository.ReusableFormDaoTest;

/**
 * Unit test for {@link InMemoryFormDao}
 * @author Alex
 */
public class InMemoryFormDaoTest extends ReusableFormDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.dao = new InMemoryFormDao();
	}
}
