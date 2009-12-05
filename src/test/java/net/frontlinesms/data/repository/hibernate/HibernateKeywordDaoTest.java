/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import net.frontlinesms.junit.HibernateTestCase;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.KeywordDao;
import net.frontlinesms.data.repository.ReusableKeywordDaoTest;

import org.springframework.beans.factory.annotation.Required;

/**
 * Test class for {@link HibernateKeywordDao}
 * @author Alex
 */
public class HibernateKeywordDaoTest extends HibernateTestCase {
//> PROPERTIES
	/** Embedded shared test code from InMemoryDownloadDaoTest - Removes need to CopyAndPaste shared test code */
	private final ReusableKeywordDaoTest test = new ReusableKeywordDaoTest() { /* nothing needs to be added */ };

//> TEST METHODS
	/** @see HibernateTestCase#test() */
	public void test() throws DuplicateKeyException {
		test.test();
	}
	/** @see ReusableKeywordDaoTest#testDuplicates() */
	public void testDuplicates() throws DuplicateKeyException {
		test.testDuplicates();
	}
	/** @see ReusableKeywordDaoTest#testKeywordMatching() */
	public void testKeywordMatching() throws DuplicateKeyException {
		test.testKeywordMatching();
	}

//> TEST SETUP/TEARDOWN
	/** @see net.frontlinesms.junit.HibernateTestCase#doTearDown() */
	@Override
	public void doTearDown() throws Exception {
		this.test.tearDown();
	}
	
//> ACCESSORS
	/** @param d The DAO to use for the test. 
	 * @throws DuplicateKeyException */
	@Required
	public void setKeywordDao(KeywordDao d) throws DuplicateKeyException
	{
		// we can just set the DAO once in the test
		test.setDao(d);
	}
}
