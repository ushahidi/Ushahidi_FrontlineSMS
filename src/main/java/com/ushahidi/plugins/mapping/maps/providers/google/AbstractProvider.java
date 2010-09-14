package com.ushahidi.plugins.mapping.maps.providers.google;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.geo.MercatorProjection;
import com.ushahidi.plugins.mapping.maps.geo.Transformation;
import com.ushahidi.plugins.mapping.maps.providers.MapProvider;

public abstract class AbstractProvider extends MapProvider {
	
	protected String ROAD_VERSION = "w2t.99";
	protected String AERIAL_VERSION = "32";
	protected String HYBRID_VERSION = "w2t.83";
	protected String TERRAIN_VERSION = "app.81";
		
	public AbstractProvider() {
		super(MIN_ZOOM, MAX_ZOOM);
		
		Transformation t = new Transformation(1.068070779e7, 0, 3.355443185e7,
				 0, -1.068070890e7, 3.355443057e7);
		projection = new MercatorProjection(26, t);
	}
	
    public String getZoomString(Coordinate coordinate) {
    	Coordinate c = Tiles.toGoogleRoad(coordinate);
        return String.format("x=%d&y=%d&z=%d", (int)c.row, (int)c.col, (int)c.zoom);
    }
	
    public int tileWidth() {
        return 256;
    }

    public int tileHeight() {
        return 256;
    }
	
}
