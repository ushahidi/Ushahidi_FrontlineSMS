package com.ushahidi.plugins.mapping.maps.providers.yahoo;

import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;

public class YahooRoadProvider extends AbstractProvider {
    public List<String> getTileUrls(Coordinate coordinate) {
    	ArrayList<String> ret = new ArrayList<String>();
    	ret.add(String.format("http://us.maps2.yimg.com/us.png.maps.yimg.com/png?v=%s&t=m&%s", ROAD_VERSION, getZoomString(sourceCoordinate(coordinate))));
        return ret;
    }
    
    @Override
    public String toString() {
    	return "YAHOO_ROAD";
    }

	@Override
	public String getTileId(Coordinate coordinate) {		
		return "YAHOO_ROAD" + getZoomString(sourceCoordinate(coordinate));
	}    
}
