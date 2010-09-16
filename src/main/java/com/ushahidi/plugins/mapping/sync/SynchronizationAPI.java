/**
 * 
 */
package com.ushahidi.plugins.mapping.sync;

/**
 * @author Emmanuel Kala
 *
 */
public final class SynchronizationAPI {
	/* Categories request parameter */
	public static final String CATEGORIES = "categories";
	
	/* Incidents request parameter */
	public static final String INCIDENTS = "incidents";
	
	/*Countries request parameter */
	public static final String COUNTRIES = "countries";
		
	/* Locations request parameter */
	public static final String LOCATIONS = "locations";
	
	/* Adds an incident */
	public static final String POST_INCIDENT = "report";
		
	/* Geographic Midpoint of Incidents */
	public static final String GEOMIDPOINT = "geographicmidpoint";
	
	public static final String GEOMIDPOINT_DOMAIN_KEY = "domain";
	
	public static final String GEOMIDPOINT_LOCATION_KEY = "geographic_midpoint";
	
	/* All incidents */
	public static final String INCIDENTS_BY_ALL	= "&by=all";
	
	/* Incidents by category id */
	public static final String INCIDENTS_BY_CATEGORY_ID = "&by=catid&id=";
	
	/* Incidents by category name */
	public static final String INCIDENTS_BY_CATEGORY_NAME = "&by=catname&name=";
	
	/* Incidents by location name */
	public static final String INCIDENTS_BY_LOCATION_NAME = "locname";
	
	/* Incidents by location id */
	public static final String INCIDENTS_BY_LOCATION_ID = "&by=locid&id=";
	
	/* Parent object for the JSON response */
	public static final String PAYLOAD	= "payload";
	
	
	/* Names for the JSON objects in the payload */
	public static final String CATEGORY_KEY = "category";
	
	public static final String INCIDENT_KEY = "incident";
	
	public static final String COUNTRY_KEY	= "country";
	
	public static final String LOCATION_KEY = "location";
		
	/*Adds a news feed to an existing incident */
	public static final String TAG_NEWS = "tagnews";
	
	/* Adds a video to an existing incident*/
	public static final String TAG_VIDEO = "tagvideo";
	
	/* Adds photo to an existing incident */
	public static final String TAG_PHOTO = "tagphoto";
	
	
	/* Pull task */
	public static final String PULL_TASK = "PULL";
	
	/* Push task */
	public static final String PUSH_TASK = "PUSH";
	
	/* URI prefix used for fetching information from an Ushahidi instance */
	public static final String REQUEST_URL_PREFIX = "api?task=";
	
	
	/* Status messages for the tasks */
	public static final String STATUS_SUCCESS  = "0";
	
	public static final String STATUS_MISSING_PARAMETER = "001";
	
	public static final String STATUS_INVALID_PARAMETER = "002";
	
	public static final String STATUS_FORM_POST_FAILED = "003";
	
	public static final String STATUS_NOT_FOUND = "999";
	
	public static final String POST_TASK = "task";
	public static final String POST_RESP = "resp";
	public static final String POST_REPORT = "report";
	public static final String POST_TITLE = "incident_title";
	public static final String POST_DESCRIPTION = "incident_description";
	public static final String POST_DATE = "incident_date";
	public static final String POST_HOUR = "incident_hour";
	public static final String POST_MINUTE = "incident_minute";
	public static final String POST_AMPM = "incident_ampm";
	public static final String POST_CATEGORIES = "incident_category";
	public static final String POST_LATITUDE = "latitude";
	public static final String POST_LONGITUDE = "longitude";
	public static final String POST_LOCATION = "location_name";
	public static final String POST_FIRSTNAME = "person_first";
	public static final String POST_LASTNAME = "person_last";
	public static final String POST_EMAIL = "person_email";
	public static final String POST_MEDIA_ID = "id";
	public static final String POST_MEDIA_URL = "url";
	
}
