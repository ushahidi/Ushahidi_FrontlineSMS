/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import net.frontlinesms.junit.HibernateTestCase;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.GroupDao;
import net.frontlinesms.data.repository.ReusableGroupDaoTest;

import org.springframework.beans.factory.annotation.Required;

/**
 * Test class for {@link HibernateGroupDao}
 * @author Alex
 */
public class HibernateGroupDaoTest extends HibernateTestCase {
//> PROPERTIES
	/** Embedded shared test code from InMemoryDownloadDaoTest - Removes need to CopyAndPaste shared test code */
	private final ReusableGroupDaoTest test = new ReusableGroupDaoTest() { /* nothing needs to be added */ };

//> TEST METHODS
	/** @see HibernateTestCase#test() */
	public void test() throws DuplicateKeyException {
		test.test();
	}
	/** @see ReusableGroupDaoTest#testCascadingDelete() */
	public void testCascadingDelete() throws DuplicateKeyException {
		test.testCascadingDelete();
	}
	/** @see ReusableGroupDaoTest#testChildDelete() */
	public void testChildDelete() throws DuplicateKeyException {
		test.testChildDelete();
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
	public void setGroupDao(GroupDao d)
	{
		// we can just set the DAO once in the test
		test.setDao(d);
	}
}
