package com.ushahidi.plugins.mapping.maps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.geo.Location;
import com.ushahidi.plugins.mapping.maps.providers.MapProvider;
import com.ushahidi.plugins.mapping.util.MappingLogger;

public class TiledMap implements TileRequestor{
//> CONSTANTS
	/** Size of the thread pool for to be used for fetching the tiles */
	public static final int THREAD_POOL_SIZE = 5;
	/** Logger object */
	public static MappingLogger LOG = MappingLogger.getLogger(TiledMap.class);
	/** Thread pool to be used to perform the tile requests */
	private final ExecutorService e = Executors.newFixedThreadPool(5);
	/** Cache for the tile requests */
	protected static final Map<String, TileRequest> tileRequests = Collections.synchronizedMap(new HashMap<String, TileRequest>());
	
//> INSTANCE VARIABLES
	/** Image object to store the map */
	private BufferedImage mapImage;
	/** Instance of the map provider to be used */
	private MapProvider provider;
	/** Dimensions of the map */
	private Dimension mapSize = new Dimension(0, 0);
	/** Coordinates */
	private Coordinate coordinate;
	/** x,y values for the offset; center of the map */
	private Point offset;
	/** Stores the current position of the map; Coordinate mirror */
	private Point mapPosition = new Point(0, 0);
	/** ImageObserver to report updates to {@link #mapImage}*/
	private ImageObserver observer;
	private int zoom;
	private long updateId;


	public TiledMap(MapProvider provider, Dimension dimensions, Coordinate coordinate, Point offset) {
		this.provider = provider;
		this.mapSize = dimensions;
		this.coordinate = coordinate;
		this.offset = offset;
		this.mapPosition = new Point((int)(coordinate.col - dimensions.width/2), (int)(coordinate.row - dimensions.height/2));
		this.zoom = coordinate.zoom;		
	}
	
	public void setMapPosition(int x, int y){
		if(mapPosition.x == x && mapPosition.y == y) {
			return;
		}
		mapPosition.x = x;
		mapPosition.y = y;
	}
	
	public void setMapPosition(Point position){
		setMapPosition(position.x, position.y);
	}
	
	public Point getMapPosition(){
		return mapPosition;
	}
	
	public void setZoom(int zoom){
		if(this.zoom == zoom) {
			return;
		}
		this.zoom = Math.min(zoom, provider.getMaxZoom());
		provider.setZoomLevel(zoom);
	}
	
	public int getZoom(){
		return this.zoom;
	}
	
	public void setObserver(ImageObserver observer) {
		this.observer = observer;
	}

	public void translateMapPosition(int tx, int ty){
		setMapPosition(mapPosition.x + tx, mapPosition.y + ty);
	}
	
	public void setOffset(int x, int y){
		if(offset.x == x && offset.y == y) {
			return;
		}
		offset = new Point(x, y);
	}
	
	public Point getOffset(){
		return this.offset;
	}
	
	public int getXTileCount(){
		return (1 << zoom);
	}
	
	public int getYTileCount(){
		return (1 << zoom);
	}
	
	public int getXMax(){
		return provider.tileWidth() * getXTileCount();
	}
	
	public int getYMax(){
		return provider.tileHeight() * getYTileCount();
	}

	@Override
	public String toString() {
		return String.format("Map(%s, %s, %s, %s)", provider, mapSize, coordinate, offset);
	}

	public void zoomTo(int targetZoom, Point pivot) {
		if(this.zoom == targetZoom )
			return;
		
		// Store the current zoom level
		int oldZoom = getZoom();
		
		// Calculate the difference between the old and target zoom levels
		int delta = ((targetZoom - oldZoom)>=1)?1:-1;
		
		// Prevent further zooming if new zoom level shall be greater than the max zoom level for the map service 
		if((this.zoom + delta) > provider.getMaxZoom()) {
			return;
		}
		
		// Set the new zoom level
		this.zoom = Math.min(provider.getMaxZoom(), (this.zoom + delta));
		provider.setZoomLevel(getZoom());
		
		int dx = pivot.x;
		int dy = pivot.y;
		
		if(delta >= 1){
			setMapPosition(mapPosition.x * 2 + dx, mapPosition.y * 2 + dy);
		}
		else if(delta < 1){
			setMapPosition((mapPosition.x - dx) / 2, (mapPosition.y - dy) / 2);
		}
		
		offset = MapFactory.calculateMapCenter(provider, new Coordinate(mapPosition.y, mapPosition.x, this.zoom));
	}
	
	/**
	 * Get the x,y location on the map image for a given geographical location
	 * 
	 * @param location Geographical location on the map
	 * @return x,y point on the map image
	 */
	public Point locationPoint(Location location) {
		Coordinate coordinate = provider.locationCoordinate(location);
		return new Point((int)(coordinate.col - mapPosition.x), (int)(coordinate.row - mapPosition.y));
	}

	/**
	 * Gets the geographical location on the map image for a given x, y point
	 * 
	 * @param point x,y point on the map
	 * @return The geographical location of the point
	 */
	public Location pointLocation(Point pivot) {		
		Point cursorPosition = new Point(mapPosition.x + pivot.x, mapPosition.y + pivot.y);
		Coordinate coord = new Coordinate(cursorPosition.y, cursorPosition.x, this.zoom);
		return provider.coordinateLocation(coord);
	}

