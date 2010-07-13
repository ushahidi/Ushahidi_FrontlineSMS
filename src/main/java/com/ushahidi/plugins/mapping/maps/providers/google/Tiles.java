package com.ushahidi.plugins.mapping.maps.providers.google;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;

public class Tiles {

	public static Coordinate toGoogleRoad(Coordinate coordinate) {
		// Return x, y, z for google road tile column, row, zoom.
		double x = coordinate.col;
		double y = coordinate.row;
		int z = 17 - coordinate.zoom;
		return new Coordinate(x, y, z);
	}

}
