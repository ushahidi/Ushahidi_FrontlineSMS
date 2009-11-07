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

	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setPosition(int indexOf) {
		// TODO Auto-generated method stub
		
	}
}
