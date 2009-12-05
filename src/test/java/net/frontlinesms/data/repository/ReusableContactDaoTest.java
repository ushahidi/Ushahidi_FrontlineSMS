/**
 * 
 */
package net.frontlinesms.data.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.frontlinesms.data.*;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.junit.ReusableTestCase;
import junit.framework.TestCase;

/**
 * Class for testing different implementations of {@link ContactDao}.
 * @author Alex
 */
public abstract class ReusableContactDaoTest extends ReusableTestCase<Contact> {
	/** DAO which we are testing */
	protected ContactDao dao;
	/** Logging object */
	protected final Log log = LogFactory.getLog(getClass());

	public void setDao(ContactDao dao) {
		this.dao = dao;
	}
	
	@Override
	public void tearDown() throws Exception {
		this.dao = null;
	}
	
	/**
	 * Test everything all at once!
	 * @throws DuplicateKeyException 
	 */
	public void test() throws DuplicateKeyException {
		assertEquals(dao.getUngroupedContacts().size(), 0);
		assertEquals(dao.getUnnamedContacts().size(), 0);
		assertEquals(dao.getAllContacts().size(), 0);
		
		
		String name = "Jonny Goodstuff";
		String msisdn = "01234567890";
		Contact jonnyGoodstuff = new Contact(name, msisdn, "07890123456", "jonny@goodstufffamily.com", "A good person", true);
		log.info("About to save contact " + jonnyGoodstuff.getName());
		dao.saveContact(jonnyGoodstuff);
		assertEquals(name, jonnyGoodstuff.getName());
		
		assertEquals(jonnyGoodstuff, dao.getContactByName(name));
		
		assertEquals(jonnyGoodstuff, dao.getFromMsisdn(msisdn));

		assertEquals(1, dao.getContactCount());

		assertEquals(jonnyGoodstuff, dao.getAllContacts().get(0));
		
		assertEquals(dao.getUngroupedContacts(), dao.getAllContacts());
		
		assertEquals(dao.getAllContacts().size(), dao.getContactCount());

		// Check that unnamed contact count works
		assertEquals(dao.getUnnamedContacts().size(), 0);
		Contact noName = new Contact("", "123456789", null, null, null, true);
		dao.saveContact(noName);
		assertEquals(1, dao.getUnnamedContacts().size());
		assertEquals(2, dao.getUngroupedContacts().size());
		Contact nullName = new Contact(null, "1234578", null, null, null, true);
		dao.saveContact(nullName);
		assertEquals(2, dao.getUnnamedContacts().size());
		assertEquals(3, dao.getUngroupedContacts().size());
		
		// Test delete
		dao.deleteContact(noName);
		assertEquals(1, dao.getUnnamedContacts().size());
		
		try {
			// Test problem saving duplicate MSISDN
			Contact jonnyBadstuff = new Contact("Jonny Badstuff", msisdn, null, null, null, true);
			dao.saveContact(jonnyBadstuff);
			fail("SHould have thrown a DuplicateKeyException");
		} catch(DuplicateKeyException ex) {
			/* expected */
		}
	}
}
