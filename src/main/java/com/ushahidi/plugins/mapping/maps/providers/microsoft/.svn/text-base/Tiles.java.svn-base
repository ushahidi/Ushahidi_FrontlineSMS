package com.ushahidi.plugins.mapping.maps.providers.microsoft;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;

public class Tiles {
	
	private String[] octalStrings = {"000", "001", "010", "011", "100", "101", "110", "111"};

	public static String toMicrosoft(Coordinate coordinate) {
		// Return x, y, z for Yahoo tile column, row, zoom.
		double x = coordinate.col;
		String xS = String.format("%1$#" + (int)coordinate.zoom + "s", Integer.toBinaryString((int)x)).replace(' ', '0');
		double y = coordinate.row;
		String yS = String.format("%1$#" + (int)coordinate.zoom + "s", Integer.toBinaryString((int)y)).replace(' ', '0');
		
		String ret = "";
		for(int i = 0; i < coordinate.zoom; i++) {
			ret += Integer.parseInt(yS.substring(i, i+1) + xS.substring(i, i+1), 2);
		}
		return ret;
	}
	
}
