/* Copyright 2008 Robert Bajzat. All rights reserved. GPL v3. Use is subject to license terms. */
package com.ushahidi.plugins.mapping.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.List;

import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

import thinlet.CustomComponent;

import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.maps.TiledMap;
import com.ushahidi.plugins.mapping.maps.MapFactory;
import com.ushahidi.plugins.mapping.maps.geo.Location;
import com.ushahidi.plugins.mapping.maps.providers.openstreetmap.OpenStreetMapProvider;
import com.ushahidi.plugins.mapping.maps.providers.offline.OfflineProvider;

@SuppressWarnings("serial")
public class MapBean extends CustomComponent implements ImageObserver {

	private static final Logger LOG = Utils.getLogger(MapBean.class);
	
	private TiledMap map = null;
	
	private Image img = null;
	private MapListener mapListener;
	private Dimension dimensions;
	
	private MapDragListener mouseListener = new MapDragListener();
		
	/** location bounds for the region covered by the map */
	private Location location;
	/** Incidents to be plotted on the map */
	private List<Incident> incidents;
	/** Instance of the UI controller for the mapping plugin */
	private MappingUIController mappingUIController;
	/** Name of the file containing the offline maps*/
	private String offlineMapFile;
	
	private static final int DEFAULT_ZOOM_LEVEL = 7;
	private static final int DEFAULT_POINT_SIZE = 18;
	private int pointSize = DEFAULT_POINT_SIZE;
	

	public MapBean() {

		//Trap for mouse motion on the map
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);		
		addMouseWheelListener(mouseListener);
	}
	
	
	public void setOfflineMapFile(String fileName){
		this.offlineMapFile = fileName;
	}

	public synchronized void setIncidents(List<Incident> incidents){
		if(this.incidents != null){
			this.incidents = incidents;
			repaint();
		}else
			this.incidents = incidents;
	}

	/**
	 * Initializes the static location property
	 * 
	 * @param longitude Longitude of the area covered
	 * @param latitude Latitude of the are covered
	 */
	public synchronized void setLocation(double longitude, double latitude){		
		if(location != null){
			map = null;
			location = new Location(latitude, longitude);
			repaint();
		}else{
			location = new Location(latitude, longitude);
		}
	}

	/**
	 * Sets the MappingUIController
	 * 
	 * @param controller
	 */
	public synchronized void setMappingUIController(MappingUIController controller){
		this.mappingUIController = controller;
	}

	public Dimension getPreferredSize() {
		return new Dimension(640, 640);
	}

	private boolean isResized() {
		return dimensions != getSize();
	}
		
	public TiledMap getMap() {
		return map;
	}	
	
	public Point getCursorPosition(){
		return new Point(mouseListener.mouseCoords.x, mouseListener.mouseCoords.y);
	}
		
	public void paint(Graphics g) {
		//LOG.debug("paint");
		
		if (dimensions == null) {
			dimensions = getSize();
		}

		//Check if the location has been set
		if(location == null){
			LOG.error("Map location not set");
			LOG.trace("EXIT");
			return;
		}
		
		if (map == null && location != null ) {
			if(offlineMapFile == null){
				map = MapFactory.mapByCenterZoom(new OpenStreetMapProvider(), location, DEFAULT_ZOOM_LEVEL, dimensions);
			}else{
				try {
					map = MapFactory.mapByCenterZoom(new OfflineProvider(offlineMapFile), 
							location, DEFAULT_ZOOM_LEVEL, dimensions);
				} catch (IOException e) {
					LOG.debug(e);
				}				
			}
			
			map.setObserver(this);
			img = map.draw();
		}
		//Have we been resized?
		if(isResized()) {
			LOG.debug("Resized");
			dimensions = getSize();
			map.resize(dimensions.width, dimensions.height);
			img = map.draw();
		}

		
		g.drawImage(img, 0, 0, null);		
		plotIncidents(g);
		
	}

	public void plotIncidents(Graphics g){
		if(location != null && map != null && incidents!=null){			
			g.setColor(Color.RED);
			for(Incident incident: incidents){
				double lat = incident.getLocation().getLatitude();
				double lon = incident.getLocation().getLongitude();
				Point incidentPoint = map.locationPoint(new Location(lat, lon));
				g.fillOval((int) incidentPoint.x, (int) incidentPoint.y, pointSize, pointSize);
			}
		}		
	}
	
	public synchronized void addMapListener(MapListener listener) {
		mapListener = listener;
	}

	public synchronized void removeMapListener() {
		mapListener = null;
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		LOG.debug("Image Update");
		repaint();
		return true;
	}
				
	/**
	 * Updates the mapping tab with the current geographical coordinates
	 * 
	 * @param x Current x position of the mouse
	 * @param y Current y position of the mouse
	 */
	public void updateCoordinateDisplay(){
		if(mappingUIController != null && map != null){			
			Location location = map.pointLocation(getCursorPosition());
			mappingUIController.updateCoordinateLabel(location.lat, location.lon);
		}
	}
	
	
	public void zoomMap(int zoomVal){
		LOG.debug("ZOOM VAL = " + zoomVal);
		pointSize = (zoomVal < map.getZoom())? pointSize-2 : 
			((zoomVal >= DEFAULT_ZOOM_LEVEL)? DEFAULT_POINT_SIZE : pointSize+2);
				
		map.zoomTo(zoomVal, new Point(this.getWidth() / 2, this.getHeight() / 2));
		map.draw();
		repaint();
	}
	
//>	HELPERS
	/**
	 * @see {@link TiledMap#getZoom()}
	 */
	public int getCurrentZoomLevel(){
		return map.getZoom();
	}
		
	
	/**
	 * Shuts down the map
	 */
	public void destroyMap(){
		if(map != null)
			map.destroy();
	}
		
	/**
	 * Private class to handle the map dragging events
	 * Credit To: http://mappanel.sourceforge.net 
	 */
	private class MapDragListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener{
		
		private Point mouseCoords;
		private Point downCoords;
		
		public MapDragListener(){
			mouseCoords = new Point();
		}
		
		public void mouseClicked(MouseEvent e){
			System.out.println("click");
			if(e.getClickCount() >= 2){
				zoomMap(map.getZoom() + 1);
			}
		}
		
		public void mousePressed(MouseEvent e){
			downCoords = e.getPoint();

			if(mapListener != null){
				Location location = map.pointLocation(e.getPoint());
				mapListener.pointSelected(location.lat, location.lon);
			}
		}
		
		public void mouseReleased(MouseEvent e){
			handleDrag(e);
			downCoords = null;
		}
		
		public void mouseMoved(MouseEvent e){
			handlePosition(e);
			updateCoordinateDisplay();
		}
		
		public void mouseDragged(MouseEvent e){
			handlePosition(e);
		}
		
		public void mouseExited(MouseEvent e){
			
		}
		
		public void mouseEntered(MouseEvent e){
			super.mouseEntered(e);
		}
		
		public void mouseWheelMoved(MouseWheelEvent e){
			
		}
		
		private void handlePosition(MouseEvent e){
			mouseCoords = e.getPoint();
		}
		
		private void handleDrag(MouseEvent e){
			if(downCoords != null){
				int tx = downCoords.x  - e.getX();
				int ty = downCoords.y - e.getY();
				map.panBy(tx, ty);
				repaint();
			}
		}
	}
}
