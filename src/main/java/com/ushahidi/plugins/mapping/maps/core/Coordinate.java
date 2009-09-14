package com.ushahidi.plugins.mapping.maps.core;

public class Coordinate implements Comparable<Coordinate> {

	public static final double MAX_ZOOM = 25;

	public double row;
	public double col;
	public double zoom;

	public Coordinate(double row, double col, double zoom) {
		this.row = row;
		this.col = col;
		this.zoom = zoom;
	}

	public Coordinate zoomTo(double destination) {
		return new Coordinate(row * Math.pow(2, destination - zoom), col
				* Math.pow(2, destination - zoom), destination);
	}

	public Coordinate zoomBy(double distance) {
		return new Coordinate(row * Math.pow(2, distance), col
				* Math.pow(2, distance), zoom + distance);
	}

	public Coordinate copy() {
		return new Coordinate(row, col, zoom);
	}

	public Coordinate container() {
		return new Coordinate(Math.floor(row), Math.floor(col), zoom);
	}

	public Coordinate up() {
		return up(1);
	}

	public Coordinate up(double distance) {
		return new Coordinate(row - distance, col, zoom);
	}

	public Coordinate right() {
		return right(1);
	}

	public Coordinate right(double distance) {
		return new Coordinate(row, col + distance, zoom);
	}

	public Coordinate down() {
		return down(1);
	}

	public Coordinate down(double distance) {
		return new Coordinate(row + distance, col, zoom);
	}

	public Coordinate left() {
		return left(1);
	}

	public Coordinate left(double distance) {
		return new Coordinate(row, col - distance, zoom);
	}

	@Override
	public String toString() {
		return String.format("Coordinate (%d, %d, %d)", (int) row, (int) col,
				(int) zoom);
	}

	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj.getClass() == this.getClass()) {
			Coordinate coord = (Coordinate) obj;
			ret = Math.round(col) == Math.round(coord.col) && Math.round(row) == Math.round(coord.row) && zoom == coord.zoom;
		}
		return ret;
	}

	public int compareTo(Coordinate coord) {
		int ret = 0; // Default equals
		if (!equals(coord)) {
			// less than?			
			if (col <= coord.col && row <=  coord.row && zoom <= coord.zoom) {
				ret = -1;
			} else {
				ret = 1;
			}
		}

		return ret;
	}
}
