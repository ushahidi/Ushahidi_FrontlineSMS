package com.ushahidi.plugins.mapping.maps.geo;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.core.Point;

public abstract class AbstractProjection {
    int zoom;
    Transformation transformation;

    public AbstractProjection(int zoom) {
        this.zoom = zoom;
        this.transformation = new Transformation(1,0,0,0,1,0);
    }

    public AbstractProjection(int zoom, Transformation transformation) {
        this.zoom = zoom;
        this.transformation = transformation;
    }

    public Transformation getTransformation() {
        return transformation;
    }

    public double getZoom() {
        return zoom;
    }

    public Point rawProject(Point p) {
        throw new UnsupportedOperationException();
    }

    public Point rawUnproject(Point p) {
        throw new UnsupportedOperationException();
    }

    public Point project(Point point) {
        point = rawProject(point);
        if(transformation != null) {
            point = transformation.transform(point);
        }
        return point;
    }

    public Point unproject(Point point) {
        if(transformation != null) {
            point = transformation.untransform(point);
        }
        point = rawUnproject(point);
        return point;
    }	

    public Coordinate locationCoordinate(Location location) {
        Point point = new Point(Math.PI * location.lon / 180.0, Math.PI * location.lat / 180.0);
        point = project(point);
        return new Coordinate(point.y, point.x, zoom);				
    }

    public Location coordinateLocation(Coordinate coordinate) {
        coordinate = coordinate.zoomTo(zoom);
        Point point = new Point(coordinate.col, coordinate.row);
        point = unproject(point);
        return new Location(180.0 * point.y / Math.PI, 180.0 * point.x / Math.PI);		
    }
}
