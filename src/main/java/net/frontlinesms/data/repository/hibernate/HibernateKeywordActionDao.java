/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.Collection;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

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

}
