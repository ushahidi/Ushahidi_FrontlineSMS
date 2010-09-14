package com.ushahidi.plugins.mapping.maps.providers.microsoft;

import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;

public class MicrosoftRoadProvider extends AbstractProvider {
	public MicrosoftRoadProvider() {}
	
    public List<String> getTileUrls(Coordinate coordinate) {
    	ArrayList<String> ret = new ArrayList<String>();
    	ret.add(String.format("http://r%d.ortho.tiles.virtualearth.net/tiles/r%s.png?g=90&shading=hill", (int)((Math.random() * 10) % 3), getZoomString(sourceCoordinate(coordinate))));
        return ret;
    }
    
	@Override
	public String getTitle() {
		return "Microsoft Virtual Earth Provider (Road)";
	}  
	
    @Override
    public String getTileId(Coordinate coordinate) {
    	return "MICROSOFT_ROAD" + getZoomString(sourceCoordinate(coordinate));
    }
    
    @Override
    public String toString() {
    	return "MICROSFT_ROAD";
    }
}
