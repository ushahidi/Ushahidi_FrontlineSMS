/**
 * 
 */
package net.frontlinesms.plugins.forms.data.repository.hibernate;

import java.util.Collection;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;
import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.repository.FormDao;

/**
 * Hibernate implementation of {@link FormDao}
 * @author Alex
 */
public class HibernateFormDao extends BaseHibernateDao<Form> implements FormDao {

//> CONSTRUCTOR
	/** Create new instance of this DAO */
	public HibernateFormDao() {
		super(Form.class);
	}
	
	/** @see FormDao#getFormsForUser(Contact, Collection) */
	public Collection<Form> getFormsForUser(Contact contact, Collection<Integer> currentFormIds) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.not(Restrictions.eq(Form.FIELD_MOBILE_ID, Form.MOBILE_ID_NOT_SET)));
		if(currentFormIds != null && currentFormIds.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in(Form.FIELD_MOBILE_ID, currentFormIds)));
		}
		// FIXME here we need to add the restriction that the contact is in the permitted group for this form
		return super.getList(criteria);
	}

	/** @see FormDao#getFromMobileId(int) */
	public Form getFromMobileId(int mobileId) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(Form.FIELD_MOBILE_ID, mobileId));
		return super.getUnique(criteria);
	}
	
	/** @see FormDao#saveForm(Form) */
	public void saveForm(Form form) {
		super.saveWithoutDuplicateHandling(form);
	}

	/** @see FormDao#updateForm(Form) */
	public void updateForm(Form form) {
		super.updateWithoutDuplicateHandling(form);
	}

	/** @see FormDao#deleteForm(Form) */
	public void deleteForm(Form form) {
		super.delete(form);
	}

	/** @see FormDao#getAllForms() */
	public Collection<Form> getAllForms() {
		return super.getAll();
	}

	/** @see FormDao#finaliseForm(Form) */
	public void finaliseForm(Form form) throws IllegalStateException {
		// FIXME calculate the new mobile ID
		int mobileId = super.countAll() + 1;
		form.setMobileId(mobileId);
		try {
			super.update(form);
		} catch (DuplicateKeyException e) {
			throw new RuntimeException("This mobile ID has already been set.");
		}
	}
}
