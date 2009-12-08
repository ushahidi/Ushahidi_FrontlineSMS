package com.ushahidi.plugins.mapping.data.repository.hibernate;

import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class HibernateIncidentDao extends BaseHibernateDao<Incident> implements
		IncidentDao {

	public HibernateIncidentDao(){
		super(Incident.class);
	}
	
	public List<Incident> getAllIncidents() {
		return (getCount() == 0)? new ArrayList<Incident>() : super.getAll();
	}

	public List<Incident> getAllIncidents(int startIndex, int limit) {
		return (getCount() == 0)?new ArrayList<Incident>():getAll(startIndex, limit);
	}

	public void saveIncident(Incident incident) throws DuplicateKeyException {		
		super.save(incident);

	}

	public int getCount() {
		return super.countAll();
	}

	/**
	 * Gets the list of all incidents that are yet to be pushed to the frontend (online instance)
	 */
	public List<Incident> getUnMarkedIncidents(MappingSetup setup) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(Incident.Field.MAPPING_SETUP.getFieldName(), setup));
		criteria.add(Restrictions.eq(Incident.Field.MARKED.getFieldName(), new Boolean(true)));
		return super.getList(criteria);
	}
	
	public void flush(){
		//Do nothing
	}

	public void saveIncident(List<Incident> incidents) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Retrieves the incident whose unique id on the frontend is the one in @param frontendId
	 * @param frontendId Unique id of the incident on the frontend
	 * @return {@link Incident}
	 */
	public Incident findIncident(long frontendId, MappingSetup setup){
		if(getCount() == 0)
			return null;
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Incident.Field.FRONTEND_ID.getFieldName(), new Long(frontendId)));
			criteria.add(Restrictions.eq(Incident.Field.MAPPING_SETUP.getFieldName(), setup));
			return super.getUnique(criteria);
		}
	}
	
	/**
	 * Retrives the incident whose unique id is @param id
	 * @param id Unique id of the category
	 * @return {@link Incident}
	 */
	public Incident getIncident(long id){
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.idEq(new Long(id)));
		return super.getUnique(criteria);
	}
	
	/**
	 * Gets incidents by location
	 * 
	 * @param location Location of the incidents to be fetched
	 * @return
	 */
	public List<Incident> getIncidentsByLocation(Location location){
		if(getCount() == 0)
			return null;
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq("location", location));
			criteria.add(Restrictions.eq(Incident.Field.MAPPING_SETUP.getFieldName(), 
					location.getMappingSetup()));
			return super.getList(criteria);
		}
	}
	
	/**
	 * Gets incidents by category
	 * 
	 * @param category Category of the incidents to be fetched
	 * @return
	 */
	public List<Incident> getIncidentsByCategory(Category category){
		if(getCount() == 0)
			return null;
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq("category", category));
			return super.getList(criteria);
		}
	}
	
	/**
	 * Gets all the incidents for a given mapping setup
	 * 
	 * @param setup {@link MappingSetup} of the incidents to be fetched
	 * @return
	 */
	public List<Incident> getAllIncidents(MappingSetup setup){
		if(getCount() == 0)
			return new ArrayList<Incident>();
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Incident.Field.MAPPING_SETUP.getFieldName(), setup));
			return super.getList(criteria);
		}
	}

	public void updateIncident(Incident incident) throws DuplicateKeyException {
		super.update(incident);
	}
	

}
