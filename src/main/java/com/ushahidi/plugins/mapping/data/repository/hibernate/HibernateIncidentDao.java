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
import net.frontlinesms.plugins.textforms.data.domain.TextFormResponse;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class HibernateIncidentDao extends BaseHibernateDao<Incident> implements
		IncidentDao {

	public HibernateIncidentDao(){
		super(Incident.class);
	}
	
	public List<Incident> getAllIncidents() {
		return (getCount() == 0) ? new ArrayList<Incident>() : super.getAll();
	}

	public List<Incident> getAllIncidents(int startIndex, int limit) {
		return (getCount() == 0) ? new ArrayList<Incident>() : getAll(startIndex, limit);
	}

	public void saveIncident(Incident incident) throws DuplicateKeyException {		
		super.save(incident);
	}
	
	public void saveIncidentWithoutDuplicateHandling(Incident incident)  {		
		super.saveWithoutDuplicateHandling(incident);
	}

	public void updateIncident(Incident incident) throws DuplicateKeyException {
		super.update(incident);
	}
	
	public void updateIncidentWithoutDuplicateHandling(Incident incident) {
		super.updateWithoutDuplicateHandling(incident);
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

	public void saveIncident(List<Incident> incidents) throws DuplicateKeyException {
		boolean duplicateKeyException = false;
		for(Incident incident : incidents) {
			try {
				super.save(incident);
			} 
			catch (DuplicateKeyException e) {
				e.printStackTrace();
				duplicateKeyException = true;
			}
		}
		if (duplicateKeyException) {
			throw new DuplicateKeyException();
		}
	}
	
	/**
	 * Retrieves the incident whose unique id on the frontend is the one in @param serverId
	 * @param serverId Unique id of the incident on the frontend
	 * @return {@link Incident}
	 */
	public Incident findIncident(long serverId, MappingSetup setup){
		if(getCount() == 0)
			return null;
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Incident.Field.SERVER_ID.getFieldName(), new Long(serverId)));
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
		if(getCount() == 0) {
			return null;
		}
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq("location", location));
			criteria.add(Restrictions.eq(Incident.Field.MAPPING_SETUP.getFieldName(), location.getMappingSetup()));
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
		if(getCount() == 0) {
			return null;
		}
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
		if(getCount() == 0) {
			return new ArrayList<Incident>();
		}
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Incident.Field.MAPPING_SETUP.getFieldName(), setup));
			return super.getList(criteria);
		}
	}

	public void deleteIncidentsWithMapping(MappingSetup setup) {
		for(Incident incident : getAllIncidents(setup)) {
			super.delete(incident);
		}
	}
	
	public Incident getIncidentByTextFormResponse(TextFormResponse textformResponse) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(Incident.Field.TEXTFORM_RESPONSE.getFieldName(), textformResponse));
		return super.getUnique(criteria);
	}
}
