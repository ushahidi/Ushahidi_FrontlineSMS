package com.ushahidi.plugins.mapping.maps.providers.yahoo;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;

public class Tiles {

	public static Coordinate toYahoo(Coordinate coordinate) {
		// Return x, y, z for Yahoo tile column, row, zoom.
		double x = coordinate.col;
		double y = Math.pow(2, coordinate.zoom - 1) - coordinate.row - 1;
		double z = 18 - coordinate.zoom;
		return new Coordinate(x, y, z);
	}

}
