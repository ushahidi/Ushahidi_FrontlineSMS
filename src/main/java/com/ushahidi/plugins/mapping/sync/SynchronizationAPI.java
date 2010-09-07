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
	
	
	/* Parameters for reporting an incident through the Ushahidi API */
	private static final String REPORT_INCIDENT_PARAMETERS = "&incident_title=%s"+
		"&incident_description=%s&incident_date=%s&incident_hour=%s&incident_minute=%s"+
		"&incident_ampm=%s&incident_category=%s&latitude=%s&longitude=%s&location_name=%s";
	
	/* Parameters for all tagging tasks for media (photos, video and news) */
	private static final String TAGMEDIA_TASK_PARAMETERS = "id=%d&url=%s";
		
	
	public static String getParameterKey(String task){
		if(task.equalsIgnoreCase(INCIDENTS)){
			return INCIDENT_KEY;
		}else if(task.equalsIgnoreCase(COUNTRIES)){
			return COUNTRY_KEY;
		}else if(task.equalsIgnoreCase(CATEGORIES)){
			return CATEGORY_KEY;
		}else if(task.equalsIgnoreCase(LOCATIONS)){
			return LOCATION_KEY;
		}else{	
			return null;
		}
	}
	
	/**
	 * Gets the URL parameter string to be submitted to the API during synchronization
	 * @param postTask
	 * @return
	 */
	public static String getSubmitURLParameters(String postTask){
		if(postTask.equalsIgnoreCase(POST_INCIDENT)){
			return REPORT_INCIDENT_PARAMETERS;
		}else if(postTask.equalsIgnoreCase(TAG_PHOTO) || postTask.equalsIgnoreCase(TAG_NEWS)
				||postTask.equalsIgnoreCase(TAG_VIDEO)){
			return TAGMEDIA_TASK_PARAMETERS;
		}
		return null;
	}
	
}
