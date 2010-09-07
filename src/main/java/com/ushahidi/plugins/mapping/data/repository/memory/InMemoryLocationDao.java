package com.ushahidi.plugins.mapping.data.repository.memory;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;

import net.frontlinesms.data.DuplicateKeyException;

public class InMemoryLocationDao implements LocationDao{
	

	Set<Location> allLocations = new HashSet<Location>();

	public List<Location> getAllLocations() {
		ArrayList<Location> locations = new ArrayList<Location>();
		locations.addAll(allLocations);
		return locations;
	}
	
	public List<Location> getAllLocations(MappingSetup setup){
		return null;
	}

	public void saveLocation(Location location) throws DuplicateKeyException {
		allLocations.add(location);
	}
	
	public void saveLocations(List<Location> locations) {
		allLocations.addAll(locations);
	}
	
	public void flush(){
		allLocations = null;
		allLocations = new HashSet<Location>();
	}

	public int getCount() {
		return allLocations.size();
	}

	public Location findLocation(long frontedId, MappingSetup setup) {
		// TODO Auto-generated method stub
		return null;
	}

	public Location getLocation(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteLocationsWithMapping(MappingSetup setup) {
		
	}
}
