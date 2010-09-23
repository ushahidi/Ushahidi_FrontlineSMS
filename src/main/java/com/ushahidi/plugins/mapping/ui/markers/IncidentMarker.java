package com.ushahidi.plugins.mapping.ui.markers;

import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;

/**
 * IncidentMarker
 * @author dalezak
 *
 */
@SuppressWarnings("serial")
public class IncidentMarker extends Marker {

	private Incident incident;
	
	public IncidentMarker(Incident incident){
		this(incident, incident.getLocation());
	}
	
	public IncidentMarker(Incident incident, Location location){
		super("/icons/big_ushahidi.png", location);
		this.incident = incident;
	}
		
	public Incident getIncident(){
		return incident;
	}

	@Override
	public String toString() {
		return String.format("[%s]", incident.getTitle());
	}
	
}