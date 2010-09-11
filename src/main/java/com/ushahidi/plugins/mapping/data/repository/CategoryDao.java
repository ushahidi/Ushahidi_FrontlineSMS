package com.ushahidi.plugins.mapping.data.repository;

import java.util.List;

import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;

import net.frontlinesms.data.DuplicateKeyException;

public interface CategoryDao {
		
	public List<Category>getAllCategories(int startIndex, int limit);
	
	public List<Category> getAllCategories();
	
	public List<Category> getAllCategories(MappingSetup setup);
	
	public void saveCategory(Category category) throws DuplicateKeyException;
	
	public void saveCategory(List<Category> categories) throws DuplicateKeyException;
	
	public void flush();
	
	public int getCount();
	
	public Category findCategory(long serverId, MappingSetup setup);
	
	public Category getCategory(long id);

	public void deleteCategoriesWithMapping(MappingSetup setup);
}
