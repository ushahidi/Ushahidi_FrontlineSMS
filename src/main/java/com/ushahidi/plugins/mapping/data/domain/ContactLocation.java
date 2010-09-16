package com.ushahidi.plugins.mapping.data.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import net.frontlinesms.data.domain.Contact;

@Entity
@Table(name="contact_location")
@Inheritance(strategy = InheritanceType.JOINED)
public class ContactLocation extends Contact {
	
	public ContactLocation() {
		super(null, null, null, null, null, true);
	}

	@ManyToOne(targetEntity=Location.class, cascade=CascadeType.ALL)
	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@Fetch (FetchMode.SELECT)
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
}