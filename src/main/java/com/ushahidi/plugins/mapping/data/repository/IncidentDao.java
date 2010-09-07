package com.ushahidi.plugins.mapping.data.repository;

import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;

import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;

public interface IncidentDao {

	public void saveIncident(Incident incident)	 throws DuplicateKeyException;
		
	public void saveIncident(List<Incident> incidents);
	
	public List<Incident>getAllIncidents(int startIndex, int limit);
	
	public List<Incident>getAllIncidents();
	
	public List<Incident>getAllIncidents(MappingSetup setup);
	
	public int getCount();
	
	public List<Incident> getUnMarkedIncidents(MappingSetup setup);
	
	public void flush();
	
	public Incident findIncident(long frontedId, MappingSetup setup);
	
	public Incident getIncident(long id);
	
	public List<Incident> getIncidentsByLocation(Location location);
	
	public void updateIncident(Incident incident) throws DuplicateKeyException;
	
	public void deleteIncidentsWithMapping(MappingSetup setup);
}
