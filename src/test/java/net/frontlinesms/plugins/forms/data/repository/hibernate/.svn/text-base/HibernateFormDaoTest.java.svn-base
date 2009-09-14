package net.frontlinesms.plugins.forms.data.repository.hibernate;

import org.springframework.beans.factory.annotation.Required;

import net.frontlinesms.junit.HibernateTestCase;
import net.frontlinesms.plugins.forms.data.repository.FormDao;
import net.frontlinesms.plugins.forms.data.repository.ReusableFormDaoTest;

/**
 * Unit test for {@link HibernateFormDao}.
 * @author Alex
 */
public class HibernateFormDaoTest extends HibernateTestCase {
//> PROPERTIES
	/** Embedded shared test code from InMemoryDownloadDaoTest - Removes need to CopyAndPaste shared test code */
	private final ReusableFormDaoTest test = new ReusableFormDaoTest() { /* nothing needs to be added */ };

//> TEST METHODS
	/** @see HibernateTestCase#test() */
	public void test() throws Throwable {
		test.test();
	}
	
//> ACCESSORS
	/** @param d The DAO to use for the test. */
	@Required
	public void setFormDao(FormDao d)
	{
		// we can just set the DAO once in the test
		test.setDao(d);
	}
}
