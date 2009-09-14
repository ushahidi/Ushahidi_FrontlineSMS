package net.frontlinesms.plugins.forms.data.repository.memory;

import net.frontlinesms.plugins.forms.data.repository.ReusableFormResponseDaoTest;

public class InMemoryFormResponseDaoTest extends ReusableFormResponseDaoTest {
	@Override
	protected void setUp() throws Exception {
		super.dao = new InMemoryFormResponseDao();
	}
}
