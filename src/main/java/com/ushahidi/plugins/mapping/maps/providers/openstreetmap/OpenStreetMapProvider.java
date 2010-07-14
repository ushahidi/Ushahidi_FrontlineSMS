package com.ushahidi.plugins.mapping.maps.providers.openstreetmap;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.maps.TileRequest;
import com.ushahidi.plugins.mapping.maps.TiledMap;
import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.geo.Location;

public class OpenStreetMapProvider extends AbstractProvider {
//> CONSTANTS
	/** Tile servers for OpenStreetMaps */
	/*
	private static final String[] SERVERS = new String[]{
		"http://tile.openstreetmap.org/",
		"http://tah.openstreetmap.org/Tiles/tile/"
	};
	*/
	
	public String getZoomString(Coordinate coordinate){
		int zoom = (int)coordinate.zoom;
		
		return String.format("%d/%d/%d", zoom, (int)coordinate.col, (int)coordinate.row);
	}

	@Override
	public String getTileId(Coordinate coordinate) {
		return "OPENSTREET_MAP" + getZoomString(coordinate);
	}

	@Override
	public List<String> getTileUrls(Coordinate coordinate) {
		ArrayList<String> ret = new ArrayList<String>();
		//int tileServer = (int)((Math.random() * 10) % 2);
		
		ret.add(String.format("http://tile.openstreetmap.org/%s.png", getZoomString(coordinate)));
		return ret;
	}
	
	@Override
	public Coordinate locationCoordinate(Location location){
		//int z = (int)projection.getZoom();
		
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
