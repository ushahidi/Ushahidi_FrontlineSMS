package com.ushahidi.plugins.mapping.maps.providers.microsoft;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.providers.AbstractMapProvider;

public abstract class AbstractProvider extends AbstractMapProvider {
	
	public AbstractProvider() {
		super(MIN_ZOOM, MAX_ZOOM);
		
	}
	
    public String getZoomString(Coordinate coordinate) { 
        return Tiles.toMicrosoft(coordinate);
    }
	
    public int tileWidth() {
        return 256;
    }

    public int tileHeight() {
        return 256;
    }

}
