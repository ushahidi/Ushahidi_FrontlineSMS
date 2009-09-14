/**
 * 
 */
package net.frontlinesms.data;

import javax.persistence.Entity;

/**
 * Field of an {@link Entity}.
 * @author Alex
 * @param <E> Entity that these fields are for
 */
public interface EntityField<E> {
	/** @return the field name of this {@link EntityField}.  For hibernate, this is the name used in database */
	public String getFieldName();
}
