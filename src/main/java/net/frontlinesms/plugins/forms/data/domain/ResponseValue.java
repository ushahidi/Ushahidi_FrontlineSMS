/**
 * 
 */
package net.frontlinesms.plugins.forms.data.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Class wrapping {@link String} as an {@link Entity} 
 * @author Alex
 */
@Entity
public class ResponseValue {
	
//> INSTANCE PROPERTIES
	/** Unique id for this entity.  This is for hibernate usage. */
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true,nullable=false,updatable=false)
	private long id;
	/** the value of this String */
	private String value;

//> CONSTRUCTORS
	/**
	 * Create a new {@link ResponseValue}
	 * @param value value of this object
	 */
	public ResponseValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
