package com.ushahidi.plugins.mapping.data.repository.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;

public class HibernateCategoryDao extends BaseHibernateDao<Category> implements CategoryDao {

	public HibernateCategoryDao(){
		super(Category.class);
	}
	
	public List<Category> getAllCategories(int startIndex, int limit) {
		return super.getAll(startIndex, limit);
	}

	public List<Category> getAllCategories() {
		return (getCount() == 0) ? new ArrayList<Category>() : super.getAll();
	}
	
	public List<Category> getAllCategories(MappingSetup setup){
		if(getCount() == 0){
			return new ArrayList<Category>();
		}
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Category.Field.MAPPING_SETUP.getFieldName(), setup));
			return super.getList(criteria);
		}
	}

	public void flush() {
		//Do nothing
	}

	public void saveCategory(Category category) throws DuplicateKeyException {
		super.save(category);
	}

	public void saveCategory(List<Category> categories) throws DuplicateKeyException{
		boolean duplicateKeyException = false;
		for(Category category : categories) {
			try {
				super.save(category);
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
	 * Retrieves the category whose unique id on the frontend is the one in @param serverId
	 * @param serverId Unique id of the category on the frontend
	 * @return {@link Category}
	 */
	public Category findCategory(long serverId, MappingSetup setup){
		if(getCount() == 0) {
			return null;
		}
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Category.Field.SERVER_ID.getFieldName(), new Long(serverId)));
			criteria.add(Restrictions.eq(Category.Field.MAPPING_SETUP.getFieldName(), setup));
			return super.getUnique(criteria);
		}
	}
	
	/**
	 * Retrieves the category with the unique id contained in @param id
	 * @param id Unique id of the category
	 * @return {@link Category}
	 */
	public Category getCategory(long id){
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.idEq(new Long(id)));
		return super.getUnique(criteria);
	}
	
	public void deleteCategoriesWithMapping(MappingSetup setup) {
		for(Category category : getAllCategories(setup)) {
			super.delete(category);
		}
	}

}
