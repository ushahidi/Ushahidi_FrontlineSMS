package com.ushahidi.plugins.mapping.maps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.core.Point;
import com.ushahidi.plugins.mapping.maps.geo.Location;
import com.ushahidi.plugins.mapping.maps.providers.AbstractMapProvider;
import com.ushahidi.plugins.mapping.maps.providers.offline.OfflineProvider;

public class Map implements TileRequestor {
	AbstractMapProvider provider;
	Point dimensions;
	Coordinate coordinate;
	Point offset;

	public static final int THREAD_POOL_SIZE = 10;
	public static final Logger LOG = Utils.getLogger(Map.class);
	private static final ExecutorService e = Executors
			.newFixedThreadPool(THREAD_POOL_SIZE);
	private BufferedImage image;
	private ImageObserver observer;
	private long updateId;

	private static final java.util.Map<String, TileRequest> tileRequests = Collections
			.synchronizedMap(new HashMap<String, TileRequest>());

	public Map(AbstractMapProvider provider, Point dimensions,
			Coordinate coordinate, Point offset) {
		this.provider = provider;
		this.dimensions = dimensions;
		this.coordinate = coordinate;
		this.offset = offset;
	}

	@Override
	public String toString() {
		return String.format("Map(%s, %s, %s, %s)", provider, dimensions,
				coordinate, offset);
	}

	/**
	 * Get the x,y location on the map image for a given geographical location
	 * @param location Geographical location on the map
	 * @return x,y point on the map image
	 */
	public Point locationPoint(Location location) {
		Point point = new Point(offset.x, offset.y);
		Coordinate coord = provider.locationCoordinate(location).zoomTo(
				coordinate.zoom);

		// distance from the known coordinate offset
		point.x += provider.tileWidth() * (coord.col - coordinate.col);
		point.y += provider.tileHeight() * (coord.row - coordinate.row);

		// because of the center/corner business
		point.x += dimensions.x / 2;
		point.y += dimensions.y / 2;

		return point;
	}

	/**
	 * Get the geographical location on the map image for a given x, y point
	 * @param point x,y point on the map
	 * @return The geographical location of the point
	 */
	public Location pointLocation(Point point) {
		Coordinate hizoomCoord = coordinate.zoomTo(Coordinate.MAX_ZOOM);

		// because of the center/corner business
		point = new Point(point.x - dimensions.x / 2, point.y - dimensions.y
				/ 2);

		// distance in tile widths from reference tile to point
		double xTiles = (point.x - offset.x) / provider.tileWidth();
		double yTiles = (point.y - offset.y) / provider.tileHeight();

		// distance in rows & columns at maximum zoom
		double xDistance = xTiles
				* Math.pow(2, (Coordinate.MAX_ZOOM - coordinate.zoom));
		double yDistance = yTiles
				* Math.pow(2, (Coordinate.MAX_ZOOM - coordinate.zoom));

		// new point coordinate reflecting that distance
		Coordinate coord = new Coordinate(Math.round(hizoomCoord.row
				+ yDistance), Math.round(hizoomCoord.col + xDistance),
				hizoomCoord.zoom);

		coord = coord.zoomTo(coordinate.zoom);

		Location location = provider.coordinateLocation(coord);

		return location;
	}

	public BufferedImage draw() {
		// Draw map out to an Image and return it.
		LOG.debug("START DRAW");

		Coordinate coord = coordinate.copy();
		Point corner = new Point((int) (offset.x + dimensions.x / 2),
				(int) (offset.y + dimensions.y / 2));

		while (corner.x > 0) {
			corner.x -= provider.tileWidth();
			coord = coord.left();
		}

		while (corner.y > 0) {
			corner.y -= provider.tileHeight();
			coord = coord.up();
		}

		// ArrayList<TileRequest> tiles = new ArrayList<TileRequest>();
		if (image == null) {
			image = new BufferedImage((int) dimensions.x, (int) dimensions.y,
					BufferedImage.TYPE_INT_RGB);
		}
		// Graphics2D g = image.createGraphics();
		// TODO: White background is boring. Checked or maybe a loading image
		// will be better...
		// g.setBackground(Color.WHITE);
		// g.fill(new Rectangle((int) dimensions.x, (int) dimensions.y));

		Coordinate rowCoord = coord.copy();
		updateId = System.currentTimeMillis()/1000;
		for (int y = (int) corner.y; y < dimensions.y; y += provider
				.tileHeight()) {
			Coordinate tileCoord = rowCoord.copy();
			for (int x = (int) corner.x; x < dimensions.x; x += provider
					.tileWidth()) {
				Point tilePoint = new Point(x, y);
				TileRequest tile = new TileRequest(provider, tileCoord,
						tilePoint, updateId);
				if (tile.isLoaded()) {
					LOG.debug("Rendering Tile " + tileCoord);
					renderTile(tile, false);
				} else {
					// Check if we've already a queued request for this tile
					// Coordinate
					// If so, just update its offset else queue the tileRequest

					// Draw blank tile
					Graphics2D g = image.createGraphics();
					g.setColor(Color.WHITE);
					g.fillRect((int) tilePoint.x, (int) tilePoint.y, provider
							.tileWidth(), provider.tileHeight());
					String tileKey = provider.getTileId(tileCoord);
					if (tileRequests.containsKey(tileKey)) {
						LOG.debug("Updating tile offset "
								+ tileCoord);
						TileRequest tile2 = tileRequests.get(tileKey);
						//Prevent NullPointerException
						if(tile2 != null){
							tile2.setOffset(tilePoint);
							tile2.setUpdateId(updateId);
						}
					} else {
						LOG.debug("Queueing tile " + tileCoord);
						// Queue tile request
						tile.setRequestor(this);
						tileRequests.put(tileKey, tile);
						e.submit(tile);
					}
				}
				tileCoord = tileCoord.right();
			}
			rowCoord = rowCoord.down();
		}
		LOG.debug("FINISH DRAW");
		return image;
	}

