package com.ushahidi.plugins.mapping.maps.geo;

import com.ushahidi.plugins.mapping.maps.core.Point;

public class Transformation {
	public double ax;
	public double bx;
	public double cx;
	public double ay;
	public double by;
	public double cy;
	
	public Transformation(double ax, double bx, double cx, double ay,
			double by, double cy) {
		super();
		this.ax = ax;
		this.bx = bx;
		this.cx = cx;
		this.ay = ay;
		this.by = by;
		this.cy = cy;
	}

	public Point transform(Point point) {
		return new Point(ax*point.x + bx*point.y + cx, 
				ay*point.x + by*point.y + cy);
	}
	
	public Point untransform(Point point) {
		return new Point((point.x*by - point.y*bx - cx*by + cy*bx) / (ax*by - ay*bx),
                (point.x*ay - point.y*ax - cx*ay + cy*ax) / (bx*ay - by*ax));
	}
}
