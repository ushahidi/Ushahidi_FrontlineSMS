package net.frontlinesms.plugins.forms.data.domain;

import java.io.Serializable;

import javax.persistence.*;

/**
 * A field in a form.
 * @author Alex
 */
@SuppressWarnings("serial")
@Entity
public class FormField implements Serializable {
//> INSTANCE PROPERTIES
	/** Unique id for this entity.  This is for hibernate usage. */
	@SuppressWarnings("unused")
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true,nullable=false,updatable=false)
	private long id;
	
	/** The label attached to this field. */
	private String label;
	
	/** The type of this field. */
	@Enumerated(EnumType.STRING)
	private FormFieldType type;
	
	/** The position of the field within the form. */
	private int positionIndex;

//> CONSTRUCTORS
	/** Empty constructor for hibernate */
	FormField() {}
	
	/**
	 * Get a new {@link FormField}.
	 * @param type
	 * @param label
	 */
	public FormField(FormFieldType type, String label) {
		this.type = type;
		this.label = label;
	}
	
//> ACCESSOR METHODS
	/** @return {@link #type} */
	public FormFieldType getType() {
		return this.type;
	}
	
	/** @return {@link #label} */
	public String getLabel() {
		return this.label;
	}
	
	/** @param label new value for {@link #label} */
	public void setLabel(String label) {
		this.label = label;
	}
	/** @return {@link #positionIndex} */
	public int getPositionIndex() {
		return this.positionIndex;
	}
	/** @param positionIndex new value for {@link #positionIndex} */
	public void setPositionIndex(int positionIndex) {
		this.positionIndex = positionIndex;
	}

//> GENERATED CODE
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + positionIndex;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormField other = (FormField) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (positionIndex != other.positionIndex)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}