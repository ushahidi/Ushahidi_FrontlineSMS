package com.ushahidi.plugins.mapping.sync;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import javax.imageio.ImageIO;

import net.frontlinesms.resources.ResourceUtils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.ushahidi.plugins.mapping.data.domain.Audio;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Media;
import com.ushahidi.plugins.mapping.data.domain.News;
import com.ushahidi.plugins.mapping.data.domain.Photo;
import com.ushahidi.plugins.mapping.data.domain.Video;
import com.ushahidi.plugins.mapping.util.MappingLogger;

/**
 * SynchronizationThread
 * @author dalezak
 *
 */
public class SynchronizationThread extends Thread{
//>	STATIC	
	public static MappingLogger LOG = MappingLogger.getLogger(SynchronizationThread.class);	
	
//>	INSTANCE VARIABLES
	/** The base task to be performed by the API; that which requires no extra parameters*/
	private String baseTask;
	
	/** Buffer to store extra task parameters to be passed along in the request */
	private StringBuffer taskBuffer;
	
	/** Instance of the SynchronizationManager that spawned this thread */
	private SynchronizationManager syncManager;
		
	/** URL parameter value appendend at the end of the final url to the submitted*/
	private String urlParameterValue = null;
	
	/** Target URL for the synchronisation */
	private final String baseURL;
	
	/** Tasks queue for processing the sync jobs in  a FIFO fashion */
	private ArrayBlockingQueue<SynchronizationTask> taskQueue = new ArrayBlockingQueue<SynchronizationTask>(10);
	
	private final List<Incident> pendingIncidents = new ArrayList<Incident>();
	private final Map<Long, Location> locations = new HashMap<Long, Location>();
	private final Map<Long, Category> categories = new HashMap<Long, Category>();
	
	/**
	 * Creates an instance of SynchronizationThread
	 * 
	 * @param syncManager SynchronizationManager spawning this thread
	 * @param syncURL The URL to be used for synchronization
	 */
	public SynchronizationThread(SynchronizationManager syncManager, String syncURL){
		this.syncManager = syncManager;
		this.baseURL = syncURL;
	}
	
	/**
	 * Creates an instance of SynchronizationThread
	 * 
	 * @param syncManager SynchronizationManager spawning this thread
	 * @param syncURL The URL to be used for synchronization
	 * @param pending collection of pending incidents to upload
	 */
	public SynchronizationThread(SynchronizationManager syncManager, String syncURL, List<Incident> pending){
		this.syncManager = syncManager;
		this.baseURL = syncURL;
		this.pendingIncidents.clear();
		if (pending != null) {
			this.pendingIncidents.addAll(pending);	
		}
	}
	
	/**
	 * Gets the number of tasks in the task queue
	 * @return
	 */
	public int getTaskCount(){
		return taskQueue.size();
	}
	
	/**
	 * Adds a {@link SynchronizationTask} to the task queue
	 * @param task
	 */
	public void addJob(SynchronizationTask task){
		taskQueue.add(task);
	}
	
	/**
	 * Thread
	 */
	public void run(){
		//Process the items in the task queue in a FIFO fashion
		while(!taskQueue.isEmpty()){
			try{
				SynchronizationTask task = taskQueue.take();
				this.baseTask = task.getTaskName();
				taskBuffer = new StringBuffer();
				taskBuffer.append(this.baseTask);
				if(task.getRequestParameter() != null) {
					taskBuffer.append(task.getRequestParameter());
				}
				if(task.getTaskType().equalsIgnoreCase(SynchronizationAPI.PULL_TASK)){
					if(task.getTaskValues().size() == 0) { 
						performPullTask(); 
					}
					else {
						multiplePullTask(task.getTaskValues());
					}
				}
				else if(task.getTaskType().equalsIgnoreCase(SynchronizationAPI.PUSH_TASK)){
					performPushTask();
				}
				syncManager.updateCurrentTaskNo();
			}
			catch(InterruptedException e){
				LOG.debug("Interruppted synchronization task while waiting ", e);
			}
		}
	}
	
	/**
	 * Performs a pull task from an Ushahidi instance; fetches information
	 * 
	 */
	public void performPullTask(){
		String urlStr = getRequestURL();
		urlStr += (urlParameterValue == null) ? "" : urlParameterValue;
		LOG.debug("URL: %s", urlStr);
		StringBuffer buffer = new StringBuffer();
		try{
			String line = null;
			URL url = new URL(urlStr);			
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			try {
				while((line = reader.readLine()) != null){
					buffer.append(line);
				}
				processPayload(buffer.toString());	
			}
			finally {
				reader.close();
			}
		}
		catch(MalformedURLException mex){
			LOG.debug("Invalid url ", mex);
		}
		catch(IOException iox){
			LOG.debug("Error in fetching response data", iox);
		}
		catch(JSONException jsx){
			LOG.debug("JSON Error ", jsx);
		}
	}
	
