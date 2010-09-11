package com.ushahidi.plugins.mapping.data.repository.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;
import net.frontlinesms.data.DuplicateKeyException;

import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.utils.MappingLogger;

public class HibernateLocationDao extends BaseHibernateDao<Location> implements LocationDao {

	public static MappingLogger LOG = MappingLogger.getLogger(HibernateLocationDao.class);
	
	public HibernateLocationDao(){
		super(Location.class);
	}
	
	public List<Location> getAllLocations() {
		return (getCount() == 0) ? new ArrayList<Location>() : super.getAll();
	}
	
	public List<Location> getAllLocations(MappingSetup setup){
		if(getCount() == 0){
			return new ArrayList<Location>();
		}
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Location.Field.MAPPING_SETUP.getFieldName(), setup));
			return super.getList(criteria);
		}
	}

	public void saveLocation(Location location) throws DuplicateKeyException {
		super.save(location);
	}

	public void flush() {
		//DO nothing
	}

	public void saveLocations(List<Location> locations) throws DuplicateKeyException {
		boolean duplicateKeyException = false;
		for(Location location : locations) {
			try {
				super.save(location);
			} 
			catch (DuplicateKeyException e) {
				duplicateKeyException = true;
				e.printStackTrace();
			}
		}
		if (duplicateKeyException) {
			throw new DuplicateKeyException();
		}
	}

	public int getCount() {
		return super.countAll();
	}
	
	/**
	 * Finds a location using the server id
	 * @param serverId ID of the location obtained from the frontend
	 * @return {@link Location}
	 */
	public Location findLocation(long serverId, MappingSetup setup){
		LOG.debug("findLocation:%d", serverId);
		if(getCount() == 0) {
			return null;
		}
		else{
			try {
				DetachedCriteria criteria = super.getCriterion();
				criteria.add(Restrictions.eq(Location.Field.SERVER_ID.getFieldName(), new Long(serverId)));
				criteria.add(Restrictions.eq(Location.Field.MAPPING_SETUP.getFieldName(), setup));
				return super.getUnique(criteria);	
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**
	 * Retrieves the location with the id in @param id
	 * @param id Unique identifier of the location
	 * @return {@link Location}
	 */
	public Location getLocation(long id){
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.idEq(new Long(id)));
		return super.getUnique(criteria);
	}
	
	public void deleteLocationsWithMapping(MappingSetup setup) {
		for(Location location : getAllLocations(setup)) {
			super.delete(location);
		}
	}
}
