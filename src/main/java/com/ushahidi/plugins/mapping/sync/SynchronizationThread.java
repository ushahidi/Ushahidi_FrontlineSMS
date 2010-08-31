package com.ushahidi.plugins.mapping.sync;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import org.apache.log4j.Logger;

import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.Incident;

import net.frontlinesms.FrontlineUtils;

public class SynchronizationThread extends Thread{
//>	STATIC	
	private static Logger LOG = FrontlineUtils.getLogger(SynchronizationThread.class);
	
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
	 * Adds a {@link SynchronizationTask} to the task queue
	 * @param task
	 */
	public void addJob(SynchronizationTask task){
		taskQueue.add(task);
	}
	
	
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
					if(task.getTaskValues().size() == 0) performPullTask(); else multiplePullTask(task.getTaskValues());
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
		urlStr += (urlParameterValue == null)? "":urlParameterValue;
		
		LOG.debug("URL: " + urlStr);
		
		StringBuffer buffer = new StringBuffer();
		try{
			String line = null;
			URL url = new URL(urlStr);			
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			try {
				while((line = reader.readLine()) != null){
					buffer.append(line);
				}
				LOG.debug("Payload :" + buffer.toString());			
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
		String urlParameterStr = SynchronizationAPI.REQUEST_URL_PREFIX + taskBuffer.toString();
		LOG.debug(urlParameterStr);
		if(baseTask.equalsIgnoreCase(SynchronizationAPI.POST_INCIDENT)){
			urlParameterStr += SynchronizationAPI.getSubmitURLParameters(baseTask);
			//Fetch all incidents and post them one by one
			for(Incident incident: syncManager.getPendingIncidents()) {
				postIncident(incident, urlParameterStr);
			}
		}
		else if(baseTask.equalsIgnoreCase(SynchronizationAPI.TAG_NEWS)){
			urlParameterStr += SynchronizationAPI.getSubmitURLParameters(baseTask);
		}
	}
	
	/**
	 * Gets the complete request URI to be submitted to the Ushahidi API
	 * @return 
	 */
	public String getRequestURL(){
		String extra = (baseURL.charAt(baseURL.length()-1) == '/')? "" : "/";
		return (baseTask == SynchronizationAPI.POST_INCIDENT)? baseURL + extra : 
			baseURL + extra + SynchronizationAPI.REQUEST_URL_PREFIX + taskBuffer.toString();
	}
	
	/**
	 * Gets the key to be used to lookup values in the payload
	 * @return
	 */
	public String getPayloadKey(){
		return SynchronizationAPI.getParameterKey(baseTask);
	}
	
	/**
	 * Processes the payload; JSON string returned by the http request
	 * @param payload
	 * @throws JSONException
	 */
	private void processPayload(String payload) throws JSONException{
		JSONObject jsonPayload = new JSONObject(payload);
		JSONObject data = jsonPayload.getJSONObject(SynchronizationAPI.PAYLOAD);
		JSONArray items = data.getJSONArray(baseTask);
		for(int i=0; i < items.length(); i++){
			JSONObject item = (JSONObject)items.getJSONObject(i);
			if(baseTask.equals(SynchronizationAPI.CATEGORIES)){
				Category category = fetchCategory((JSONObject)item.get(SynchronizationAPI.CATEGORY_KEY));
				syncManager.addCategory(category);
			}
			else if(baseTask.equals(SynchronizationAPI.INCIDENTS)){
				Incident incident = fetchIncident((JSONObject)item.get(SynchronizationAPI.INCIDENT_KEY));
				if (item.has(SynchronizationAPI.CATEGORIES)) {
					JSONArray categories = (JSONArray)item.getJSONArray(SynchronizationAPI.CATEGORIES);
					if (categories != null) {
						for(int j=0; j < categories.length(); j++){
							JSONObject categoryItem = (JSONObject)categories.getJSONObject(j);
							if (categoryItem.has(SynchronizationAPI.CATEGORY_KEY)) {
								Category category = fetchCategory((JSONObject)categoryItem.get(SynchronizationAPI.CATEGORY_KEY));
								if (category != null) {
									incident.addCategory(category);		
								}		
							}
						}
					}		
				}
				//TODO load media
				syncManager.addIncident(incident);
			}
			else if(baseTask.equals(SynchronizationAPI.LOCATIONS)){
				Location location = fetchLocation((JSONObject)item.get(SynchronizationAPI.LOCATION_KEY));
				syncManager.addLocation(location);
			}
		}
	}
	
	/**
	 * Fetches the categories from the JSON array and populates the InMemory database
	 * @param categories
	 * @throws JSONException
	 */
	private Category fetchCategory(JSONObject item) throws JSONException{
		System.out.println("fetchCategory: " + item.toString());
		Category category = new Category();
		category.setFrontendId(item.getLong("id"));
		if (item.has("title")) {
			category.setTitle(item.getString("title"));
		}
		if (item.has("description")) {
			category.setDescription(item.getString("description"));
		}
		return category;
	}
	
	private Incident fetchIncident(JSONObject item) throws JSONException{
		System.out.println("fetchIncident: " + item.toString());
		Incident incident = new Incident();
		incident.setFrontendId(item.getLong("incidentid"));
		incident.setTitle(item.getString("incidenttitle"));
		incident.setDescription(item.getString("incidentdescription"));
		incident.setVerified(item.getInt("incidentverified") == 1);
		incident.setActive(item.getInt("incidentactive") == 1);
		incident.setMarked(false);
		
		//Fetch the location info
		Location location = new Location();
		location.setFrontendId(item.getLong("locationid"));
		location.setName(item.getString("locationname"));
		location.setLatitude(item.getDouble("locationlatitude"));
		location.setLongitude(item.getDouble("locationlongitude"));
		incident.setLocation(location);
		
		String dateStr = item.getString("incidentdate");;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			Date date = dateFormat.parse(dateStr);
			incident.setIncidentDate(date);
		}
		catch(ParseException e){
			LOG.debug("Error in parsing the date",e);
		}
		return incident;
	}
	
