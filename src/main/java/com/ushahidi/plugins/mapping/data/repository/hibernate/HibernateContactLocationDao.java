package com.ushahidi.plugins.mapping.data.repository.hibernate;

import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import com.ushahidi.plugins.mapping.data.domain.ContactLocation;
import com.ushahidi.plugins.mapping.data.repository.ContactLocationDao;
import com.ushahidi.plugins.mapping.util.MappingLogger;

public class HibernateContactLocationDao extends BaseHibernateDao<ContactLocation> implements ContactLocationDao {

	@SuppressWarnings("unused")
	private static MappingLogger LOG = MappingLogger.getLogger(HibernateLocationDao.class);
	
	public HibernateContactLocationDao(){
		super(ContactLocation.class);
	}
	
	public void deleteContactLocation(ContactLocation contactLocation) {
		super.delete(contactLocation);
	}

	public List<ContactLocation> getAllContactLocations(int startIndex, int limit) {
		return super.getAll(startIndex, limit);
	}

	public List<ContactLocation> getContactLocations() {
		return super.getAll();
	}

	public void saveContactLocation(ContactLocation contactLocation) throws DuplicateKeyException {
		super.save(contactLocation);
	}

	public void saveContactLocation(List<ContactLocation> contactLocations) throws DuplicateKeyException {
		boolean duplicateKeyException = false;
		for(ContactLocation contactLocation : contactLocations) {
			try {
				super.save(contactLocation);	
			}
			catch (DuplicateKeyException ex) {
				duplicateKeyException = true;
			}
		}
		if (duplicateKeyException) {
			throw new DuplicateKeyException();
		}
	}

	public void saveContactLocationWithoutDuplicateHandling(ContactLocation contactLocation) {
		super.saveWithoutDuplicateHandling(contactLocation);
	}

	public void updateContactLocation(ContactLocation contactLocation) throws DuplicateKeyException {
		super.update(contactLocation);
	}

	public void updateContactLocationWithoutDuplicateHandling(ContactLocation contactLocation) {
		super.updateWithoutDuplicateHandling(contactLocation);
	}
	
	
}