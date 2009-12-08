package com.ushahidi.plugins.mapping.data.repository.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.frontlinesms.data.DuplicateKeyException;

import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;

public class InMemoryIncidentDao implements IncidentDao {
	

	Set<Incident> allIncidents = new HashSet<Incident>();

	public List<Incident> getAllIncidents(int startIndex, int limit) {
		ArrayList<Incident> incidents = new ArrayList<Incident>();
		incidents.addAll(allIncidents);
		return incidents.subList(startIndex, Math.min(incidents.size(), startIndex+limit));
	}

	/**
	 * Gets the list of all incidents
	 */
	public List<Incident> getAllIncidents() {
		ArrayList<Incident> incidents = new ArrayList<Incident>();
		incidents.addAll(allIncidents);
		return incidents;
	}
	
	public List<Incident>getAllIncidents(MappingSetup setup){
		return null;
	}
	
	public void saveIncident(Incident incident)	throws DuplicateKeyException {
		allIncidents.add(incident);
	}

	public int getCount() {
		return allIncidents.size();
	}

	public List<Incident> getUnMarkedIncidents(MappingSetup setup) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void flush(){
		allIncidents = null;
		allIncidents = new HashSet<Incident>();
	}

	public void saveIncident(List<Incident> incidents) {
		allIncidents.addAll(incidents);
	}

	public Incident findIncident(long frontedId, MappingSetup setup) {
		// TODO Auto-generated method stub
		return null;
	}

	public Incident getIncident(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Incident> getIncidentsByLocation(Location location) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateIncident(Incident incident) throws DuplicateKeyException {
		// TODO Auto-generated method stub
		
	}

}
