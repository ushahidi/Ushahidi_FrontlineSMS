package com.ushahidi.plugins.mapping.data.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import net.frontlinesms.data.domain.Contact;

@Entity
@Table(name="contact_location")
public class ContactLocation {
	
	public ContactLocation() {}
	public ContactLocation(Contact contact) {
		this.contact = contact;
	}
	public ContactLocation(Contact contact, Location location) {
		this.contact = contact;
		this.location = location;
	}

	/** Unique id for this entity.  This is for hibernate usage. */
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true,nullable=false,updatable=false)
	private long id;
	
	//@JoinColumn(name="location_id", nullable=true)
//	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.REMOVE})
//	@JoinTable(name="location_join", joinColumns={@JoinColumn(name="id")},  
//               inverseJoinColumns={@JoinColumn(name="location_id")})
//	@Cascade(value=org.hibernate.annotations.CascadeType.REMOVE)
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Location location;
	
	//@Fetch(FetchMode.JOIN)
	//@JoinColumn(name="contact_id", nullable=true) 
//	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.REMOVE})
//	@JoinTable(name="contact_join", joinColumns={@JoinColumn(name="id")},  
//               inverseJoinColumns={@JoinColumn(name="contact_id")})
//	@Cascade(value=org.hibernate.annotations.CascadeType.REMOVE)
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Contact contact;
	
	/** @return the database ID of this contact */
	public long getId() {
		return this.id;
	}
	
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	
	public Contact getContact() {
		return contact;
	}
	
	public String getContactName() {
		return contact != null ? contact.getName() : null;
	}
	
	public long getContactId() {
		return contact != null ? contact.getId() : 0;
	}
	
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