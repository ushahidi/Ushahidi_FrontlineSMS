package com.ushahidi.plugins.mapping.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.ushahidi.plugins.mapping.maps.core.Point;
import com.ushahidi.plugins.mapping.utils.MappingLogger;
import com.ushahidi.plugins.mapping.data.domain.*;

/**
 * Synchronization Manager
 * @author dalezak
 *
 */
public class SynchronizationManager {	
	/** Logger */
	public static MappingLogger LOG = MappingLogger.getLogger(SynchronizationManager.class);
	
	private final SynchronizationCallback callback;
	
	private final String url;
	
	/** Synchronization primitive that keeps track of the number of failed sync attempts */
	private Set<Incident> failedIncidents = new HashSet<Incident>();
	
	/** Controller thread to manage the synchronization thread */
	private ManagerThread managerThread;
	
	/** The total number of tasks */
	private int totalTasks = 0;
	
	/** Keeps track of the current task no */
	private int currentTask = 0;
	
	/**
	 * Creates an instance of {@link SynchronizationManager}
	 * 
	 * @param mappingController MappingUIController instance that started up the synchronisation manager
	 */
	public SynchronizationManager(SynchronizationCallback callback, String url){
		this.callback = callback;
		this.url = url;
	}
	
	/**
	 * Peforms a specific synchronization task. If the synchronisation URL is null, the synchronisation
	 * exits permaturely
	 * 
	 * @param task Synchronization task to be performed
	 * @param requestParameter Parameter(s) to be passed along together with the task
	 */
	public synchronized void runSynchronizationTask(String task, String requestParameter){
		if (this.url == null) {
			return;
		}
		SynchronizationThread syncThread = new SynchronizationThread(this, this.url);
		syncThread.addJob(new SynchronizationTask(task, requestParameter));
		this.totalTasks = syncThread.getTaskCount();
		if (managerThread == null) {
			managerThread = new ManagerThread(this, syncThread);
			managerThread.start();
		}
		this.callback.synchronizationStarted(this.totalTasks);
	}
	
	/**
	 * Performs both a push and pull synchronization. The push is done first followed by the incidents
	 */
	public synchronized void performFullSynchronization(List<Incident> pendingIncidents){
		if (this.url == null) {
			return;
		}
		//Instantiate the a synchronization thread
		SynchronizationThread syncThread = new SynchronizationThread(this, this.url, pendingIncidents);
		
		if (pendingIncidents != null && pendingIncidents.size() > 0) {
			syncThread.addJob(new SynchronizationTask(SynchronizationAPI.PUSH_TASK, SynchronizationAPI.POST_INCIDENT));
		}
		//Fetch categories and locations
		syncThread.addJob(new SynchronizationTask(SynchronizationAPI.PULL_TASK, SynchronizationAPI.CATEGORIES));
		syncThread.addJob(new SynchronizationTask(SynchronizationAPI.PULL_TASK, SynchronizationAPI.LOCATIONS));
		
		// Fetch all incidents
		SynchronizationTask incidentTask = new SynchronizationTask(SynchronizationAPI.PULL_TASK, SynchronizationAPI.INCIDENTS);		
		incidentTask.setRequestParameter(SynchronizationAPI.INCIDENTS_BY_ALL);
		syncThread.addJob(incidentTask);
		this.totalTasks = syncThread.getTaskCount();
		if (managerThread == null) {
			managerThread = new ManagerThread(this, syncThread);
			managerThread.start();
		}
		this.callback.synchronizationStarted(this.totalTasks);
	}
	
	public synchronized void downloadGeoMidpoint() {
		if (this.url == null) {
			return;
		}
		SynchronizationThread syncThread = new SynchronizationThread(this, this.url);
		syncThread.addJob(new SynchronizationTask(SynchronizationAPI.PULL_TASK, SynchronizationAPI.GEOMIDPOINT));
		this.totalTasks = syncThread.getTaskCount();
		if (managerThread == null) {
			managerThread = new ManagerThread(this, syncThread);
			managerThread.start();
		}
		this.callback.synchronizationStarted(this.totalTasks);
	}
	
	/**
	 * Performs a synchronization task with multiple parameters e.g. fetch categories, fetch categories
	 * @param task
	 * @param requestParameters
	 */
	public void runSynchronizationTask(String task, String[] requestParameters){
		for(int i=0; i<requestParameters.length; i++) {
			runSynchronizationTask(task, requestParameters[i]);
		}
	}
	
	/**
	 * Updates the failure count by 1
	 */
	public synchronized void updateFailedIncidents(Incident incident){
		failedIncidents.add(incident);
	}
	
	/**
	 * Gets the list of failed synchronization tasks
	 * @return
	 */
	public synchronized List<Incident> getFailedIncidents(){
		List<Incident> list = new ArrayList<Incident>();
		list.addAll(failedIncidents);
		return list;
	}
	
	/**
	 * Clears the posted incident from the list of pending incidents
	 * 
	 * @param incident
	 */
	public synchronized void updatePostedIncidents(Incident incident){
		this.callback.uploadedIncident(incident);
	}
		
	public void addCategory(Category category){
		this.callback.downloadedCategory(category);
	}
	
	public void addIncident(Incident incident){
		this.callback.downloadedIncident(incident);
	}
	
	public void addLocation(Location location){
		this.callback.downloadedLocation(location);
	}
	
	public void addPoint(String domain, Point point) {
		LOG.debug("domain:%s point:%s", domain, point.toString());
	}
	
	/**
	 * Terminates the instance of {@link ManagerThread} that is running the synchronization
	 * @param t Thread to be terminated
	 */
	public synchronized void terminateManagerThread(Thread thread){
		try{
			this.callback.synchronizationUpdated(totalTasks, currentTask);
			Thread.sleep(5000);
			this.callback.synchronizationFinished();
			if (thread instanceof ManagerThread) {
				managerThread.join();
			}
		}
		catch(InterruptedException e){
			LOG.debug("Error in terminating synchronization thread manager ", e);
		}
	}
	
	/**
	 * Terminates the current instance of {@link ManagerThread}
	 */
	public synchronized void terminateManagerThread(){
		this.callback.synchronizationFinished();
		try{
			managerThread.shutdown();
			managerThread.join();
		}
		catch(InterruptedException e){
			LOG.debug("Error in terminating the synchronization thread manager ", e);
		}
	}
	
	public synchronized void updateCurrentTaskNo(){
		currentTask++;
		this.callback.synchronizationUpdated(totalTasks, currentTask);
	}
	
	/**
	 * Private inner class that manages the execution of the synchronization jobs
	 * @author ekala
	 *
	 */
	private final class ManagerThread extends Thread{
		/** Thread to be run by the manager thread */
		private Thread task;
		/** Instance of SynchronizationManager that spawned this thread */
		private SynchronizationManager manager;
		
		/**
		 * Constructor
		 * 
		 * @param manager Reference to {@link SynchronizationManager} instance spawning this thread
		 * @param task {@link SynchronizationTask} to be run by this thread
		 */
		public ManagerThread(SynchronizationManager manager, SynchronizationThread task){
			this.manager = manager;
			this.task = task;
		}
		
		public void run(){
			task.start();
			
			shutdown();
			
			//Signal {@link SynchronizationManager} to terminate this thread
			manager.terminateManagerThread(this);
		}
		
		/**
		 * Shuts down the {@link SynchronizationTask} thread
		 */
		public void shutdown(){
			try{
				task.join();
			}
			catch(InterruptedException e){
				
			}
		}
		
	}
}