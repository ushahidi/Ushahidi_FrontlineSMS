/**
 * Data object representing a location. 
 * A location is uniquely identified by it's geographical coordinates; longitude and latitude
 * 
 * @author Ushahidi Dev Team
 */
package com.ushahidi.plugins.mapping.data.domain;

import java.io.Serializable;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"location_id","mappingSetup_id"})})
public class Location implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//>	COLUMN NAME CONSTANTS
	/** Column name for {@link #id} */
	private static final String FIELD_ID = "location_id";
	/** Column name for {@link #serverId} */
	private static final String FIELD_SERVER_ID = "serverId";
	/** Column name for {@link #name} */
	private static final String FIELD_NAME="name";
	/** Column name for {@link #longitude} */
	private static final String FIELD_LONGITUDE="longitude";
	/** Column name for {@link #latitude} */
	private static final String FIELD_LATITUDE="latitude";
	/** Column name for {@link #mappingSetup} */
	private static final String FIELD_MAPPING="mappingSetup";
	/** Column name for {@link #mappingSetup} */
	private static final String FIELD_MAPPING_ID="mappingSetup_id";

//>	ENTITY FIELDS
	public enum Field implements EntityField<Location>{
		/** Field mapping for {@link Location#id} */
		ID(FIELD_ID),
		/** Field mapping for {@link Location#serverId} */
		SERVER_ID(FIELD_SERVER_ID),
		/** Field mapping for {@link Location#name} */
		NAME(FIELD_NAME),
		/** Field mapping for {@link Location#longitude} */
		LONGITUDE(FIELD_LONGITUDE),
		/** Field mapping for {@link Location#latitude} */
		LATITUDE(FIELD_LATITUDE),
		/** Field mapping for {@link Location#mappingSetup} */
		MAPPING_SETUP(FIELD_MAPPING),
		/** Field mapping for {@link Location#mappingSetup} */
		MAPPING_SETUP_ID(FIELD_MAPPING_ID);
		/** Name of a field */
		private final String fieldName;		
		/**
		 * Creates a new {@link Field}
		 * @param fieldName name of the field
		 */
		Field(String fieldName){this.fieldName = fieldName; }
		
		/** @see EntityField#getFieldName() */
		public String getFieldName(){ return this.fieldName; }
	}

//>	INSTANCE PROPERTIES
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name=FIELD_ID, unique=true, nullable=false, updatable=false)
	private long id;
	
	/** Id of this location on the frontend */
	@Column(name=FIELD_SERVER_ID, nullable=true)
	private long serverId;
	
	/** Name of this location */
	@Column(name=FIELD_NAME, nullable=false)
	private String name;
	
	/** Longitude of this location */
	@Column(name=FIELD_LONGITUDE, nullable=false)
	private double longitude;
	
	/** Latitude for this location */
	@Column(name=FIELD_LATITUDE, nullable=false)
	private double latitude;
	
	@ManyToOne
	private MappingSetup mappingSetup;
	
	/**
	 * Creates an instance of Location with the longitude and latitude values
	 * @param lat Latitude of the location
	 * @param lon Longitude of the location
	 */
	public Location(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Empty constructor
	 */
	public Location(){
		
	}
	
	/**
	 * Sets the unique id of the location
	 * @param id
	 */
	public void setId(long id){
		this.id = id;
	}
	
	/**
	 * Gets the location's unique id
	 * @return {@link #id}
	 */
	public long getId(){
		return this.id;
	}
	
	/**
	 * Sets the frontend ID of this location
	 * @param id
	 */
	public void setServerId(long serverId){
		this.serverId = serverId;
	}
	
	/**
	 * Gets the frontend id of the location
	 * @return {@link #serverId}
	 */
	public long getServerId(){
		return this.serverId;
	}
	
	/**
	 * Sets the location's name
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Gets the name of this location
	 * @return {@link #name}
	 */
	public String getName(){
		return name;
	}
	
	public String getCoordinates() {
		return String.format("%f, %f", latitude, longitude);
	}
	
	/**
	 * Gets the display name of this location
	 */
	public String getDisplayName(){
		return (name != null && name.equalsIgnoreCase("unknown") == false) 
			? String.format("%s (%f, %f)", name, latitude, longitude) 
			: String.format("(%f, %f)", latitude, longitude);
	}
	
	/**
	 * Sets the longitude of this location
	 * @param longitude
	 */
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	/**
	 * Gets the longitude of this location
	 * @return {@link #longitude}
	 */
	public double getLongitude(){
		return longitude;
	}
	
	/**
	 * Sets the location's latitude
	 * @param latitude
	 */
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	
	/**
	 * Gets the latitude of this location
	 * @return {@link #latitude}
	 */
	public double getLatitude(){
		return latitude;
	}
	
	public void setMappingSetup(MappingSetup mappingSetup){
		this.mappingSetup = mappingSetup;
	}
	
	public MappingSetup getMappingSetup(){
		return this.mappingSetup;
	}
}
