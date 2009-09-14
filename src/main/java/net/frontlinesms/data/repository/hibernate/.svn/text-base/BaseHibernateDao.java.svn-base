/**
 * 
 */
package net.frontlinesms.data.repository.hibernate;

import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.EntityField;
import net.frontlinesms.data.Order;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Alex
 * @param <E> Entity that this dao is for
 */
public abstract class BaseHibernateDao<E> extends HibernateDaoSupport {
	/** Logging object */
	final Log log = LogFactory.getLog(getClass());
	
	/** Class that this dao deals with. */
	private final Class<E> clazz;
	/** The unqualified name of {@link #clazz} */
	private final String className;
	
	/**
	 * @param clazz
	 */
	protected BaseHibernateDao(Class<E> clazz) {
		this.clazz = clazz;
		this.className = clazz.getName();
	}
	
	/**
	 * Save an entity, without checking for exceptions thrown for duplicate keys or unique columns.
	 * @param entity entity to save
	 */
	protected void saveWithoutDuplicateHandling(E entity) {
		log.trace("Saving entity: " + entity);
		this.getHibernateTemplate().save(entity);
		log.trace("Entity saved.");
	}
	
	/**
	 * Saves an entity .
	 * @param entity entity to save 
	 * @throws DuplicateKeyException if there was a {@link ConstraintViolationException} thrown while saving
	 */
	protected void save(E entity) throws DuplicateKeyException {
		try {
			saveWithoutDuplicateHandling(entity);
		} catch(RuntimeException ex) {
			if(isClashOfUniqueColumns(ex)) {
				throw new DuplicateKeyException(ex);
			} else {
				throw ex;
			}
		}
	}
	
	/**
	 * Checks if a {@link Throwable} was caused by a clash of unique items.
	 * @param t {@link Throwable} thrown
	 * @return <code>true</code> if the {@link Throwable} represents a clash of unique column values
	 */
	private boolean isClashOfUniqueColumns(Throwable t) {
		Throwable cause = t.getCause();
		return cause != null
				&& (cause instanceof ConstraintViolationException
						|| cause instanceof NonUniqueObjectException);
	}
	
	/**
	 * Updates an entity. 
	 * @param entity entity to update
	 * @throws DuplicateKeyException if there was a {@link ConstraintViolationException} thrown while updating
	 */
	protected void update(E entity) throws DuplicateKeyException {
		try {
			updateWithoutDuplicateHandling(entity);
		} catch(RuntimeException ex) {
			if(isClashOfUniqueColumns(ex)) {
				throw new DuplicateKeyException(ex);
			} else {
				throw ex;
			}
		}
	}

	/**
	 * Updates an entity, without checking for exceptions thrown for duplicate keys or unique columns. 
	 * @param entity entity to update
	 */
	protected void updateWithoutDuplicateHandling(E entity) {
		log.trace("Updating entity: " + entity);
		this.getHibernateTemplate().update(entity);
		log.trace("Entity updated.");
	}
	
	/**
	 * Deletes an entity. 
	 * @param entity entity to delete
	 */
	protected void delete(E entity) {
		log.trace("Deleting entity: " + entity);
		this.getHibernateTemplate().delete(entity);
		log.trace("Entity deleted.");
	}
	
	/**
	 * Gets all entities of type {@link #clazz}.
	 * @return list of all entities of type {@link #clazz}
	 */
	@SuppressWarnings("unchecked")
	protected List<E> getAll() {
		log.trace("Fetching all entities...");
		List<E> allEntities = this.getHibernateTemplate().loadAll(this.clazz);
		log.trace("Fetched: " + allEntities.size());
		return allEntities;
	}
	
	/**
	 * Gets a list of E matching the supplied criteria.
	 * @param criteria
	 * @return a list of Es matching the supplied criteria
	 */
	@SuppressWarnings("unchecked")
	protected List<E> getList(DetachedCriteria criteria) {
		return this.getHibernateTemplate().findByCriteria(criteria);
	}
	
	/**
	 * Gets total number of this entity saved in the database.
	 * @return total number of this entity saved in the database
	 */
	protected int countAll() {
		return (int)((Long)this.getHibernateTemplate().find("select count(*) from " + this.className).get(0)).longValue();
	}
	
	/**
	 * Get all entities within a specific range.
	 * @param startIndex index of first entity to fetch
	 * @param limit maximum number of entities to fetch
	 * @return all entities within a specific range
	 */
	protected List<E> getAll(int startIndex, int limit) {
		return this.getList(getCriterion(), startIndex, limit);
	}
	
	/**
	 * Gets a unique result of type E from the supplied criteria.
	 * @param criteria
	 * @return a single E, or <code>null</code> if none was found.
	 */
	@SuppressWarnings("unchecked")
	protected E getUnique(DetachedCriteria criteria) {
		return (E) DataAccessUtils.uniqueResult(this.getList(criteria));
	}
	
	/**
	 * Gets a paged list of {@link #clazz}.
	 * @param criteria
	 * @param startIndex
	 * @param limit
	 * @return paged list fitting the supplied criteria.
	 */
	@SuppressWarnings("unchecked")
	protected List<E> getList(DetachedCriteria criteria, int startIndex, int limit) {
		return this.getHibernateTemplate().findByCriteria(criteria, startIndex, limit);
	}
	
	/**
	 * Gets a {@link DetachedCriteria} to sort by a particular field.
	 * @param sortBy
	 * @param order
	 * @return {@link DetachedCriteria} with order and sort field set
	 */
	protected DetachedCriteria getSortCriterion(EntityField<E> sortBy, Order order) {
		DetachedCriteria criteria = getCriterion();
		if(sortBy != null) {
			criteria.addOrder(order.getHibernateOrder(sortBy.getFieldName()));
		}
		return criteria;
	}
	
	/**
	 * Gets a {@link DetachedCriteria} to sort by a particular field.
	 * @return {@link DetachedCriteria} with order and sort field set
	 */
	protected DetachedCriteria getCriterion() {
		return DetachedCriteria.forClass(this.clazz);
	}
	
	/**
	 * Gets a count of the results for the supplied criteria.
	 * @param criteria
	 * @return number of result rows there are for the supplied criteria.
	 */
	protected int getCount(DetachedCriteria criteria) {
		criteria.setProjection(Projections.rowCount());
		return DataAccessUtils.intResult(this.getHibernateTemplate().findByCriteria(criteria));
	}
}
