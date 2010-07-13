package com.ushahidi.plugins.mapping.maps;

import java.awt.Dimension;
import java.awt.Point;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.geo.Location;
import com.ushahidi.plugins.mapping.maps.providers.AbstractMapProvider;

public class MapFactory {

	/**
	 * Return map instance given a provider, center location, zoom value, and dimensions point.
	 * 
	 * @param provider
	 * @param center
	 * @param zoom
	 * @param dimensions
	 * @return
	 */
	public static TiledMap mapByCenterZoom(AbstractMapProvider provider, Location center, int zoom, Dimension dimensions) {
		provider.setZoomLevel(zoom);

		Coordinate centerCoord = provider.locationCoordinate(center);
		Point mapOffset = calculateMapCenter(provider, centerCoord);
				
		return new TiledMap(provider, dimensions, centerCoord, mapOffset);
	}
	
	/**
	 * Based on a provider and center coordinate, returns the coordinate
	 * of an initial tile and its point placement, relative to the map center.
	 * 
	 * @param provider
	 * @param centerCoord
	 * @return
	 */
	public static Point calculateMapCenter(AbstractMapProvider provider, Coordinate centerCoord) {
		// Initial tile coordinate
		Coordinate initTileCoord = centerCoord.container();
		
		// initial tile position, assuming centered tile well in grid
		int initX = (int)((initTileCoord.col - centerCoord.col) * provider.tileWidth());
		int initY = (int)((initTileCoord.row - centerCoord.row) * provider.tileHeight());
		Point initPoint = new Point(initX, initY);   

		return initPoint;			
	}
}
