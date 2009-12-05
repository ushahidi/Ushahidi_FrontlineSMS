/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.domain.Keyword;
import net.frontlinesms.data.domain.KeywordAction;
import net.frontlinesms.data.repository.KeywordActionDao;

/**
 * Hibernate implementation of {@link KeywordActionDao}.
 * @author Alex
 */
public class HibernateKeywordActionDao extends BaseHibernateDao<KeywordAction> implements KeywordActionDao {
	/** Create instance of this class */
	public HibernateKeywordActionDao() {
		super(KeywordAction.class);
	}

	/** @see KeywordActionDao#deleteKeywordAction(KeywordAction) */
	public void deleteKeywordAction(KeywordAction action) {
		super.delete(action);
	}

	/** @see KeywordActionDao#getReplyActions() */
	public Collection<KeywordAction> getReplyActions() {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(KeywordAction.Field.TYPE.getFieldName(), KeywordAction.TYPE_REPLY));
		return super.getList(criteria);
	}

	/** @see KeywordActionDao#getSurveysActions() */
	public Collection<KeywordAction> getSurveysActions() {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(KeywordAction.Field.TYPE.getFieldName(), KeywordAction.TYPE_SURVEY));
		return super.getList(criteria);
	}

	/** @see KeywordActionDao#saveKeywordAction(KeywordAction) */
	public void saveKeywordAction(KeywordAction action) {
		super.saveWithoutDuplicateHandling(action);
	}

	/** @see KeywordActionDao#updateKeywordAction(KeywordAction) */
	public void updateKeywordAction(KeywordAction action) {
		super.updateWithoutDuplicateHandling(action);
	}

	/** @see net.frontlinesms.data.repository.KeywordActionDao#getAction(net.frontlinesms.data.domain.Keyword, int) */
	public KeywordAction getAction(Keyword keyword, int actionType) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(KeywordAction.Field.KEYWORD.getFieldName(), keyword));
		criteria.add(Restrictions.eq(KeywordAction.Field.TYPE.getFieldName(), actionType));
		return super.getUnique(criteria);
	}
	
	/** @see net.frontlinesms.data.repository.KeywordActionDao#getActions(net.frontlinesms.data.domain.Keyword)*/
	public List<KeywordAction> getActions(Keyword keyword) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(KeywordAction.Field.KEYWORD.getFieldName(), keyword));
		return super.getList(criteria);
	}
}
