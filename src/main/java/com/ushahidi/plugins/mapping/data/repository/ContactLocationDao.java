package com.ushahidi.plugins.mapping.data.repository;

import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;

import com.ushahidi.plugins.mapping.data.domain.ContactLocation;

/**
 * ContactLocationDao
 * @author dalezak
 *
 */
public interface ContactLocationDao {
	
	public void saveContactLocation(ContactLocation contactLocation) throws DuplicateKeyException;
	
	public void saveContactLocationWithoutDuplicateHandling(ContactLocation contactLocation);
	
	public void saveContactLocation(List<ContactLocation> contactLocations) throws DuplicateKeyException;
	
	public void updateContactLocation(ContactLocation contactLocation) throws DuplicateKeyException;
	
	public void updateContactLocationWithoutDuplicateHandling(ContactLocation contactLocation);
	
	public List<ContactLocation>getAllContactLocations(int startIndex, int limit);
	
	public List<ContactLocation>getContactLocations();
	
	public void deleteContactLocation(ContactLocation contactLocation);
	
}