package com.ushahidi.plugins.mapping.maps.providers.openstreetmap;

import com.ushahidi.plugins.mapping.maps.providers.AbstractMapProvider;

public abstract class AbstractProvider extends AbstractMapProvider {
	
	public AbstractProvider() {
		super(1, 18);
	}
	
	
	public int tileWidth(){
		return 256;
	}
	
	public int tileHeight(){
		return 256;
	}	

}