	private Location fetchLocation(JSONObject item) throws JSONException{
		System.out.println("fetchLocation: " + item.toString());
		Location location = new Location();
		location.setFrontendId(item.getInt("id"));
		location.setName(item.getString("name"));
		location.setLatitude(item.getDouble("latitude"));
		location.setLongitude(item.getDouble("longitude"));
		return location;
	}
	
	/**
	 * Posts an incident to the frontend. 
	 * 
	 * @param incident The incident to be posted
	 * @param urlParameterStr Pre-defined url parmaeter string for posting an incident to the frontend
	 */
	private void postIncident(Incident incident, String urlParameterStr){
		Date date = incident.getIncidentDate();
		String parameterStr = String.format(urlParameterStr, 
				incident.getTitle(), 
				incident.getDescription(), 
				getDateTimeComponent(date, "MM/dd/yyyy"), 
				getDateTimeComponent(date, "HH"), getDateTimeComponent(date, "mm"),
				getDateTimeComponent(date, "a").toLowerCase(),
				incident.getCategoryIDs(),
				Double.toString(incident.getLocation().getLatitude()), 
				Double.toString(incident.getLocation().getLongitude()),
				incident.getLocation().getName()
				);
		//post the incident to the frontend
		LOG.debug("Posting incident " + parameterStr);
		try{
			//Send data
			URL url = new URL(getRequestURL());			
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			StringBuffer response = new StringBuffer();
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
			try {
				//write parameters
				writer.write(parameterStr);
				writer.flush();
				
				//Get the response
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				try {
					String line = null;
					while((line = reader.readLine()) != null){
						response.append(line);
					}

					LOG.debug("Response: "  + response.toString());	
				}
				finally {
					reader.close();
				}	
			}
			finally {
				writer.close();
			}			
			//Get the status of the posting and update the sync manager with the list of failed incidents
			if(response.toString().indexOf("{") != -1){
				JSONObject payload = new JSONObject(response.toString());
				JSONObject status = payload.getJSONObject("error");			
				if(((String)status.get("code")).equalsIgnoreCase("0")){
					syncManager.updatePostedIncidents(incident);
					LOG.debug("Incident post succeeded: " + payload);
				}
				else{
					syncManager.updateFailedIncidents(incident);
					LOG.debug("Incident post failed: "+ payload.toString());
				}
			}
			else{
				syncManager.updateFailedIncidents(incident);
				LOG.debug("Incident post failed: "+ response.toString());
			}
			
		}
		catch(MalformedURLException me){
			LOG.debug("URL error: ", me);
		}
		catch(IOException io){
			LOG.debug("IO Error: ", io); 
		}
		catch(JSONException jsx){
			LOG.debug("JSON Error: ", jsx);
		}
	}
	
	private String getDateTimeComponent(Date date, String part){
		SimpleDateFormat dateFormat = new SimpleDateFormat(part);
		return dateFormat.format(date);
	}
	
	/**
	 * Gets the number of tasks in the task queue
	 * @return
	 */
	public int getTaskCount(){
		return taskQueue.size();
	}
	
}