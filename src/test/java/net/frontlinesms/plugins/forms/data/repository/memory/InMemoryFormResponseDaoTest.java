package net.frontlinesms.plugins.forms.data.repository.memory;

import net.frontlinesms.plugins.forms.data.repository.ReusableFormResponseDaoTest;

/**
 * Unit test for {@link InMemoryFormResponseDao}
 * @author Alex
 */
public class InMemoryFormResponseDaoTest extends ReusableFormResponseDaoTest {
	/** Set up the test using the in-memory implementation of the DAO */
	@Override
	protected void setUp() throws Exception {
		super.dao = new InMemoryFormResponseDao();
	}
}