	/** Performs a pull task for each of the values in {@link #taskValues} */
	private void multiplePullTask(List<String> values){
		for(String value: values){
			urlParameterValue = value;
			performPullTask();
		}
		urlParameterValue = null;
	}
	
	/**
	 * Pushes information to the Ushahidi instance. Only the items that have been
	 * marked for posting are submitted
	 */
	public void performPushTask(){
		LOG.debug("performPushTask");
		String url = SynchronizationAPI.REQUEST_URL_PREFIX + taskBuffer.toString();
		if(baseTask.equalsIgnoreCase(SynchronizationAPI.POST_INCIDENT)){
			for(Incident incident : this.pendingIncidents) {
				postIncidentAndPhotos(incident, url);
			}
		}
		else if(baseTask.equalsIgnoreCase(SynchronizationAPI.TAG_NEWS)){
			LOG.debug("TODO POST MEDIA URLS");
		}
	}
	
	/**
	 * Gets the complete request URI to be submitted to the Ushahidi API
	 * @return 
	 */
	public String getRequestURL(){
		String extra = (baseURL.charAt(baseURL.length()-1) == '/') ? "" : "/";
		return (baseTask == SynchronizationAPI.POST_INCIDENT) ? baseURL + extra : 
			baseURL + extra + SynchronizationAPI.REQUEST_URL_PREFIX + taskBuffer.toString();
	}
	
