package com.ushahidi.plugins.mapping.maps.geo;

import com.ushahidi.plugins.mapping.maps.core.Point;

public class MercatorProjection extends AbstractProjection {

	public MercatorProjection(double zoom) {
		super(zoom);
	}

	public MercatorProjection(double zoom, Transformation transformation) {
		super(zoom, transformation);
	}
	
	public Point rawProject(Point point) {
		return new Point(point.x, 
				Math.log(Math.tan(0.25 * Math.PI + 0.5 * point.y)));
	}
	
	public Point rawUnproject(Point point) {
		return new Point(point.x,
                2 * Math.atan(Math.pow(Math.E, point.y)) - 0.5 * Math.PI);
	}

}
