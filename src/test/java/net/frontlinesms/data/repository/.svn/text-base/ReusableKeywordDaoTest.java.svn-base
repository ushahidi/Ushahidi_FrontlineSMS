/**
 * 
 */
package net.frontlinesms.data.repository;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Keyword;
import net.frontlinesms.junit.ReusableTestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Base test class for testing {@link KeywordDao}
 * @author Alex
 */
public abstract class ReusableKeywordDaoTest extends ReusableTestCase<Keyword> {
	/** Instance of this DAO implementation we are testing. */
	private KeywordDao dao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(KeywordDao dao) {
		this.dao = dao;
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.dao = null;
	}
	
	/**
	 * Test everything all at once!
	 * @throws DuplicateKeyException 
	 */
	public void test() throws DuplicateKeyException {
		Keyword blankKeyword = dao.createKeywordsHierarchically(new String[]{""}, "Blank keyword, used to be triggerd by every received message.", false);
		assertNotNull("Failed to create blank keyword.", blankKeyword);
		assertEquals(0, dao.getAllKeywords().size());
		dao.saveKeyword(blankKeyword);
		assertEquals(1, dao.getAllKeywords().size());
		
		Keyword simple = new Keyword(null, "simple", "a very simple keyword");
		assertEquals(1, dao.getAllKeywords().size());
		dao.saveKeyword(simple);
		assertEquals(2, dao.getAllKeywords().size());
		assertEquals(dao.getAllKeywords(), dao.getRootKeywords());
		Keyword simpleChild = new Keyword(simple, "child", "a child of the very simple keyword");
		assertEquals(2, dao.getAllKeywords().size());
		assertEquals(dao.getAllKeywords(), dao.getRootKeywords());
		dao.saveKeyword(simpleChild);
		assertEquals(3, dao.getAllKeywords().size());
		assertEquals(2, dao.getRootKeywords().size());
	}
}
