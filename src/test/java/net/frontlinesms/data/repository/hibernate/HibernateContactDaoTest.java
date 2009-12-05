/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import net.frontlinesms.junit.HibernateTestCase;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.data.repository.ReusableContactDaoTest;

import org.springframework.beans.factory.annotation.Required;

/**
 * Test class for {@link HibernateContactDao}
 * @author Alex
 */
public class HibernateContactDaoTest extends HibernateTestCase {
//> PROPERTIES
	/** Embedded shared test code from InMemoryDownloadDaoTest - Removes need to CopyAndPaste shared test code */
	private final ReusableContactDaoTest test = new ReusableContactDaoTest() { /* nothing needs to be added */ };

//> TEST METHODS
	/** @see HibernateTestCase#test() */
	public void test() throws DuplicateKeyException {
		test.test();
	}

//> TEST SETUP/TEARDOWN
	/** @see net.frontlinesms.junit.HibernateTestCase#doTearDown() */
	@Override
	public void doTearDown() throws Exception {
		this.test.tearDown();
	}
	
//> ACCESSORS
	/** @param d The DAO to use for the test. */
	@Required
	public void setContactDao(ContactDao d)
	{
		// we can just set the DAO once in the test
		test.setDao(d);
	}
}
