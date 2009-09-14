/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.Collection;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.Order;
import net.frontlinesms.data.domain.Email;
import net.frontlinesms.data.domain.Email.Field;
import net.frontlinesms.data.repository.EmailDao;

/**
 * @author Alex
 *
 */
public class HibernateEmailDao extends BaseHibernateDao<Email> implements EmailDao {
	/** Create a new instance of this class */
	public HibernateEmailDao() {
		super(Email.class);
	}

	/** @see EmailDao#deleteEmail(Email) */
	public void deleteEmail(Email email) {
		super.delete(email);
	}

	/** @see EmailDao#getAllEmails() */
	public Collection<Email> getAllEmails() {
		return super.getAll();
	}

	/** @see EmailDao#getEmailCount() */
	public int getEmailCount() {
		return super.countAll();
	}

	/** @see EmailDao#getEmailsForStatus(Integer[]) */
	public Collection<Email> getEmailsForStatus(Integer[] status) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.in(Field.STATUS.getFieldName(), status));
		return super.getList(criteria);
	}

	/** @see EmailDao#getEmailsWithLimit(Email.Field, net.frontlinesms.data.Order, int, int) */
	public Collection<Email> getEmailsWithLimit(Field sortBy, Order order, int startIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
		return super.getList(criteria, startIndex, limit);
	}

	/** @see EmailDao#getEmailsWithLimitWithoutSorting(int, int) */
	public Collection<Email> getEmailsWithLimitWithoutSorting(int startIndex, int limit) {
		return super.getAll(startIndex, limit);
	}

	/** @see EmailDao#saveEmail(Email) */
	public void saveEmail(Email email) {
		super.saveWithoutDuplicateHandling(email);
	}

}
