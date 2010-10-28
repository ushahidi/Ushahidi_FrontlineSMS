package com.ushahidi.plugins.mapping.sync;

import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;

/**
 * Synchronization Callback
 * @author dalezak
 *
 */
public interface SynchronizationCallback {
	
	public void synchronizationStarted(int tasks);
	
	public void synchronizationUpdated(int tasks, int completed);
	
	public void synchronizationFinished();
	
	public void synchronizationFailed(String error);
	
	public void downloadedGeoMidpoint(String domain, String latitude, String longitude);
	
	public void downloadedCategory(Category category);
	
	public void downloadedLocation(Location location);
	
	public void downloadedIncident(Incident incident);
	
	public void uploadedIncident(Incident incident);
	
	public void failedIncident(Incident incident);
}