/**
 * 
 */
package net.frontlinesms.plugins.forms.data.repository.hibernate;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;
import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.domain.FormResponse;
import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;

/**
 * Hibernate implementation of {@link FormResponseDao}
 * @author Alex
 */
public class HibernateFormResponseDao extends BaseHibernateDao<FormResponse> implements FormResponseDao {

	/** Create new instance of this DAO */
	public HibernateFormResponseDao() {
		super(FormResponse.class);
	}
	
	/** @see FormResponseDao#getFormResponseCount(Form) */
	public int getFormResponseCount(Form form) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(FormResponse.FIELD_FORM, form));
		return super.getCount(criteria );
	}

	/** @see FormResponseDao#getFormResponses(Form, int, int) */
	public List<FormResponse> getFormResponses(Form form, int startIndex, int limit) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(FormResponse.FIELD_FORM, form));
		return super.getList(criteria, startIndex, limit);
	}

	/** @see FormResponseDao#saveResponse(FormResponse) */
	public void saveResponse(FormResponse formResponse) {
		super.saveWithoutDuplicateHandling(formResponse);
	}

}
