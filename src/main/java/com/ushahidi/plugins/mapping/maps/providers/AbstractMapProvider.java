package com.ushahidi.plugins.mapping.maps.providers;

import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.geo.AbstractProjection;
import com.ushahidi.plugins.mapping.maps.geo.Location;
import com.ushahidi.plugins.mapping.maps.geo.MercatorProjection;
import com.ushahidi.plugins.mapping.maps.geo.Transformation;

public abstract class AbstractMapProvider {

	protected AbstractProjection projection;
	protected static double MAX_ZOOM = 20;
	protected static double MIN_ZOOM = 1;
	protected Coordinate topLeftOutLimit;
	protected Coordinate bottomRightInLimit;

	public AbstractMapProvider(double minZoom, double maxZoom) {

		MIN_ZOOM = minZoom;
		MAX_ZOOM = maxZoom;
		// see:
		// http://modestmaps.mapstraction.com/trac/wiki/TileCoordinateComparisons#TileGeolocations
		Transformation t = new Transformation(1.068070779e7, 0, 3.355443185e7,
				0, -1.068070890e7, 3.355443057e7);
		projection = new MercatorProjection(26, t);

		topLeftOutLimit = new Coordinate(0, Double.NEGATIVE_INFINITY, minZoom);
		bottomRightInLimit = new Coordinate(1, Double.POSITIVE_INFINITY, 0)
				.zoomTo(maxZoom);
	}

	public abstract List<String> getTileUrls(Coordinate coordinate);

	public abstract String getTileId(Coordinate coordinate);

	public abstract int tileWidth();

	public abstract int tileHeight();
	
	public double getMaxZoom() {
		return MAX_ZOOM;
	}
	
	public double getMinZoom() {
		return MIN_ZOOM;
	}
	
	public AbstractProjection getProjection() {
		return projection;
	}

	public List<Coordinate> getOuterLimits() {
		ArrayList<Coordinate> ret = new ArrayList<Coordinate>();
		ret.add(topLeftOutLimit.copy());
		ret.add(bottomRightInLimit.copy());
		return ret;
	}

	public Coordinate locationCoordinate(Location location) {
		return projection.locationCoordinate(location);
	}

	public Location coordinateLocation(Coordinate coordinate) {
		return projection.coordinateLocation(coordinate);
	}

	/**
	 * Wraps the column around the earth, doesn't touch the row.
	 * 
	 * Row coordinates shouldn't be outside of outerLimits, so we shouldn't need
	 * to worry about them here. 
	 * 
	 * @param coordinate The Coordinate to wrap.
	 * @return The wrapped Coordinate
	 */
	public Coordinate sourceCoordinate(Coordinate coordinate) {
		double wrappedColumn = coordinate.col % Math.pow(2, coordinate.zoom);

		while (wrappedColumn < 0) {
			wrappedColumn += Math.pow(2, coordinate.zoom);
		}

		return new Coordinate(coordinate.row, wrappedColumn, coordinate.zoom);
	}

}
