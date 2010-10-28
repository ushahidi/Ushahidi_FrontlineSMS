package com.ushahidi.plugins.mapping.maps.providers.openstreetmap;

import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.geo.Location;

public class OpenStreetMapProvider extends AbstractProvider {
	
	public OpenStreetMapProvider() {}
	
	public String getZoomString(Coordinate coordinate){
		int zoom = (int)coordinate.zoom;
		return String.format("%d/%d/%d", zoom, (int)coordinate.col, (int)coordinate.row);
	}

	@Override
	public String getTitle() {
		return "Open Street Map Provider";
	}  
	
	@Override
    public String toString() {
    	return "Open Street Map Provider";
    }
	
	@Override
	public String getTileName(Coordinate coordinate) {
		return String.format("OSM_%d_%d_%d.png", coordinate.zoom, (int)coordinate.col, (int)coordinate.row); 
	}
	
	@Override
	public String getTileId(Coordinate coordinate) {
		return String.format("OSM_%d_%d_%d", coordinate.zoom, (int)coordinate.col, (int)coordinate.row); 
	}

	@Override
	public List<String> getTileUrls(Coordinate coordinate) {
		ArrayList<String> urls = new ArrayList<String>();
		urls.add(String.format("http://tile.openstreetmap.org/%s.png", getZoomString(coordinate)));
		return urls;
	}
	
	@Override
	public Coordinate locationCoordinate(Location location){
		double ymax = this.tileHeight() * (1 << getZoomLevel());
		int col = (int)Math.floor((location.lon + 180) / 360 * ymax);
		int row = (int)Math.floor((1 - Math.log(Math.tan(Math.toRadians(location.lat)) + 1 / Math.cos(Math.toRadians(location.lat))) / Math.PI) / 2 * ymax);
		return new Coordinate(row, col, getZoomLevel());
	}
	
	@Override
	public Location coordinateLocation(Coordinate coordinate){
		double y = coordinate.row;
		double x = coordinate.col;
		
		double ymax = this.tileHeight() * (1 << getZoomLevel());
		
		double lat = Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / ymax)));		
		double lon = x / ymax * 360.0 - 180;		
		
		return new Location(lat, lon);
	}
	
}
