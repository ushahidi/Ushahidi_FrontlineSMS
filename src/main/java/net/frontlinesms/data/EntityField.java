/**
 * 
 */
package net.frontlinesms.data;

import javax.persistence.Entity;

import org.hibernate.Criteria;

import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

/**
 * Field of an {@link Entity}.  Implementations of this interface are used by {@link BaseHibernateDao} to allow
 * easier embedding of field names into {@link Criteria} and easier refactoring.
 * 
 * TODO it seems unclear to me wheteher {@link #getFieldName()} is actually meant to return the name of the field in Java or in the database.  It currently
 * looks like it should be the Java name, which contradicts the javadoc comment for {@link #getFieldName()}.
 * 
 * @author Alex
 * @param <E> Entity that these fields are for
 */
public interface EntityField<E> {
	/** @return the field name of this {@link EntityField}.  For hibernate, this is the name used in database */
	public String getFieldName();
}
