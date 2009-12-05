/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.Order;
import net.frontlinesms.data.domain.Keyword;
import net.frontlinesms.data.repository.KeywordDao;

/**
 * Hibernate implementation of {@link KeywordDao}.
 * @author Alex
 */
public class HibernateKeywordDao extends BaseHibernateDao<Keyword> implements KeywordDao {
	/** Create instance of this class */
	public HibernateKeywordDao() {
		super(Keyword.class);
	}

	/** @see KeywordDao#deleteKeyword(Keyword) */
	public void deleteKeyword(Keyword keyword) {
		super.delete(keyword);
	}

	/** @see KeywordDao#getAllKeywords() */
	public List<Keyword> getAllKeywords() {
		return super.getAll();
	}

	/** @see KeywordDao#getAllKeywords(int, int) */
	public List<Keyword> getAllKeywords(int startIndex, int limit) {
		return super.getAll(startIndex, limit);
	}

	/** @see KeywordDao#getFromMessageText(String)
	 * FIXME Get hibernate to do the keyword sorting and selection for us. */
	public Keyword getFromMessageText(String messageText) {
		messageText = messageText.trim().toUpperCase();
		List<Keyword> results = super.getAll();
		if(results.size() == 0) return null;
		Keyword longest = null;
		for(Keyword k : results) {
			if(k.matches(messageText)
					&& (longest == null || longest.getKeyword().length() < k.getKeyword().length())) {
				longest = k;
			}
		}
		return longest;
	}

	/** @see KeywordDao#getPageNumber(Keyword, int) */
	public int getPageNumber(Keyword keyword, int keywordsPerPage) {
		// TODO do this better
		return super.getAll().indexOf(keyword) / keywordsPerPage;
	}

	/** @see KeywordDao#getTotalKeywordCount() */
	public int getTotalKeywordCount() {
		return super.countAll();
	}

	/** @see KeywordDao#saveKeyword(Keyword) */
	public void saveKeyword(Keyword keyword) throws DuplicateKeyException {
		super.save(keyword);
	}
}
