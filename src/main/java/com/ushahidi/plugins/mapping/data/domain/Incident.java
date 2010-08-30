package com.ushahidi.plugins.mapping.data.domain;

import java.util.Date;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;


@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"id","mappingSetup_id"})})
public class Incident {
	
//>	COLUMN NAME CONSTANTS
	/** Column name for {@link #frontendId} */
	private static final String FIELD_FRONTEND_ID = "frontendId";
	/** Column name for {@link #title} */
	private static final String FIELD_TITLE = "title";
	/** Column name for {@link #description} */
	private static final String FIELD_DESCRIPTION = "description";
	/** Column name for {@link #incidentDate} */
	private static final String FIELD_INCIDENT_DATE = "incidentDate";
	/** Column name for  {@link #marked} */
	private static final String FIELD_MARKED = "marked";
	/** Column name for {@link #active} */
	private static final String FIELD_ACTIVE = "active";
	/** Column name for {@link #verified} */
	private static final String FIELD_VERIFIED  = "verified";
	/** Column name for {@link #firstName} */
	private static final String FIELD_FIRST_NAME = "firstName";
	/** Column name for {@link #lastName} */
	private static final String FIELD_LAST_NAME = "lastName";
	/** Column name for {@link #emailAddress } */
	private static final String FIELD_EMAIL = "emailAddress";
	/** Column name for {@link #phoneNumber} */
	private static final String FIELD_PHONE_NUMBER = "phoneNumber";
	
	
//>	ENTITY DETAILS
	/** Field mapping for the properties contained in this class/entity */
	public enum Field implements EntityField<Incident>{
		/** Field mapping for {@link Incident#frontendid} */
		FRONTEND_ID(FIELD_FRONTEND_ID),
		/** Field mapping for {@link Incident#title} */
		TITLE(FIELD_TITLE),
		/** Field mapping for {@link Incident#description} */
		DESCRIPTION(FIELD_DESCRIPTION),
		/** Field mapping for {@link Incident#incidentDate} */
		INCIDENT_DATE(FIELD_INCIDENT_DATE),
		/** Field mapping for {@link Incident#marked} */
		MARKED(FIELD_MARKED),
		/** Field mapping for {@link Incident#verified} */
		VERIFIED(FIELD_VERIFIED),
		/** Field mapping for {@link Incident#active} */
		ACTIVE(FIELD_ACTIVE),
		/** Field mapping for {@link Incident#mappingSetup} */
		MAPPING_SETUP("mappingSetup");
		
		/** name of a field */
		private final String fieldName;
		
		/**
		 * Creates a new {@link Field}
		 * @param fieldName Name of the field
		 */
		Field(String fieldName){ this.fieldName = fieldName; }
		/** @see EntityField#getFieldName() */
		public String getFieldName(){ return this.fieldName; }
	}
	
//>	INSTANCE PROEPRTIES
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, nullable=false, updatable=false)
	private long id;
	
	/** Id of this Incident from the frontend */
	@Column(name=FIELD_FRONTEND_ID)
	private long frontendId;
	
	/** Title of this incident */
	@Column(name=FIELD_TITLE)
	private String title;
	
	/** Description of the incident */
	@Column(name=FIELD_DESCRIPTION, length=2147483647)
	private String description;
	
	/** Flag to denote if the incident has been posted */
	@Column(name=FIELD_MARKED)
	private boolean marked;
	
	/** Flag to denote if the incident has been verified */
	@Column(name=FIELD_VERIFIED)
	private boolean verified;
	
	/** Flag to denote if the incident is active */
	@Column(name=FIELD_ACTIVE)
	private boolean active;
	
	/** Date when incident took place */
	@Column(name=FIELD_INCIDENT_DATE)
	private Date  incidentDate;
	
	/** Category of this incident */
	@ManyToOne
	private Category category;
	
	/** Location of this incident */
	@ManyToOne
	private Location location;
	
	/** MappingSetup associated with this incident */
	@ManyToOne
	private MappingSetup mappingSetup;
		
	/**
	 * Sets a unique identifier for this Incident
	 * @param id
	 */
	public void setId(long id){
		this.id = id;
	}
	
	/**
	 * Gets this incident's unique id
	 * @return {@link #id}
	 */
	public long getId(){
		return this.id;
	}
	
	/**
	 * Sets the id of this incident fetched from the frontend
	 * @param id
	 */
	public void setFrontendId(long id){
		this.frontendId = id;
	}
	
	/**
	 * Gets the frontend id of this incident. If the incident has been created from the desktop application,
	 * this value is null. The application shall have to be sync'd so that this value can be set
	 * @return {@link #frontendId}
	 */
	public long getFrontendId(){
		return this.frontendId;
	}
	
	/**
	 * Sets the title of the incident
	 * @param title
	 */
	public void setTitle(String title){
		this.title = title;
	}
	
	/**
	 * Gets the title of the incident
	 * @return {@link #title}
	 */
	public String getTitle(){
		return title;
	}
	
	/**
	 * Sets the description of the incident
	 * @param description
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**
	 * Gets the description of the incident
	 * @return {@link #description}
	 */
	public String getDescription(){
		return this.description;
	}
	
	/**
	 * Sets the location of the incident
	 * @param location
	 */
	public void setLocation(Location location){
		this.location = location;
	}
	
	/**
	 * Gets the location of the incident
	 * @return {@link #location}
	 */
	public Location getLocation(){
		return location;
	}
	
	/**
	 * Sets the date when the incident occurred
	 * @param date
	 */
	public void setIncidentDate(Date date){
		this.incidentDate = date;
	}
	
	/**
	 * Gets the date when the incident occurred
	 * @return {@link #incidentDate}
	 */
	public Date getIncidentDate(){
		return incidentDate;
	}
	
	/**
	 * Sets the category of this incident
	 * @param category
	 */
	public void setCategory(Category category){
		this.category = category;
	}
	
	/**
	 * Gets the category of the incident
	 * @return {@link #category}
	 */
	public Category getCategory(){
		return category;
	}
	
	/**
	 * Marks this incident for posting during the next synchronisation task 
	 * @param marked
	 */
	public void setMarked(boolean marked){
		this.marked = marked;
	}
	
	/**
	 * Checks whether the incident has been marked for synchronization
	 * @return {@link #marked}
	 */
	public boolean isMarked(){
		return marked;
	}
	
	/**
	 * Set is verified
	 * @param verified
	 */
	public void setVerified(boolean verified){
		this.verified = verified;
	}
	
	/**
	 * Checks whether the incident has been verified
	 * @return {@link #verified}
	 */
	public boolean isVerified(){
		return this.verified;
	}
	
	/**
	 * Set is active
	 * @param active
	 */
	public void setActive(boolean active){
		this.active = active;
	}
	
	/**
	 * Checks whether the incident is active
	 * @return {@link #verified}
	 */
	public boolean isActive(){
		return this.active;
	}
	
	/**
	 * Sets the mapping setup item associated with this incident
	 * @param setup
	 */
	public void setMappingSetup(MappingSetup setup){
		this.mappingSetup = setup;
	}
	
	/**
	 * Gets the mapping setup for the incident
	 * @return {@link #mappingSetup}
	 */
	public MappingSetup getMappingSetup(){
		return this.mappingSetup;
	}
}
