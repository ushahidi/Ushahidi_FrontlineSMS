package com.ushahidi.plugins.mapping.data.repository.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.frontlinesms.data.DuplicateKeyException;

import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;

public class InMemoryCategoryDao implements CategoryDao {

	Set<Category> allCategories = new HashSet<Category>();
	
	public List<Category> getAllCategories(int startIndex, int limit) {
		ArrayList<Category> categories = new ArrayList<Category>();
		categories.addAll(allCategories);
		return categories.subList(startIndex, Math.min(categories.size(), startIndex + limit));
	}

	public List<Category> getAllCategories(){
		ArrayList<Category> categories = new ArrayList<Category>();
		categories.addAll(allCategories);
		return categories;
	}
	
	public List<Category> getAllCategories(MappingSetup setup){
		return null;
	}
	
	public void saveCategory(Category category) throws DuplicateKeyException {
		allCategories.add(category);		
	}
	
	/**
	 * Resets the category list
	 */
	public void flush(){
		allCategories = null;
		allCategories = new HashSet<Category>();
	}

	public void saveCategory(List<Category> categories) {
		allCategories.addAll(categories);
	}

	public int getCount() {
		return allCategories.size();
	}

	public Category findCategory(long serverId, MappingSetup setup) {
		// TODO Auto-generated method stub
		return null;
	}

	public Category getCategory(long id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void deleteCategoriesWithMapping(MappingSetup setup) {
	
	}
}
