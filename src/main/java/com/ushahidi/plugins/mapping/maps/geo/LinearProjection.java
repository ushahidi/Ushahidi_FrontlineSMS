package com.ushahidi.plugins.mapping.maps.geo;

import com.ushahidi.plugins.mapping.maps.core.Point;

public class LinearProjection extends AbstractProjection {

	public LinearProjection(int zoom) {
		super(zoom);
	}

	public LinearProjection(int zoom, Transformation transformation) {
		super(zoom, transformation);
	}

	public Point rawProject(Point point) {
		return point;
	}
	
	public Point rawUnproject(Point point) {
		return point;
	}
}
