/**
 * 
 */
package net.frontlinesms.data.repository;

import net.frontlinesms.data.domain.KeywordAction;
import net.frontlinesms.junit.ReusableTestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Base test class for testing {@link KeywordActionDao}
 * @author Alex
 */
public abstract class ReusableKeywordActionDaoTest extends ReusableTestCase<KeywordAction> {
	/** Instance of this DAO implementation we are testing. */
	private KeywordActionDao dao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(KeywordActionDao dao) {
		this.dao = dao;
	}
	
	@Override
	public void tearDown() throws Exception {
		this.dao = null;
	}
	
	/**
	 * Test everything all at once!
	 */
	public void test() {
		
	}
}
