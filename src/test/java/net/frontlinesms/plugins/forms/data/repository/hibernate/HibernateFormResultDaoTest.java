package net.frontlinesms.plugins.forms.data.repository.hibernate;

import org.springframework.beans.factory.annotation.Required;

import net.frontlinesms.junit.HibernateTestCase;
import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;
import net.frontlinesms.plugins.forms.data.repository.ReusableFormResponseDaoTest;

/**
 * Unit test for {@link HibernateFormDao}.
 * @author Alex
 */
public class HibernateFormResultDaoTest extends HibernateTestCase {
//> PROPERTIES
	/** Embedded shared test code from InMemoryDownloadDaoTest - Removes need to CopyAndPaste shared test code */
	private final ReusableFormResponseDaoTest test = new ReusableFormResponseDaoTest() { /* nothing needs to be added */ };

//> TEST METHODS
	/** @see HibernateTestCase#test() */
	public void test() throws Throwable {
		test.test();
	}
	
//> ACCESSORS
	/** @param d The DAO to use for the test. */
	@Required
	public void setFormResponseDao(FormResponseDao d)
	{
		// we can just set the DAO once in the test
		test.setDao(d);
	}
}
