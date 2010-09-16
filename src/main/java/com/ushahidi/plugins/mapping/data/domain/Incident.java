package com.ushahidi.plugins.mapping.data.domain;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import net.frontlinesms.data.EntityField;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"incident_id","mappingSetup_id"})})
public class Incident implements Serializable {
	
	public Incident() {
		this.categories = new ArrayList<Category>();
	}
	
	private static final long serialVersionUID = 1L;
	//>	COLUMN NAME CONSTANTS
	/** Column name for {@link #id} */
	private static final String FIELD_ID = "incident_id";
	/** Column name for {@link #serverId} */
	private static final String FIELD_SERVER_ID = "serverId";
	/** Column name for {@link #title} */
	private static final String FIELD_TITLE = "title";
	/** Column name for {@link #description} */
	private static final String FIELD_DESCRIPTION = "description";
	/** Column name for {@link #incidentDate} */
	private static final String FIELD_INCIDENT_DATE = "incident_date";
	/** Column name for  {@link #marked} */
	private static final String FIELD_MARKED = "marked";
	/** Column name for {@link #active} */
	private static final String FIELD_ACTIVE = "active";
	/** Column name for {@link #verified} */
	private static final String FIELD_VERIFIED  = "verified";
	/** Column name for {@link #categories} */
	private static final String FIELD_CATEGORIES  = "categories";
	/** Column name for {@link #mappingSetup} */
	private static final String FIELD_MAPPING="mappingSetup";
	/** Column name for {@link #mappingSetup} */
	private static final String FIELD_MAPPING_ID="mappingSetup_id";
	
//	/** Column name for {@link #firstName} */
//	private static final String FIELD_FIRST_NAME = "firstName";
//	/** Column name for {@link #lastName} */
//	private static final String FIELD_LAST_NAME = "lastName";
//	/** Column name for {@link #emailAddress } */
//	private static final String FIELD_EMAIL = "emailAddress";
//	/** Column name for {@link #phoneNumber} */
//	private static final String FIELD_PHONE_NUMBER = "phoneNumber";
	
	/** Field mapping for the properties contained in this class/entity */
	public enum Field implements EntityField<Incident>{
		/** Field mapping for {@link Incident#id} */
		ID(FIELD_ID),
		/** Field mapping for {@link Incident#servedId} */
		SERVER_ID(FIELD_SERVER_ID),
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
		/** Field mapping for {@link Incident#catergories} */
		CATEGORIES(FIELD_CATEGORIES),
		/** Field mapping for {@link Incident#mappingSetup} */
		MAPPING_SETUP(FIELD_MAPPING),
		/** Field mapping for {@link Incident#mappingSetup} */
		MAPPING_SETUP_ID(FIELD_MAPPING_ID);
		
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
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name=FIELD_ID, unique=true, nullable=false, updatable=false)
	private long id;
	
	/** Id of this Incident from the server */
	@Column(name=FIELD_SERVER_ID, nullable=true)
	private long serverId;
	
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
	private Date incidentDate;
	
	/** Categories of this incident */
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinTable(name="incident_category",
			   joinColumns=@JoinColumn(name="incident_id", unique=false),
			   inverseJoinColumns=@JoinColumn(name="category_id", unique=false))
	@JoinColumn(name="incident_id") 
	private List<Category> categories;
	
	/** Location of this incident */
	@ManyToOne(cascade=CascadeType.ALL)
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
	public void setServerId(long serverId){
		this.serverId = serverId;
	}
	
	/**
	 * Gets the frontend id of this incident. If the incident has been created from the desktop application,
	 * this value is null. The application shall have to be sync'd so that this value can be set
	 * @return {@link #serverId}
	 */
	public long getServerId(){
		return this.serverId;
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
	
	public boolean isLocation(Location location) {
		if (this.location != null && location != null) {
			return this.location.getId() == location.getId();
		}
		return false;
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
	 * Gets the categories of the incident
	 */
	public List<Category> getCategories() {
		return this.categories;
	}
	
	/**
	 * Sets the categories of this incident
	 * @param c
	 */
	public void setCategories(List<Category> categoryList) {
		this.categories.clear();
		if (categoryList != null) {
			this.categories.addAll(categoryList);	
		}
	}
	
	public void removeCategory(Category category) {
		if (this.categories != null) {
			this.categories.remove(category);
		}
	}
	
	public void removeCategories() {
		if (this.categories != null) {
			this.categories.removeAll(this.categories);
		}
	}
	
	public void addCategory(Category category) {
		this.categories.add(category);
	}
	
	public boolean hasCategory(Category category) {
		if (this.categories != null) {
			for (Category c : this.categories) {
				if (c.getServerId() == category.getServerId()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String getCategoryNames() {
		StringBuilder sb = new StringBuilder();
		if (this.categories != null) {
			for(Category category: this.categories) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(category.getTitle());
			}
		}
		return sb.toString();
	}
	
	public String getCategoryIDs() {
		StringBuilder sb = new StringBuilder();
		if (this.categories != null) {
			for(Category category: this.categories) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(category.getServerId());
			}
		}
		return sb.toString();
	}
	
	public Color getCategoryColor() {
		if (this.categories != null) {
			for(Category category: this.categories) {
				if (category.getColor() != null) {
					return category.getColor();
				}
			}
		}
		return Color.RED;
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
	public void setMappingSetup(MappingSetup mappingSetup){
		this.mappingSetup = mappingSetup;
	}
	
	/**
	 * Gets the mapping setup for the incident
	 * @return {@link #mappingSetup}
	 */
	public MappingSetup getMappingSetup(){
		return this.mappingSetup;
	}
}