	/**
	 * Processes the payload; JSON string returned by the http request
	 * @param payload
	 * @throws JSONException
	 */
	private void processPayload(String payload) throws JSONException{
		JSONObject jsonPayload = new JSONObject(payload);
		LOG.debug("Payload: %s", jsonPayload);
		JSONObject data = jsonPayload.getJSONObject(SynchronizationAPI.PAYLOAD);
		if(baseTask.equals(SynchronizationAPI.GEOMIDPOINT)){
			try {
				String domain = data.getString(SynchronizationAPI.GEOMIDPOINT_DOMAIN_KEY);
				JSONArray locations = data.getJSONArray(SynchronizationAPI.GEOMIDPOINT_LOCATION_KEY);
				if (locations != null && locations.length() > 0) {
					JSONObject location = locations.getJSONObject(0);
					String latitude = location.has("latitude") ? location.getString("latitude") : "0.0";
					String longitude = location.has("longitude") ? location.getString("longitude") : "0.0";
					syncManager.downloadedGeoMidpoint(domain, latitude, longitude);	
				}
			}
			catch(Exception ex) {
				syncManager.synchronizationError(ex.getLocalizedMessage());
			}
		}
		else {
			JSONArray items = data.getJSONArray(baseTask);
			//LOG.debug("items: %s", items);
			for(int i=0; i < items.length(); i++){
				JSONObject item = (JSONObject)items.getJSONObject(i);
				if(baseTask.equals(SynchronizationAPI.CATEGORIES)){
					JSONObject categoryJSON = item.getJSONObject(SynchronizationAPI.CATEGORY_KEY);
					Category category = parseCategory(categoryJSON);
					syncManager.downloadedCategory(category);
				}
				else if(baseTask.equals(SynchronizationAPI.INCIDENTS)){
					JSONObject incidentJSON = item.getJSONObject(SynchronizationAPI.INCIDENT_KEY);
					Incident incident = parseIncident(incidentJSON);
					try {
						if (item.has(SynchronizationAPI.CATEGORIES)) {
							JSONArray categories = (JSONArray)item.getJSONArray(SynchronizationAPI.CATEGORIES);
							if (categories != null) {
								for(int j=0; j < categories.length(); j++){
									JSONObject categoryItem = categories.getJSONObject(j);
									if (categoryItem.has(SynchronizationAPI.CATEGORY_KEY)) {
										JSONObject categoryJSON = categoryItem.getJSONObject(SynchronizationAPI.CATEGORY_KEY);
										Category category = parseCategory(categoryJSON);
										if (category != null) {
											incident.addCategory(category);		
										}		
									}
								}
							}		
						}	
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					try {
						if (item.has(SynchronizationAPI.MEDIA)) {
							JSONArray mediaItems = (JSONArray)item.getJSONArray(SynchronizationAPI.MEDIA);
							if (mediaItems != null) {
								for(int j=0; j < mediaItems.length(); j++){
									JSONObject mediaItem = mediaItems.getJSONObject(j);
									LOG.debug("Media: %s", mediaItem);
									Media media = parseMedia(mediaItem);
									if(media != null) {
										LOG.debug("Adding Media: %s", media);
										incident.addMedia(media);
									}
								}
							}
						}	
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					syncManager.downloadedIncident(incident);
				}
				else if(baseTask.equals(SynchronizationAPI.LOCATIONS)){
					JSONObject locationJSON = item.getJSONObject(SynchronizationAPI.LOCATION_KEY);
					Location location = parseLocation(locationJSON);
					syncManager.downloadedLocation(location);
				}
			}
		}
	}
	
	private Media parseMedia(JSONObject item) throws JSONException{
		LOG.debug("parseMedia: %s", item.toString());
		long id = item.getLong("id");
		int type = item.getInt("type");
		String link = item.getString("link");
		if (type == Media.Type.PHOTO.getCode()) {
			try {
				File destinationDirectory = new File(ResourceUtils.getConfigDirectoryPath(), "photos");
				if (destinationDirectory.exists() == false) {
					destinationDirectory.mkdir();
				}
				File destinationFilePath = new File(destinationDirectory, link);
				URI baseURI = new URI(baseURL.toLowerCase());
				if (destinationFilePath.exists() == false) {
					String sourceFilePath = baseURI.getHost().indexOf(".crowdmap.com") > -1 
						? String.format("%s/media/uploads/%s/%s", baseURL, baseURI.getHost().split("\\.")[0], link)
						: String.format("%s/media/uploads/%s", baseURL, link);
					URL url = new URL(sourceFilePath);
					BufferedImage image = ImageIO.read(url);
					ImageIO.write(image, "jpg", destinationFilePath);
					LOG.debug("Downloaded Photo: %s", sourceFilePath);
				}
				else {
					LOG.debug("Photo Exists, Skipping: %s", destinationFilePath);
				}
				return new Photo(id, link, destinationFilePath.getAbsolutePath());
			} 
			catch (URISyntaxException e) {
				LOG.error("URISyntaxException Parsing URI: %s", baseURL);
			}
			catch (IOException ex) {
				LOG.error("IOException Downloading Media %s : %s", link, ex);
			}
			catch (Exception ex) {
				LOG.error("Unknown Exception Downloading Media %s : %s", link, ex);
			}
			return null;
		}
		if (type == Media.Type.AUDIO.getCode()) {
			return new Audio(id, link);
		}
		if (type == Media.Type.VIDEO.getCode()) {
			return new Video(id, link);
		}
		if (type == Media.Type.NEWS.getCode()) {
			return new News(id, link);
		}
		return null;
	}
	
	/**
	 * Fetches the categories from the JSON array and populates the InMemory database
	 * @param categories
	 * @throws JSONException
	 */
	private Category parseCategory(JSONObject item) throws JSONException{
		LOG.debug("parseCategory: %s", item.toString());
		long id = item.getLong("id");
		if (categories.containsKey(id)) {
			return categories.get(id);
		}
		else {
			Category category = new Category();
			category.setServerId(id);
			if (item.has("title")) {
				category.setTitle(item.getString("title"));
			}
			if (item.has("description")) {
				category.setDescription(item.getString("description"));
			}
			if (item.has("color")) {
				try {
					String color = "#" + item.getString("color");
					category.setColor(Color.decode(color));	
				}
				catch (NumberFormatException ex) {
					category.setColor(Color.RED);
				}
			}
			else {
				category.setColor(Color.RED);
			}
			categories.put(id, category);
			return category;	
		}
	}
	
	private Incident parseIncident(JSONObject item) throws JSONException{
		LOG.debug("parseIncident: %s", item.toString());
		Incident incident = new Incident();
		incident.setServerId(item.getLong("incidentid"));
		incident.setTitle(item.getString("incidenttitle"));
		incident.setDescription(item.getString("incidentdescription"));
		incident.setVerified(item.getInt("incidentverified") == 1);
		incident.setActive(item.getInt("incidentactive") == 1);
		incident.setMarked(false);
		
		try {
			//LOG.debug("Loading Location...");
			long locationId = item.getLong("locationid");
			if (locations.containsKey(locationId)) {
				incident.setLocation(locations.get(locationId));	
			}
			else {
				Location location = new Location();
				location.setServerId(locationId);
				location.setName(item.getString("locationname"));
				location.setLatitude(item.getDouble("locationlatitude"));
				location.setLongitude(item.getDouble("locationlongitude"));
				incident.setLocation(location);	
				locations.put(locationId, location);
			}	
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String dateString = item.getString("incidentdate");;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			Date date = dateFormat.parse(dateString);
			incident.setIncidentDate(date);
		}
		catch(ParseException e){
			LOG.debug("Error in parsing the date",e);
		}
		return incident;
	}
	
	private Location parseLocation(JSONObject item) throws JSONException{
		LOG.debug("parseLocation: %s", item.toString());
		long id = item.getLong("id");
		if (locations.containsKey(id)) {
			return locations.get(id);
		}
		else {
			Location location = new Location();
			location.setServerId(id);
			location.setName(item.getString("name"));
			location.setLatitude(item.getDouble("latitude"));
			location.setLongitude(item.getDouble("longitude"));
			locations.put(id, location);
			return location;	
		}
	}
	
	/**
	 * Posts an incident and photos to the frontend. 
	 * 
	 * @param incident The incident to be posted
	 * @param requestParams Pre-defined url parmaeter string for posting an incident to the frontend 
	 */
	private void postIncidentAndPhotos(Incident incident, String requestParams) {
		String url = String.format("%s%s", getRequestURL(), requestParams);
		LOG.debug("URL: %s", url);
		try {
			SynchronizationPost post = new SynchronizationPost(url);
			post.add(SynchronizationAPI.POST_TASK, SynchronizationAPI.POST_REPORT);
			post.add(SynchronizationAPI.POST_RESP, SynchronizationAPI.JSON);
			post.add(SynchronizationAPI.POST_TITLE, incident.getTitle());
			post.add(SynchronizationAPI.POST_DESCRIPTION, incident.getDescription());
			post.add(SynchronizationAPI.POST_DATE, incident.getDateString());
			post.add(SynchronizationAPI.POST_HOUR, incident.getDateHour());
			post.add(SynchronizationAPI.POST_MINUTE, incident.getDateMinute());
			post.add(SynchronizationAPI.POST_AMPM, incident.getDateAmPm());
			post.add(SynchronizationAPI.POST_CATEGORIES, incident.getCategoryIDs());
			post.add(SynchronizationAPI.POST_LATITUDE, incident.getLocationLatitude());
			post.add(SynchronizationAPI.POST_LONGITUDE, incident.getLocationLongitude());
			post.add(SynchronizationAPI.POST_LOCATION, incident.getLocationName());
			post.add(SynchronizationAPI.POST_FIRSTNAME, incident.getFirstName());
			post.add(SynchronizationAPI.POST_LASTNAME, incident.getLastName());
			post.add(SynchronizationAPI.POST_EMAIL, incident.getEmailAddress());
			for(Photo photo : incident.getPhotos()) {
				post.add(SynchronizationAPI.POST_PHOTO, photo.getFilePath());
			}
			String response = post.postFormData();
			if(response != null && response.toString().indexOf("{") > -1){
				JSONObject payload = new JSONObject(response.toString());
				LOG.debug("PAYLOAD: %s", payload);
				JSONObject status = payload.getJSONObject("error");			
				String message = (String)status.get("message");
				String code = (String)status.get("code");
				if(code.equalsIgnoreCase(SynchronizationAPI.STATUS_SUCCESS)){
					incident.setSyncStatus(null);
					syncManager.uploadedIncident(incident);
					LOG.debug("POST Successful: %s", payload);
				}
				else if (code.equalsIgnoreCase(SynchronizationAPI.STATUS_FORM_POST_FAILED)){
					syncManager.updateFailedIncidents(incident);
					incident.setSyncStatus(message);
					syncManager.updateFailedIncidents(incident);
					LOG.error("POST Failed: %s", message);
				}
				else {
					syncManager.updateFailedIncidents(incident);
					incident.setSyncStatus(message);
					syncManager.updateFailedIncidents(incident);
					LOG.error("POST Failed: %s", message);
				}
			}
			else {
				syncManager.updateFailedIncidents(incident);
				LOG.error("POST Failed: %s", response);
			}
		} 
		catch (MalformedURLException ex) {
			LOG.error("MalformedURLException: %s", ex);
			ex.printStackTrace();
			syncManager.updateFailedIncidents(incident);
		}
		catch (IOException ex) {
			LOG.error("IOException: %s", ex);
			ex.printStackTrace();
			syncManager.updateFailedIncidents(incident);
		} 
		catch (JSONException ex) {
			LOG.error("JSONException: %s", ex);
			ex.printStackTrace();
			syncManager.updateFailedIncidents(incident);
		} 
		catch (Exception ex) {
			LOG.error("Exception: %s", ex);
			ex.printStackTrace();
			syncManager.updateFailedIncidents(incident);
		} 
	}
		
}