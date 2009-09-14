package com.ushahidi.plugins.mapping.maps.core;

public class Point {
	public double x;
	public double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return String.format("Point (%d, %d)", (int)x, (int)y);
	}
	
}
