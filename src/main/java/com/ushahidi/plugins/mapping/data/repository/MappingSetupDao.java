package com.ushahidi.plugins.mapping.data.repository;

import java.util.List;

import com.ushahidi.plugins.mapping.data.domain.MappingSetup;

import net.frontlinesms.data.DuplicateKeyException;

public interface MappingSetupDao {
	/**
	 * Gets the list of all setup items
	 * @return
	 */
	public List<MappingSetup> getAllSetupItems();
	
	/**
	 * Creates a new mapping setup
	 * @param setup setup item to be added
	 * @throws DuplicateKeyException
	 */
	public void saveMappingSetup(MappingSetup setup) throws DuplicateKeyException;
	
	/**
	 * Gets the default mapping setup. There can only be one default mapping setup
	 * @return {@link MappingSetup}
	 */
	public MappingSetup getDefaultSetup();
	
	/**
	 * Updates a mapping setup
	 * @param setup Setup item to be updated
	 * @throws DuplicateKeyException
	 */
	public void updateMappingSetup(MappingSetup setup) throws DuplicateKeyException;
	
	/**
	 * Gets the total number of MappingSetup items
	 * @return
	 */
	public int getCount();

}
