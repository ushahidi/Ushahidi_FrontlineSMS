package com.ushahidi.plugins.mapping.maps.providers.openstreetmap;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;

public class Tiles {
	public static Coordinate toOpenStreetMap(Coordinate coordinate){
		double r = coordinate.row;
		double c = coordinate.col;
		int z = 18 - coordinate.zoom;
		
		return new Coordinate(r, c, z);
	}
}
