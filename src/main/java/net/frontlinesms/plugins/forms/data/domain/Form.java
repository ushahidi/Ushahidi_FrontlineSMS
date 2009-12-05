package net.frontlinesms.plugins.forms.data.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.*;

import net.frontlinesms.data.domain.Group;

/**
 * A form for filling in with data.
 * @author Alex
 */
@SuppressWarnings("serial")
@Entity
public class Form implements Serializable {
//> FIELD NAMES
	/** Column name for {@link #mobileId} */
	public static final String FIELD_MOBILE_ID = "mobileId";
	/** Column name for {@link #permittedGroup} */
	public static final String FIELD_PERMITTED = "permittedGroup";
	
	/** Value for {@link #mobileId} before a form is finalised */
	public static final int MOBILE_ID_NOT_SET = -1;
	
//> INSTANCE PROPERTIES
	/** Unique id for this entity.  This is for hibernate usage. */
	@SuppressWarnings("unused")
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true,nullable=false,updatable=false)
	private long id;
	
	/** The name of this form */
	private String name;
	/** Fields attached to this form */
	@OneToMany(fetch=FetchType.EAGER, targetEntity=FormField.class, cascade=CascadeType.ALL)
	private List<FormField> fields = new ArrayList<FormField>();
	
	/** The ID of this form when handled on a mobile device. */
	@Column(name=FIELD_MOBILE_ID) // FIXME make this UNIQUE AND NULLABLE (trickier than it first appears)
	private int mobileId = MOBILE_ID_NOT_SET;
	
	/** Phone numbers which are allowed to download this form. */
	@ManyToOne
	private Group permittedGroup;
	
//> CONSTRUCTORS
	/** Empty constructor for hibernate */
	Form() {}
	
	/**
	 * Creates a new form with the supplied name.
	 * @param name name of the form
	 */
	public Form(String name) {
		this.name = name;
	}

//> ACCESSOR METHODS
	/**
	 * Check whether this form is finalised by comparing the value of
	 * {@link #mobileId} to {@link #MOBILE_ID_NOT_SET}.
	 * @return <code>true</code> if this form has had its {@link #mobileId} set; <code>false</code> otherwise.
	 */
	public boolean isFinalised() {
		return this.mobileId != MOBILE_ID_NOT_SET;
	}

	/** @return {@link #fields} */
	public Collection<FormField> getFields() {
		return Collections.unmodifiableList(this.fields);
	}
	
	/** @return {@link #permittedGroup} */
	public Group getPermittedGroup() {
		return permittedGroup;
	}

	/** @param group new value for {@link #permittedGroup} */
	public void setPermittedGroup(Group group) {
		this.permittedGroup = group;
	}

	/** @return {@link #name} */
	public String getName() {
		return this.name;
	}

	/** @param name the new value for {@link #name} */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Adds a new field at the end of the form
	 * @param newField the field to add
	 */
	public void addField(FormField newField) {
		this.addField(newField, this.fields.size());
	}

	/**
	 * Removes a field from this form.
	 * @param formField the field to remove
	 */
	public void removeField(FormField formField) {
		this.fields.remove(formField);
	}

	/**
	 * Adds a new field at the specified position.
	 * @param newField the {@link FormField} to add
	 * @param position the position on the form to add the new field at
	 */
	public void addField(FormField newField, int position) {
		this.fields.add(position, newField);
	}

	/** @return number of fields that are editable */
	public int getEditableFieldCount() {
		int count = 0;
		for(FormField field : this.fields) {
			if(field.getType().hasValue()) {
				++count;
			}
		}
		return count;
	}
	
	/** @return {@link #mobileId} */
	public int getMobileId() {
		return mobileId;
	}

	/** @param mobileId new value for {@link #mobileId} */
	public void setMobileId(int mobileId) {
		if(this.mobileId != MOBILE_ID_NOT_SET) {
			throw new IllegalStateException("Cannot set a mobileId that has already been set.");
		}
		if(this.permittedGroup == null) {
			throw new IllegalStateException("Could not finalise form as no group has been set.");
		}
		this.mobileId = mobileId;
	}

//> GENERATED CODE
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + mobileId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Form other = (Form) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (mobileId != other.mobileId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
