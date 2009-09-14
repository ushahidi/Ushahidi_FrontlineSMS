/**
 * 
 */
package net.frontlinesms.data.repository;

import net.frontlinesms.data.domain.Message;
import net.frontlinesms.junit.ReusableTestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Base test class for testing {@link MessageDao}
 * @author Alex
 */
public abstract class ReusableMessageDaoTest extends ReusableTestCase<Message> {
	/** Instance of this DAO implementation we are testing. */
	private MessageDao dao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(MessageDao dao) {
		this.dao = dao;
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.dao = null;
	}
	
	/**
	 * Test everything all at once!
	 */
	public void test() {
		checkSanity();
		
		long startTime = System.currentTimeMillis();
		String ARTHUR = "+44123456789";
		String BERNADETTE = "+447890123456";
		Message m = Message.createIncomingMessage(startTime + 1000, ARTHUR, BERNADETTE, "Hello mate.");
		dao.saveMessage(m);
	
		checkSanity();
		assertEquals(1, dao.getSMSCount(0l, Long.MAX_VALUE));
		assertEquals(1, dao.getSMSCountForMsisdn(ARTHUR, 0l, Long.MAX_VALUE));
		assertEquals(1, dao.getSMSCountForMsisdn(BERNADETTE, 0l, Long.MAX_VALUE));
		assertEquals(0, dao.getSMSCountForMsisdn("whatever i am invented", 0l, Long.MAX_VALUE));
		assertEquals(0, dao.getMessageCount(Message.TYPE_OUTBOUND, 0l, Long.MAX_VALUE));
		assertEquals(1, dao.getMessageCount(Message.TYPE_RECEIVED, 0l, Long.MAX_VALUE));
		
		dao.deleteMessage(m);

		checkSanity();
		assertEquals(0, dao.getSMSCount(startTime, Long.MAX_VALUE));
	}

	/**
	 * Check that various methods agree with each other.
	 */
	private void checkSanity() {
		assertEquals(dao.getSMSCount(0l, Long.MAX_VALUE), dao.getAllMessages().size());
	}
	
}