	public BufferedImage draw() {
		// Draw map out to an Image and return it.
		// Create the canvas onto which the map tiles will be drawn
		if(mapImage == null){
			mapImage = new BufferedImage(mapSize.width , mapSize.height, BufferedImage.TYPE_INT_RGB);
		}
		
		updateId = System.currentTimeMillis()/1000;
		
		// Update the map coordinates to reflect the map position
		coordinate.col = mapPosition.x;
		coordinate.row = mapPosition.y;
		coordinate.zoom = getZoom();
		
		int x0 = (int)Math.floor(coordinate.col / provider.tileWidth());
		int y0 = (int)Math.floor(coordinate.row / provider.tileHeight());
		int x1 = (int)Math.ceil((coordinate.col + mapSize.width) / provider.tileWidth());
		int y1 = (int)Math.ceil((coordinate.row + mapSize.height) / provider.tileHeight());
		
		int dy = y0 * provider.tileHeight() - (int)coordinate.row;		
		
		for (int y= y0; y< y1; ++y){
			int dx = x0 * provider.tileWidth() - (int)coordinate.col;
			for (int x = x0; x < x1; ++x){
				Coordinate tileCoord = new Coordinate(y, x, coordinate.zoom);
				Point tilePoint = new Point(dx, dy);
				TileRequest tile = new TileRequest(provider, tileCoord, tilePoint, updateId);
				if (tile.isLoaded()) {
					renderTile(tile, false);
				} else {
					// Draw blank tile
					queueTileRequest(tile, tileCoord, tilePoint);
				}
				dx += provider.tileWidth();
			}
			dy += provider.tileHeight();
		}		

		// Draw the tiles on the canvas
		return mapImage;
	}

	protected void renderTile(TileRequest tile, boolean update) {
		String tileKey = provider.getTileId(tile.getCoord());
		if (tileRequests.containsKey(tileKey)) {
			tileRequests.remove(tileKey);
		}
		//Are we still in the viewport after a scroll?
		if(tile.getUpdateId() != updateId) {
			return;
		}
		Graphics2D g = mapImage.createGraphics();
		g.drawImage(tile.getImage(), (int) tile.getOffset().x, (int) tile.getOffset().y, null);
		if (observer != null & update) {
			observer.imageUpdate(mapImage, 0, 0, 0, 0, 0);
		}
	}

	public void renderTile(TileRequest tile) {
		renderTile(tile, true);
	}
	
	protected void queueTileRequest(TileRequest tile, Coordinate tileCoord, Point tilePoint){
		// Get the graphics context
		Graphics2D g = mapImage.createGraphics();
		
		g.setColor(Color.WHITE);
		g.fillRect((int) tilePoint.x, (int) tilePoint.y, provider.tileWidth(), provider.tileHeight());
		
		String tileKey = provider.getTileId(tileCoord);
		
		if (tileRequests.containsKey(tileKey)) {
			TileRequest tile2 = tileRequests.get(tileKey);
			//Prevent NullPointerException
			if(tile2 != null){
				tile2.setOffset(tilePoint);
				tile2.setUpdateId(updateId);
			}
		} 
		else {
			// Queue tile request
			tile.setRequestor(this);
			tileRequests.put(tileKey, tile);
			e.submit(tile);
		}		
	}

	public void resize(int x, int y) {
		mapSize = new Dimension(x, y);
		mapImage = null;
	}

	/**
	 * Shifts the map's offset by the amount of displacement
	 * @param x Displacement on the x axis
	 * @param y Displacement on the y axis
	 */
	public void panBy(int x, int y) {
		//Adjust the offset by the amount of displacement		
		setOffset(offset.x + x, offset.y + y);
		
		// Adjust the coordinate position of the map
		setMapPosition(mapPosition.x + x, mapPosition.y + y);
	}
	
	
	/**
	 * Find the top left coordinate of the map
	 * @return Coordinate of the top left tile
	 */
	public Coordinate topLeftCoord() {
		// Find top left corner coordinate
		/*Coordinate topLeft = coordinate.copy();
		Point corner = new Point(((int)offset.x + mapSize.width / 2), ((int)offset.y + mapSize.height / 2));

		while (corner.x > 0) {
			corner.x -= provider.tileWidth();
			topLeft = topLeft.left();
		}

		while (corner.y > 0) {
			corner.y -= provider.tileHeight();
			topLeft = topLeft.up();
		}
		*/
		int x0 = (int)Math.floor(mapPosition.x / provider.tileWidth());
		int y1 = (int)Math.ceil((mapPosition.y + mapSize.height) / provider.tileHeight());
		return new Coordinate(y1, x0, getZoom());
		//return topLeft;
	}
	
	/**
	 * Find bottom right coordinate of the map
	 * @return Coordinate of the bottom right tile
	 */
	public Coordinate btmRightCoord() {
		/*
		Coordinate btmRight = coordinate.copy();
		Point corner = new Point((int) (offset.x + mapSize.width / 2), ((int)offset.y + mapSize.height / 2));

		while (corner.x < mapSize.width) {
			corner.x += provider.tileWidth();
			btmRight = btmRight.right();
		}

		while (corner.y < mapSize.height) {
			corner.y += provider.tileHeight();
			btmRight = btmRight.down();
		}
		*/
		
		int y0 = (int)Math.floor(coordinate.row / provider.tileHeight());
		int x1 = (int)Math.ceil((coordinate.col + mapSize.width) / provider.tileWidth());
		
		return new Coordinate(y0, x1, getZoom());
		//return btmRight;
	}
	
	/**
	 * Returns the screen coordinates of the map center
	 * 
	 * @return {@link #offset}
	 */
	public Point getMapCenter(){
		return this.offset;
	}

	public Coordinate getCoordinate(){
		return coordinate;
	}
	
	public Dimension getMapSize(){
		return mapSize;
	}
	
	public MapProvider getProvider(){
		return this.provider;
	}
	
	/**
	 * Shuts down the executor service that fetches the map tiles
	 */
	public void destroy(){
		e.shutdown();
	}

}
