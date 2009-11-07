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

	/**
	 * Gets a keyword.
	 * @param parent parent of the keyword to fetch, or <code>null</code> if it is top level
	 * @param keyword keyword
	 * @return keyword, or <code>null</code> if none was found
	 */
	private Keyword get(Keyword parent, String keyword) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(getEqualsOrNull(Keyword.Field.PARENT, parent));
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

	/** @see KeywordDao#getFromMessageText(String)
	 * FIXME not convinced by this method.  WRITE A PROPER UNIT TEST. */
	public Keyword getFromMessageText(String messageText) {
		String[] messageWords = messageText.split("\\s");
		
		Keyword parent = null;
		for(String word : messageWords) {
			Keyword child = this.get(parent, word);
			if(child == null) {
				break;
			} else {
				parent = child;
			}
		}
		return parent;
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
		if(keyword.getParent() == null) {
			// Check there is not already a top-level keyword with this name.  We do this here as SQL does not
			// consider NULL == NULL, so top-level keywords with the same name are allowed in the database.
			// TODO this check/save operation would ideally be atomic - what if someone snuck in and created
			// this keyword between the check and the creation?
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.isNull(Keyword.Field.PARENT.getFieldName()));
			criteria.add(Restrictions.eq(Keyword.Field.KEYWORD.getFieldName(), keyword.getKeyword()));
			if(super.getUnique(criteria) != null) {
				throw new DuplicateKeyException();
			}
		}
		super.save(keyword);
	}
}
