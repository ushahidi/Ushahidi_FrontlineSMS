package com.ushahidi.plugins.mapping.maps.providers.google;

import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;

public class GoogleRoadProvider extends AbstractProvider {
    public List<String> getTileUrls(Coordinate coordinate) {
    	ArrayList<String> ret = new ArrayList<String>();
    	// http://mt3.google.com/vt/v=w2t.99&hl=en&x=615&y=516&z=10&s=G
    	// http://mt2.google.com/vt/v=w2t.99&hl=en&x=615&y=516&z=4
    	ret.add(String.format("http://mt%d.google.com/vt/v=%s&hl=en&%s", (int)((Math.random() * 10) % 3), ROAD_VERSION, getZoomString(sourceCoordinate(coordinate))));
        return ret;
    }
    
    @Override
    public String toString() {
    	return "GOOGLE_ROAD";
    }

	@Override
	public String getTileId(Coordinate coordinate) {
		return "GOOGLE_ROAD" + getZoomString(sourceCoordinate(coordinate));
	}    
}
