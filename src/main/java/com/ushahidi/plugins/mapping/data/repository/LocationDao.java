package com.ushahidi.plugins.mapping.data.repository;

import java.util.List;
import net.frontlinesms.data.DuplicateKeyException;

import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;

public interface LocationDao {
	
	public void saveLocation(Location location) throws DuplicateKeyException;
	
	public void saveLocations(List<Location> locations);
	
	public List<Location> getAllLocations();
	
	/** Clears the InMemory database */
	public void flush();
	
	/**
	 * Gets the total number of locations
	 * @return
	 */
	public int getCount();
	
	public Location findLocation(long frontedId, MappingSetup setup);
	
	public Location getLocation(long id);
}
