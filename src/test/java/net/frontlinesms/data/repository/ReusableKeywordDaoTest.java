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
	private static final String BLANK_KEYWORD_DESCRIPTION = "The blank keyword.";
	/** Instance of this DAO implementation we are testing. */
	private KeywordDao dao;
	/** Logging object */
	private final Log log = LogFactory.getLog(getClass());

	public void setDao(KeywordDao dao) throws DuplicateKeyException {
		
		System.out.println("ReusableKeywordDaoTest.setDao()");
		
		this.dao = dao;
		dao.saveKeyword(new Keyword("", BLANK_KEYWORD_DESCRIPTION));
	}
	
	@Override
	public void tearDown() throws Exception {
		// Delete all keywords
		for(Keyword k : this.dao.getAllKeywords()) {
			this.dao.deleteKeyword(k);
		}
		this.dao = null;
	}
	
	/**
	 * Test everything all at once!
	 * @throws DuplicateKeyException 
	 */
	public void test() throws DuplicateKeyException {
		// Confirm that the blank keyword exists
		assertEquals(1, dao.getAllKeywords().size());
		
		Keyword simple = new Keyword("simple", "a very simple keyword");
		assertEquals(1, dao.getAllKeywords().size());
		dao.saveKeyword(simple);
		assertEquals(2, dao.getAllKeywords().size());
		assertEquals(dao.getAllKeywords(), dao.getAllKeywords());
		Keyword simpleChild = new Keyword("simple child", "a child of the very simple keyword");
		assertEquals(2, dao.getAllKeywords().size());
		assertEquals(dao.getAllKeywords(), dao.getAllKeywords());
		dao.saveKeyword(simpleChild);
		assertEquals(3, dao.getAllKeywords().size());
		
		dao.deleteKeyword(simpleChild);
		assertEquals(2, dao.getAllKeywords().size());
	}
	
	
	public void testDuplicates() throws DuplicateKeyException {
		try {
			dao.saveKeyword(new Keyword("", BLANK_KEYWORD_DESCRIPTION));
			fail("Duplicate keyword was successfully saved.  This should not be allowed.");
		} catch(DuplicateKeyException ex) {}
		
		dao.saveKeyword(new Keyword("one", ""));
		try {
			dao.saveKeyword(new Keyword("one", ""));
			fail("Duplicate keyword was successfully saved.  This should not be allowed.");
		} catch(DuplicateKeyException ex) { /* expected */ }
	}
	
	public void testKeywordMatching() throws DuplicateKeyException {
		Keyword keyword1 = new Keyword("one", "");
		dao.saveKeyword(keyword1);
		
		Keyword keyword2 = new Keyword("two", "");
		dao.saveKeyword(keyword2);
		
		Keyword keyword3 = new Keyword("three", "");
		dao.saveKeyword(keyword3);
		
		Keyword keyword1a = new Keyword("one a", "");
		dao.saveKeyword(keyword1a);
		
		Keyword keyword2a = new Keyword("two a", "");
		dao.saveKeyword(keyword2a);
		
		Keyword keyword3a = new Keyword("three a", "");
		dao.saveKeyword(keyword3a);
		
		Keyword keyword1ax = new Keyword("one a x", "");
		dao.saveKeyword(keyword1ax);
		
		Keyword keyword1b = new Keyword("one b", "");
		dao.saveKeyword(keyword1b);
		
		Keyword keyword1byz = new Keyword("one b y z", "");
		dao.saveKeyword(keyword1byz);

		testKeywordMatching(keyword1, "one");
		testKeywordMatching(keyword1, "one ");
		testKeywordMatching(keyword1, "one is the keyword that we seek");
		testKeywordMatching(keyword1, "one as the keyword that we seek");
		testKeywordMatching(keyword1a, "one a");
		testKeywordMatching(keyword1a, "one a is the keyword that we seek");
		testKeywordMatching(keyword1a, "one a xis the keyword that we seek");
		testKeywordMatching(keyword1ax, "one a x");
		testKeywordMatching(keyword1ax, "one a x is the keyword that we seek");
		
		// Test again, with upper cases
		testKeywordMatching(keyword1, "ONE");
		testKeywordMatching(keyword1, "ONE ");
		testKeywordMatching(keyword1, "ONE IS THE KEYWORD THAT WE SEEK");
		testKeywordMatching(keyword1, "ONE AS THE KEYWORD THAT WE SEEK");
		testKeywordMatching(keyword1a, "ONE A");
		testKeywordMatching(keyword1a, "ONE A IS THE KEYWORD THAT WE SEEK");
		testKeywordMatching(keyword1a, "ONE A XIS THE KEYWORD THAT WE SEEK");
		testKeywordMatching(keyword1ax, "ONE A X");
		testKeywordMatching(keyword1ax, "ONE A X IS THE KEYWORD THAT WE SEEK");

		// Test again with mixed cases
		testKeywordMatching(keyword1, "oNe");
		testKeywordMatching(keyword1, "onE ");
		testKeywordMatching(keyword1, "One is the keyword that we seek");
		testKeywordMatching(keyword1, "oNE as the keyword that we seek");
		testKeywordMatching(keyword1a, "ONE a");
		testKeywordMatching(keyword1a, "one A is the keyword that we seek");
		testKeywordMatching(keyword1a, "one a XIS THE KEYWORD THAT WE SEEK");
		testKeywordMatching(keyword1ax, "one a X");
		testKeywordMatching(keyword1ax, "ONe A x is the keyword that we seek");
		
		// Test no match
		testKeywordMatching(null, "my one two three is a four five six");
	}
	
	private void testKeywordMatching(Keyword expected, String messageText) {
		assertEquals("Incorrect keyword retrieved for message text: '" + messageText + "'", expected, dao.getFromMessageText(messageText));
	}
}
