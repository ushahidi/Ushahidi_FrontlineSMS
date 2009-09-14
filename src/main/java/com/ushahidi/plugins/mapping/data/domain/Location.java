/**
 * Data object representing a location. 
 * A location is uniquely identified by it's geographical coordinates; longitude and latitude
 * 
 * @author Ushahidi Dev Team
 */
package com.ushahidi.plugins.mapping.data.domain;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"frontendId","mappingSetup_id"})})
public class Location {
	
//>	COLUMN NAME CONSTANTS
	/** Column name for {@link #frontendId} */
	private static final String FIELD_FRONTEND_ID = "frontendId";
	/** Column name for {@link #name} */
	private static final String FIELD_NAME="name";
	/** Column name for {@link #longitude} */
	private static final String FIELD_LONGITUDE="longitude";
	/** Column name for {@link #latitude} */
	private static final String FIELD_LATITUDE="latitude";

//>	ENTITY FIELDS
	public enum Field implements EntityField<Location>{
		/** Field mapping for {@link Location#frontendId} */
		FRONTEND_ID(FIELD_FRONTEND_ID),
		/** Field mapping for {@link Location#name} */
		NAME(FIELD_NAME),
		/** Field mapping for {@link Location#longitude} */
		LONGITUDE(FIELD_LONGITUDE),
		/** Field mapping for {@link Location#latitude} */
		LATITUDE(FIELD_LATITUDE),
		/** Field mapping for {@link Location#mappingSetup} */
		MAPPING_SETUP("mappingSetup");
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
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, nullable=false, updatable=false)
	private long id;
	
	/** Id of this location on the frontend */
	@Column(name=FIELD_FRONTEND_ID)
	private long frontendId;
	
	/** Name of this location */
	@Column(name=FIELD_NAME,nullable=false)
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
		return id;
	}
	
	/**
	 * Sets the frontend ID of this location
	 * @param id
	 */
	public void setFrontendId(long id){
		this.frontendId = id;
	}
	
	/**
	 * Gets the frontend id of the location
	 * @return {@link #frontendId}
	 */
	public long getFrontendId(){
		return this.frontendId;
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
	
	public void setMappingSetup(MappingSetup setup){
		this.mappingSetup =  setup;
	}
	
	public MappingSetup getMappingSetup(){
		return this.mappingSetup;
	}
}
