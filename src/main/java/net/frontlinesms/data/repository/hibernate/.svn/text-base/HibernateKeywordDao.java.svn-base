/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.DuplicateKeyException;
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

	/** @see KeywordDao#createKeywordsHierarchically(String[], String, boolean) */
	public Keyword createKeywordsHierarchically(String[] keywordHierarchy, String description, boolean classicMode) throws DuplicateKeyException {
		Keyword parent = null;
		int skip = 0;
		for(String keyword : keywordHierarchy) {
			parent = this.get(parent, keyword);
			if(parent == null) {
				break;
			} else {
				++skip;
			}
		}
		if(skip == keywordHierarchy.length) {
			throw new DuplicateKeyException();
		} else {
			for(; skip < keywordHierarchy.length; ++skip) {
				parent = new Keyword(parent, keywordHierarchy[skip], description);
			}
		}
		return parent;
	}
	
	/**
	 * Gets a keyword.
	 * @param parent parent of the keyword to fetch, or <code>null</code> if it is top level
	 * @param keyword keyword
	 * @return keyword, or <code>null</code> if none was found
	 */
	private Keyword get(Keyword parent, String keyword) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(Keyword.Field.PARENT.getFieldName(), parent));
		criteria.add(Restrictions.eq(Keyword.Field.KEYWORD.getFieldName(), keyword));
		return super.getUnique(criteria);
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

	/** @see KeywordDao#getFromMessageText(String) */
	public Keyword getFromMessageText(String messageText) {
		int partCount = 0;
		String[] messageWords = messageText.split("\\s");
		Keyword parent = null;
		while(true) {
			Keyword child = this.get(parent, messageWords[partCount]);
			if(child == null) {
				return parent;
			} else {
				parent = child;
				++partCount;
			}
		}
	}

	/** @see KeywordDao#getPageNumber(Keyword, int) */
	public int getPageNumber(Keyword keyword, int keywordsPerPage) {
		// TODO do this better
		return super.getAll().indexOf(keyword) / keywordsPerPage;
	}

	/** @see KeywordDao#getRootKeywords() */
	public List<Keyword> getRootKeywords() {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.isNull(Keyword.Field.PARENT.getFieldName()));
		return super.getList(criteria);
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
