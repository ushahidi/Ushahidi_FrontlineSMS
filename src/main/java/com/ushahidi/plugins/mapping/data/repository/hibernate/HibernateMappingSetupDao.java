package com.ushahidi.plugins.mapping.data.repository.hibernate;

import java.util.ArrayList;
import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class HibernateMappingSetupDao extends BaseHibernateDao<MappingSetup> implements MappingSetupDao {

	/**
	 * Constructor
	 */
	public HibernateMappingSetupDao(){
		super(MappingSetup.class);
	}
	
	/**
	 * Gets all setup items
	 */
	public List<MappingSetup> getAllSetupItems() {
		return (getCount() == 0)? new ArrayList<MappingSetup>() : super.getAll();
	}

	/**
	 * @see {@link MappingSetupDao#saveMappingSetup(MappingSetup)}
	 */
	public void saveMappingSetup(MappingSetup setup) throws DuplicateKeyException {
		//check if there's already a setup item that has already been designated as default
		if(setup.isDefaultSetup()){
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(MappingSetup.Field.DEFAULT.getFieldName(), 
					new Boolean(true)));
			if(super.getList(criteria).size() > 1) {
				throw new DuplicateKeyException();
			}
		}
		
		super.save(setup);
	}

	/**
	 * @see {@link MappingSetupDao#getDefaultSetup()}
	 */
	public MappingSetup getDefaultSetup() {
		if(super.countAll() == 0){
			return null;
		}else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(MappingSetup.Field.DEFAULT.getFieldName(), new Boolean(true)));
			return super.getUnique(criteria);			
		}
	}
	
	/**
	 * @see {@link MappingSetupDao#updateMappingSetup(MappingSetup)}
	 */
	public void updateMappingSetup(MappingSetup setup) throws DuplicateKeyException {
		if(setup.isDefaultSetup()){
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(MappingSetup.Field.DEFAULT.getFieldName(), new Boolean(true)));
			if(super.getList(criteria).size() > 1) {
				throw new DuplicateKeyException();
			}
		}
		super.update(setup);
	}

	/**
	 * @see {@link MappingSetupDao#getCount()}
	 */
	public int getCount() {
		return super.countAll();
	}
	
	/**
	 * Retrieves the mapping setup with the id in @param id
	 * @param id Unique id of the mapping setup
	 * @return {@link MappingSetup}
	 */
	public MappingSetup getMappingSetup(long id){
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.idEq(new Long(id)));
		return super.getUnique(criteria);
	}

	/**
	 * Delete a mapping setup
	 * @param setup MappingSetup
	 */
	public void deleteMappingSetup(MappingSetup setup) {
		super.delete(setup);
	}
}
