package com.ushahidi.plugins.mapping.data.repository.hibernate;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;

public class HibernateCategoryDao extends BaseHibernateDao<Category> implements
		CategoryDao {

	public HibernateCategoryDao(){
		super(Category.class);
	}
	
	public List<Category> getAllCategories(int startIndex, int limit) {
		return super.getAll(startIndex, limit);
	}

	public List<Category> getAllCategories() {
		return super.getAll();
	}
	
	public List<Category> getAllCategories(MappingSetup setup){
		if(getCount() == 0){
			return null;
		}else{
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

	public void saveCategory(List<Category> categories) {
		// TODO Auto-generated method stub
		
	}

	public int getCount() {
		return super.countAll();
	}
	
	/**
	 * Retrieves the category whose unique id on the frontend is the one in @param frontendId
	 * @param frontendId Unique id of the category on the frontend
	 * @return {@link Category}
	 */
	public Category findCategory(long frontendId, MappingSetup setup){
		if(getCount() == 0)
			return null;
		else{
			DetachedCriteria criteria = super.getCriterion();
			criteria.add(Restrictions.eq(Category.Field.FRONTEND_ID.getFieldName(), new Long(frontendId)));
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

}
