package net.frontlinesms.plugins.forms.data.domain;

import javax.persistence.*;

/**
 * A field in a form.
 * @author Alex
 */
@Entity
public class FormField {
//> INSTANCE PROPERTIES
	/** Unique id for this entity.  This is for hibernate usage. */
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true,nullable=false,updatable=false)
	private long id;
	
	/** The label attached to this field. */
	private String label;
	
	/** The form this field is part of. */
	@ManyToOne
	private Form parentForm;
	
	/** The type of this field. */
	@Enumerated(EnumType.STRING)
	private FormFieldType type;

//> CONSTRUCTORS
	/**
	 * Get a new {@link FormField}.
	 * @param parentForm
	 * @param type
	 * @param label
	 */
	public FormField(Form parentForm, FormFieldType type, String label) {
		this.parentForm = parentForm;
		this.type = type;
		this.label = label;
	}
	
//> ACCESSOR METHODS
	/** @return {@link #parentForm} */
	public Form getForm() {
		return this.parentForm;
	}

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
