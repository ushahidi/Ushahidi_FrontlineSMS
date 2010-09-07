package com.ushahidi.plugins.mapping.data.domain;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;

@Entity
public class MappingSetup {
	
//> COLUMN NAMES
	/** Column name for {@link #name} */
	private static final String FIELD_NAME = "name";
	/**  Column name for {@link #sourceURL} */
	private static final String FIELD_SOURCEURL = "source_url";
	/** Column name for {@link #latitude} */
	private static final String FIELD_LATITUDE  = "latitude";
	/** Column name for {@link #longitude} */
	private static final String FIELD_LONGITUDE = "longitude";
	/** Column name for {@link #defaultSetup } */
	private static final String FIELD_DEFAULT = "defaultSetup";
	/** Column name for {@link #offline} */
	private static final String FIELD_OFFLINE = "offline";
	/** Column name for {@link #offlineMapFile} */
	private static final String FIELD_OFFLINEMAP_FILE = "offline_map_file";
	/** Column name for {@link #onlineMapProvider} */
	private static final String FIELD_ONLINEMAP_PROVIDER = "online_map_provider";

//> ENTITY FIELDS
	/** Details of the fields that this entity has */
	public enum Field implements EntityField<MappingSetup>{
		/** Field mapping for {@link MappingSetup#name} */
		NAME(FIELD_NAME),
		/** Field mapping for {@link MappingSetup#sourceURL} */
		SOURCE_URL(FIELD_SOURCEURL),
		/** Field mapping for {@link MappingSetup#latitude} */
		LATITUDE(FIELD_LATITUDE),
		/** Field mapping for {@link MappingSetup#longitude} */
		LONGITUDE(FIELD_LONGITUDE),
		/** Field mapping for {@link MappingSetup#defaultSetup} */
		DEFAULT(FIELD_DEFAULT),
		/** Field mapping for {@link MappingSetup#offline} */
		OFFLINE(FIELD_OFFLINE),
		/** Field mapping for {@link MappingSetup#offlineMapFile} */
		OFFLINEMAP_FILE(FIELD_OFFLINEMAP_FILE),
		/** Field mapping for {@link MappingSetup#onlineMapProvider} */
		ONLINEMAP_PROVIDER(FIELD_ONLINEMAP_PROVIDER);
		/** Name of the field */
		private final String fieldName;
		/**
		 * Creates a new {@link Field}
		 * @param fieldName
		 */
		Field(String fieldName){ this.fieldName = fieldName; }
		/** @see EntityField#getFieldName() */
		public String getFieldName(){ return this.fieldName; }
	}
	
//>	INSTANCE PROEPRTIES
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, updatable=false, nullable=false)
	private long id;
	
	/** Name of the mapping setup item */
	@Column(name=FIELD_NAME, nullable=false)
	private String name;
	
	/** Source URL for the mapping setup */
	@Column(name=FIELD_SOURCEURL, nullable=false, unique=true)
	private String sourceURL;
	
	/** Longitude for the area in the mapping setup */
	@Column(name=FIELD_LONGITUDE, nullable=false)
	private double longitude;
	
	/** Latitude for the area in the mapping setup */
	@Column(name=FIELD_LATITUDE, nullable=false)
	private double latitude;
	
	/** Flag to denote whether the current setup is default or not */
	@Column(name=FIELD_DEFAULT)
	private boolean defaultSetup;
	
	/** Flag to denote whether the current setup uses an offline map or not */
	@Column(name=FIELD_OFFLINE)
	private boolean offline;
	
	/** File (Absolute) name of the offline map*/
	@Column(name=FIELD_OFFLINEMAP_FILE)
	private String offlineMapFile;
	
	/** Name of the map provider to use for the online map */
	@Column(name=FIELD_ONLINEMAP_PROVIDER)
	private String onlineMapProvider;
	
	/**
	 * Sets the unique identifier for this setup item
	 * @param id
	 */
	public void setId(long id){
		this.id = id;
	}
	
	/**
	 * Gets the unique identifier for the setup item
	 * @return {@link #id}
	 */
	public long getId(){
		return this.id;
	}
	
	/**
	 * Sets the name for the setup item
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Gets the name for the setup item
	 * @return {@link #name}
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Sets the URL with which the synchronization is to be done. For example http://demo.ushahidi.com
	 * The URL specfied must be able to make use of the synchronization API
	 * @param url
	 */
	public void setSourceURL(String url){
		this.sourceURL = url;
	}
	
	/**
	 * Gets the URL that the setup item uses for synchronization 
	 * @return {@link #sourceURL}
	 */
	public String getSourceURL(){
		return sourceURL;
	}
	
	public boolean isSourceURL(String url) {
		if (sourceURL == null && url == null) {
			return true;
		}
		if (sourceURL == null && url != null) {
			return false;
		}
		if (sourceURL != null && url == null) {
			return false;
		}
		if (sourceURL.equalsIgnoreCase(url)) {
			return true;
		}
		if (sourceURL.toUpperCase().indexOf(url.toLowerCase()) > -1) {
			return true;
		}
		if (url.toLowerCase().indexOf(sourceURL.toLowerCase()) > -1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the latitude for the area from which the map is to be pulled
	 * @param latitude
	 */
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	
	/**
	 * Gets the latitude for the target map
	 * @return {@link #latitude}
	 */
	public double getLatitude(){
		return this.latitude;
	}
	
	/**
	 * Sets the longitude for the target map area
	 * @param longitude
	 */
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	/**
	 * Gets the longitude for the target map area
	 * @return {@link #longitude}
	 */
	public double getLongitude(){
		return this.longitude;
	}
	
	/**
	 * Sets the status flag for the setup item
	 * @param active
	 */
	public void setDefaultSetup(boolean active){
		this.defaultSetup = active;
	}
	
	/**
	 * Gets the status value for the mapping setup. If true, the setup parameters can be used
	 * for the synchronization tasks
	 * 
	 * @return {@link #defaultSetup}
	 */
	public boolean isDefaultSetup(){
		return defaultSetup;
	}
	
	/**
	 * Sets {@link #offline} to true or false.
	 * 
	 * @param offline
	 */
	public void setOffline(boolean offline){
		this.offline = offline;
	}
	
	/**
	 * Checks for the map to be used; The online or the offline version. The offline maps are saved
	 * on disk while the online ones are fetched directly from the Internet.
	 * 
	 * @return {@link #offline}
	 */
	public boolean isOffline(){
		return this.offline;
	}
	
	/**
	 * Sets the name of the offline map file
	 * 
	 * @param file Absolute file name of the the offline map
	 */
	public void setOfflineMapFile(String file){
		this.offlineMapFile = file;
	}
	
	/**
	 * Gets the name absolute file name of the offline map
	 * 
	 * @return {@link #offlineMapFile}
	 */
	public String getOfflineMapFile(){
		return this.offlineMapFile;
	}
	
	/**
	 * Sets the name of the online map provider e.g. Yahoo, Google, Microsoft etc
	 * 
	 * @param provider Name of the provider
	 */
	public void setOnlineMapProvider(String provider){
		this.onlineMapProvider = provider;
	}
	
	/**
	 * Gets the name of the provider to be used for the online map
	 * 
	 * @return {@link #onlineMapProvider}
	 */
	public String getOnlineMapProvider(){
		return this.onlineMapProvider;
	}
}
