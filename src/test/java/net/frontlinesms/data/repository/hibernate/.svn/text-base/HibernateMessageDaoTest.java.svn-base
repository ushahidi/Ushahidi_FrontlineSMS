/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import net.frontlinesms.junit.HibernateTestCase;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.MessageDao;
import net.frontlinesms.data.repository.ReusableMessageDaoTest;
import net.frontlinesms.data.repository.hibernate.*;

import org.springframework.beans.factory.annotation.Required;

/**
 * Test class for {@link HibernateMessageDao}
 * @author Alex
 */
public class HibernateMessageDaoTest extends HibernateTestCase {
//> PROPERTIES
	/** Embedded shared test code from InMemoryDownloadDaoTest - Removes need to CopyAndPaste shared test code */
	private final ReusableMessageDaoTest test = new ReusableMessageDaoTest() { /* nothing needs to be added */ };

//> TEST METHODS
	/** @see HibernateTestCase#test() */
	public void test() throws DuplicateKeyException {
		test.test();
	}
	
//> ACCESSORS
	/** @param d The DAO to use for the test. */
	@Required
	public void setMessageDao(MessageDao d)
	{
		// we can just set the DAO once in the test
		test.setDao(d);
	}
}
