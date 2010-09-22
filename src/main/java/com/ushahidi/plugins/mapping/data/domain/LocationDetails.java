package com.ushahidi.plugins.mapping.data.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import net.frontlinesms.data.domain.Details;

/**
 * LocationDetails
 * @author dalezak
 *
 */
@Entity
@Table(name="location_details")
public class LocationDetails extends Details {

	public LocationDetails(){}
	public LocationDetails(Location location){
		this.location = location;
	}
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Location location;
	
	public void setLocation(Location location){
		this.location = location;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public long getLocationID(){
		return location != null ? location.getId() : 0;
	}
	
	public String getLocationName() {
		return location != null ? location.getName() : null;
	}
	
	public String getLocationLatitude() {
		return location != null && location.getLatitude() != 0 ? String.valueOf(location.getLatitude()) : null;
	}
	
	public String getLocationLongitude() {
		return location != null && location.getLongitude() != 0 ? String.valueOf(location.getLongitude()) : null;
	}

	public String getLocationCoordinates() {
		return location != null && location.getLatitude() != 0 && location.getLongitude() != 0 
			? String.format("%f, %f", location.getLatitude(), location.getLongitude()) : null;
	}
	
	@Override
	public String toString() {
		return null;
	}
	
}