	public void renderTile(TileRequest tile, boolean update) {
		LOG.debug("Rendering tile. Update. " + tile.getCoord());
		String tileKey = provider.getTileId(tile.getCoord());
		if (tileRequests.containsKey(tileKey)) {
			tileRequests.remove(tileKey);
		}
		//Are we still in the viewport after a scroll?
		if(tile.getUpdateId() != updateId) {
			LOG.debug("Tile no longer visible " + tile.getCoord());
			return;
		}
		Graphics2D g = image.createGraphics();
		g.drawImage(tile.getImage(), (int) tile.getOffset().x, (int) tile
				.getOffset().y, null);
		if (observer != null & update) {
			observer.imageUpdate(image, 0, 0, 0, 0, 0);
		}
	}

	public void renderTile(TileRequest tile) {
		renderTile(tile, true);
	}

	public void resize(double x, double y) {
		dimensions = new Point(x, y);
		image = null;
	}

	/**
	 * Shifts the map's offset by the amount of displacement
	 * @param x Displacement on the x axis
	 * @param y Displacement on the y axis
	 */
	public void panBy(double x, double y) {
		//Adjust the offset by the amount of displacement
		this.offset = new Point(offset.x + x, offset.y + y);
	}
	
	public void panTo(double x, double y) {
		Point newCenterPoint = new Point(x, y);

		Coordinate newCenterCoord = provider.locationCoordinate(
				pointLocation(newCenterPoint)).zoomTo(coordinate.zoom);
		Point mapOffset = MapFactory.calculateMapCenter(provider,
				newCenterCoord);
		this.coordinate = newCenterCoord;
		this.offset = mapOffset;
	}

	
	public void zoomBy(double delta) {
		coordinate = coordinate.zoomBy(delta);
		offset = MapFactory.calculateMapCenter(provider, coordinate);
	}

	public void setObserver(ImageObserver observer) {
		this.observer = observer;
	}
	
	/**
	 * Find the top left coordinate of the map
	 * @return Coordinate of the top left tile
	 */
	public Coordinate topLeftCoord() {
		// Find top left corner coordinate
		Coordinate topLeft = coordinate.copy();
		Point corner = new Point((int) (offset.x + dimensions.x / 2),
				(int) (offset.y + dimensions.y / 2));

		while (corner.x > 0) {
			corner.x -= provider.tileWidth();
			topLeft = topLeft.left();
		}

		while (corner.y > 0) {
			corner.y -= provider.tileHeight();
			topLeft = topLeft.up();
		}
		
		return topLeft;
	}
	
	/**
	 * Find bottom right coordinate of the map
	 * @return Coordinate of the bottom right tile
	 */
	public Coordinate btmRightCoord() {
		Coordinate btmRight = coordinate.copy();
		Point corner = new Point((int) (offset.x + dimensions.x / 2),
				(int) (offset.y + dimensions.y / 2));

		while (corner.x < dimensions.x) {
			corner.x += provider.tileWidth();
			btmRight = btmRight.right();
		}

		while (corner.y < dimensions.y) {
			corner.y += provider.tileHeight();
			btmRight = btmRight.down();
		}
		
		return btmRight;
	}
	
	/**
	 * Returns the screen coordinates of the map center
	 * 
	 * @return {@link #offset}
	 */
	public Point getMapCenter(){
		return this.offset;
	}

	public static void main(String[] args) {
		Map m = null;
		try {
			m = MapFactory.mapByCenterZoom(new OfflineProvider("/Users/bmuita/tmp/map.zip"),
					new Location(-1.450040, 36.826172), 13, new Point(600, 600));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			LOG.debug(e1);
		}
		BufferedImage image = m.draw();
		File outFile = new File("/Users/bmuita/tmp/map.png");
		try {
			ImageIO.write(image, "png", outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.debug(e);
		}

	}
	
	public double getZoomLevel(){
		return coordinate.zoom;
	}

}
