package com.ushahidi.plugins.mapping.data.repository.hibernate;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;
import net.frontlinesms.data.DuplicateKeyException;

import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;

public class HibernateLocationDao extends BaseHibernateDao<Location> implements
		LocationDao {

	public HibernateLocationDao(){
		super(Location.class);
	}
	
	public List<Location> getAllLocations() {
		return super.getAll();
	}
	
	public List<Location> getAllLocations(MappingSetup setup){
		if(getCount() == 0){
			return null;
		}else{
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

	public void saveLocations(List<Location> locations) {
		//Do nothing
	}

	public int getCount() {
		return super.countAll();
	}
	
	/**
	 * Finds a location using the frontend id
	 * @param frontendId ID of the location obtained from the frontend
	 * @return {@link Location}
	 */
	public Location findLocation(long frontendId, MappingSetup setup){
		if(getCount() == 0)
			return null;
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Location.Field.FRONTEND_ID.getFieldName(), new Long(frontendId)));
			criteria.add(Restrictions.eq(Location.Field.MAPPING_SETUP.getFieldName(), setup));
			return super.getUnique(criteria);
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
	

}
