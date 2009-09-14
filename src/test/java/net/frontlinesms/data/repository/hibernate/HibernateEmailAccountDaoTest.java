/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import net.frontlinesms.junit.HibernateTestCase;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.EmailAccountDao;
import net.frontlinesms.data.repository.ReusableEmailAccountDaoTest;
import net.frontlinesms.data.repository.hibernate.*;

import org.springframework.beans.factory.annotation.Required;

/**
 * Test class for {@link HibernateEmailAccountDao}
 * @author Alex
 */
public class HibernateEmailAccountDaoTest extends HibernateTestCase {
//> PROPERTIES
	/** Embedded shared test code from InMemoryDownloadDaoTest - Removes need to CopyAndPaste shared test code */
	private final ReusableEmailAccountDaoTest test = new ReusableEmailAccountDaoTest() { /* nothing needs to be added */ };

//> TEST METHODS
	/** @see HibernateTestCase#test() */
	public void test() throws DuplicateKeyException {
		test.test();
	}
	
//> ACCESSORS
	/** @param d The DAO to use for the test. */
	@Required
	public void setEmailAccountDao(EmailAccountDao d)
	{
		// we can just set the DAO once in the test
		test.setDao(d);
	}
}
