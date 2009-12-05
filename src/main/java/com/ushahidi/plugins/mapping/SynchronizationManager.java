package com.ushahidi.plugins.mapping;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ushahidi.plugins.mapping.ui.MappingUIController;
import com.ushahidi.plugins.mapping.data.domain.*;

public class SynchronizationManager {	

	/** Maximum size of the thread pool */
	private static final int MAX_THREAD_POOL_SIZE  = 10;
	
	private final MappingUIController mappingController;
	
	/** Executor service to handle synchronisation tasks */
	private static final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
	
	/** Status of the synchronization*/
	private boolean complete = false;
	
	/**
	 * Creates an instance of {@link SynchronizationManager}
	 * 
	 * @param mappingController MappingUIController instance that started up the synchronisation manager
	 */
	public SynchronizationManager(MappingUIController mappingController){
		this.mappingController = mappingController;
	}
	
	/**
	 * Peforms a specific synchronization task. If the synchronisation URL is null, the synchronisation
	 * exits permaturely
	 * 
	 * @param task Synchronization task to be performed
	 * @param requestParameter Parameter(s) to be passed along together with the task
	 */
	public synchronized void runSynchronizationTask(String task, String requestParameter){
		if(mappingController.getDefaultSynchronizationURL()== null)
			return;
		
		SynchronizationTask syncTask = new SynchronizationTask(this, 
				mappingController.getDefaultSynchronizationURL(), task, requestParameter);
		//Spawn a thread to perform the synchronisation
		executorService.submit(syncTask);
	}
	
	/**
	 * Performs both a push and pull synchronization. The push is done first followed by the incidents
	 */
	public synchronized void performFullSynchronization(){
		//Push the incidents so that any new locations are added
		if(getPendingIncidents().size() > 0)
			runSynchronizationTask(SynchronizationAPI.PUSH_TASK, SynchronizationAPI.POST_INCIDENT);
		
		//Pull tasks
		runSynchronizationTask(SynchronizationAPI.PULL_TASK, new String[]{
				SynchronizationAPI.CATEGORIES, SynchronizationAPI.LOCATIONS
				});
		
		// Fetch all incidents
		SynchronizationTask incidentTask = new SynchronizationTask(this, 
				mappingController.getDefaultSynchronizationURL(),
				SynchronizationAPI.PULL_TASK, SynchronizationAPI.INCIDENTS);
		
		incidentTask.addRequestParameter(SynchronizationAPI.INCIDENTS_BY_ALL);
		executorService.submit(incidentTask);
		
	}
	
	/**
	 * Performs a synchronization task with multiple parameters e.g. fetch categories, fetch categories
	 * @param task
	 * @param requestParameters
	 */
	public void runSynchronizationTask(String task, String[] requestParameters){
		for(int i=0; i<requestParameters.length; i++){
			runSynchronizationTask(task, requestParameters[i]);
		}
	}
	
	public void addCategory(Category category){
		mappingController.addCategory(category);
	}
	
	public void addIncident(Incident incident){
		mappingController.addIncident(incident);
	}
	
	public void addLocation(Location location){
		mappingController.addLocation(location);
	}
	
	public synchronized List<Incident> getPendingIncidents(){
		return mappingController.getPendingIncidents();
	}
	
	public boolean synchronizationComplete(){
		return complete;
	}
		
}
