package com.ushahidi.plugins.mapping.maps.providers.yahoo;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.providers.MapProvider;

public abstract class AbstractProvider extends MapProvider {
	
	protected String ROAD_VERSION = "3.52";
	protected String AERIAL_VERSION = "1.7";
	protected String HYBRID_VERSION = "2.2";
	
	public AbstractProvider() {
		super(MIN_ZOOM, MAX_ZOOM);
		
	}
	
    public String getZoomString(Coordinate coordinate) {
    	Coordinate c = Tiles.toYahoo(coordinate);
        return String.format("x=%d&y=%d&z=%d", (int)c.row, (int)c.col, (int)c.zoom);
    }
	
    public int tileWidth() {
        return 256;
    }

    public int tileHeight() {
        return 256;
    }
	
}
