package com.ushahidi.plugins.mapping.maps.providers.openstreetmap;

import com.ushahidi.plugins.mapping.maps.providers.MapProvider;

public abstract class AbstractProvider extends MapProvider {
	
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
