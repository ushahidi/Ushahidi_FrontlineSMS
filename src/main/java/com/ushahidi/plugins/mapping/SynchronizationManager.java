package com.ushahidi.plugins.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.ushahidi.plugins.mapping.ui.MappingUIController;
import com.ushahidi.plugins.mapping.data.domain.*;

import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

public class SynchronizationManager {	
	/** Logger */
	private static final Logger LOG = Utils.getLogger(SynchronizationManager.class);
	
	private final MappingUIController mappingController;
	
	/** Synchronization primitive that keeps track of the number of failed sync attempts */
	private Set<Incident> failedIncidents = new HashSet<Incident>();
	
	/** Controller thread to manage the synchronization thread */
	private ManagerThread managerThread;
	
	/** Handle for the synchronization dialog */
	private Object syncDialog;
	
	/** Keeps track of the current task no */
	private int currentTaskNo = 0;
	
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
		
		SynchronizationThread syncThread = new SynchronizationThread(this, 
				mappingController.getDefaultSynchronizationURL());
		
		syncThread.addJob(new SynchronizationTask(task, requestParameter));
		if(managerThread == null){
			managerThread = new ManagerThread(this, syncThread);
			managerThread.start();
			syncDialog = mappingController.showSynchronizationDialog();
		}
	}
	
	/**
	 * Performs both a push and pull synchronization. The push is done first followed by the incidents
	 */
	public synchronized void performFullSynchronization(){
		//Instantiate the a synchronization thread
		SynchronizationThread syncThread = new SynchronizationThread(this, 
				mappingController.getDefaultSynchronizationURL());
		
		if(getPendingIncidents().size() > 0)
			syncThread.addJob(new SynchronizationTask(SynchronizationAPI.PUSH_TASK, SynchronizationAPI.POST_INCIDENT));
		
		//Fetch categories and locations
		syncThread.addJob(new SynchronizationTask(SynchronizationAPI.PULL_TASK, SynchronizationAPI.CATEGORIES));
		syncThread.addJob(new SynchronizationTask(SynchronizationAPI.PULL_TASK, SynchronizationAPI.LOCATIONS));
		
		// Fetch all incidents
		SynchronizationTask incidentTask = new SynchronizationTask(SynchronizationAPI.PULL_TASK, 
				SynchronizationAPI.INCIDENTS);		
		incidentTask.setRequestParameter(SynchronizationAPI.INCIDENTS_BY_ALL);
		syncThread.addJob(incidentTask);
		
		int taskCount = syncThread.getTaskCount();
		
		//Start the synchronization thread
		if(managerThread == null){
			managerThread = new ManagerThread(this, syncThread);
			managerThread.start();

			//Show the synchronization modal dialog
			syncDialog = mappingController.showSynchronizationDialog();
			mappingController.setSynchronizationTaskCount(syncDialog, taskCount);
			
		}
				
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
		ArrayList<Incident> list = new ArrayList<Incident>();
		list.addAll(failedIncidents);
		return list;
	}
	
	/**
	 * Clears the posted incident from the list of pending incidents
	 * 
	 * @param incident
	 */
	public synchronized void updatePostedIncidents(Incident incident){
		mappingController.updatePostedIncident(incident);
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
	
	/**
	 * Terminates the instance of {@link ManagerThread} that is running the synchronization
	 * @param t Thread to be terminated
	 */
	public synchronized void terminateManagerThread(Thread t){
	
		try{
			//Remove the synchronization dialog
			updateCurrentTaskNo();
			mappingController.updateProgressBar(syncDialog, currentTaskNo);
			Thread.sleep(5000);
			mappingController.removeDialog(syncDialog);

			if(t instanceof ManagerThread)
				managerThread.join();
		}catch(InterruptedException e){
			LOG.debug("Error in terminating synchronization thread manager ", e);
		}

	}
	
	/**
	 * Terminates the current instance of {@link ManagerThread}
	 */
	public synchronized void terminateManagerThread(){
		mappingController.removeDialog(syncDialog);
		
		try{
			managerThread.shutdown();
			managerThread.join();
		}catch(InterruptedException e){
			LOG.debug("Error in terminating the synchronization thread manager ", e);
		}
		
	}
	
	public synchronized void updateCurrentTaskNo(){
		currentTaskNo++;
		mappingController.updateProgressBar(syncDialog, currentTaskNo);
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
			}catch(InterruptedException e){
				
			}
		}
		
	}
}