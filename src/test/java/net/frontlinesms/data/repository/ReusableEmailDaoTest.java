/**
 * 
 */
package net.frontlinesms.data.repository;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.Order;
import net.frontlinesms.data.domain.Email;
import net.frontlinesms.data.domain.EmailAccount;
import net.frontlinesms.junit.ReusableTestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Base test class for testing {@link EmailDao}
 * @author Alex
 */
public abstract class ReusableEmailDaoTest extends ReusableTestCase<Email> {
//> INSTANCE PROPERTIES
	/** Instance of this DAO implementation we are testing. */
	private EmailDao dao;
	/** Dao for email accounts */
	private EmailAccountDao emailAccountDao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(EmailDao dao) {
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
		EmailAccount EMAIL_ACCOUNT = new EmailAccount("test@frontlinesms.net", "frontlinesms.net", 123, "secretpassword", false);
		emailAccountDao.saveEmailAccount(EMAIL_ACCOUNT);
		
		assertEquals(0, dao.getAllEmails().size());
		assertEquals(dao.getAllEmails().size(), dao.getEmailCount());
		assertEquals(dao.getAllEmails(), dao.getEmailsWithLimitWithoutSorting(0, Integer.MAX_VALUE));
		assertEquals(dao.getAllEmails(), dao.getEmailsWithLimit(Email.Field.EMAIL_CONTENT, Order.ASCENDING, 0, Integer.MAX_VALUE));
		
		Email email = new Email(EMAIL_ACCOUNT, "all@myfriends.com", "About the weekend", "Hey guys,\nJust a quick note about the weekend.  I can't wait.\rLove from Mr. Test");
		dao.saveEmail(email);

		assertEquals(1, dao.getAllEmails().size());
		assertEquals(dao.getAllEmails().size(), dao.getEmailCount());
		assertEquals(dao.getAllEmails(), dao.getEmailsWithLimitWithoutSorting(0, Integer.MAX_VALUE));
		assertEquals(dao.getAllEmails(), dao.getEmailsWithLimit(Email.Field.FROM, Order.ASCENDING, 0, Integer.MAX_VALUE));
		
		dao.deleteEmail(email);

		assertEquals(0, dao.getAllEmails().size());
		assertEquals(dao.getAllEmails().size(), dao.getEmailCount());
		assertEquals(dao.getAllEmails(), dao.getEmailsWithLimitWithoutSorting(0, Integer.MAX_VALUE));
		assertEquals(dao.getAllEmails(), dao.getEmailsWithLimit(Email.Field.STATUS, Order.ASCENDING, 0, Integer.MAX_VALUE));
	}

	/**
	 * @param emailAccountDao the emailAccountDao to set
	 */
	public void setEmailAccountDao(EmailAccountDao emailAccountDao) {
		this.emailAccountDao = emailAccountDao;
	}
}
