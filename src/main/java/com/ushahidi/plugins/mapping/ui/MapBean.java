/* Copyright 2008 Robert Bajzat. All rights reserved. GPL v3. Use is subject to license terms. */
package com.ushahidi.plugins.mapping.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

import thinlet.CustomComponent;

import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.maps.Map;
import com.ushahidi.plugins.mapping.maps.MapFactory;
import com.ushahidi.plugins.mapping.maps.core.Point;
import com.ushahidi.plugins.mapping.maps.geo.Location;
import com.ushahidi.plugins.mapping.maps.providers.microsoft.MicrosoftRoadProvider;
import com.ushahidi.plugins.mapping.maps.providers.offline.OfflineProvider;

@SuppressWarnings("serial")
public class MapBean extends CustomComponent implements ImageObserver {

	private Map map = null;
	public Map getMap() {
		return map;
	}

	private Image img = null;
	private MapListener mapListener;
	private Dimension dimensions;
	private int initialX;
	private int initialY;
	private Queue<Point> scrollQ = new ArrayBlockingQueue<Point>(256);
		
	/** location bounds for the region covered by the map */
	private Location location;
	/** Incidents to be plotted on the map */
	private List<Incident> incidents;
	private MappingUIController mappingUIController;
	/** Name of the file containing the offline maps*/
	private String offlineMapFile;
	
	private static final int DEFAULT_ZOOM_LEVEL = 7;
	private static final int DEFAULT_POINT_SIZE = 25;
	private int pointSize = DEFAULT_POINT_SIZE;
	
	public static final Logger LOG = Utils.getLogger(MapBean.class);

	public MapBean() {

		//Trap for mouse motion on the map
		addMouseMotionListener(new MouseMotionAdapter(){ 
			public void mouseMoved(MouseEvent me){				
				updateCoordinateDisplay(me.getX(), me.getY());
			}
		});
		
		addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent me) {
				LOG.debug("Mouse Pressed");
				initialX = me.getX();
				initialY = me.getY();
				
				if(mapListener != null){
					Location loc = map.pointLocation(new Point(initialX, initialY));
					mapListener.pointSelected(loc.lat, loc.lon);					
				}
			}

			public void mouseReleased(MouseEvent me) { 
			}

			// TODO: Change mouse cursor to a hand for dragging, crosshair for
			// selecting point
			// FIXME: Figure out changes to thinlet to allow cursor change
			public void mouseEntered(MouseEvent me) {
				
			}

			public void mouseExited(MouseEvent me) {
				
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent me) {
				LOG.debug("Mouse Dragged");
				scrollQ.add(new Point(me.getX() - initialX, me.getY()-initialY));
				initialX = me.getX();
				initialY = me.getY();
				repaint();
			}
		});
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(640, 640);
	}

	private boolean isResized() {
		return dimensions != getSize();
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
			if(offlineMapFile == null)
				map = MapFactory.mapByCenterZoom(
						new MicrosoftRoadProvider(), location, DEFAULT_ZOOM_LEVEL, new Point(dimensions.getWidth(), 
								dimensions.getHeight()));
			else{
				try {
					map = MapFactory.mapByCenterZoom(new OfflineProvider(offlineMapFile),
							location, DEFAULT_ZOOM_LEVEL, new Point(dimensions.width, dimensions.height));
				} catch (IOException e) {
					LOG.debug(e);
				}				
			}
			
			//Set the map center			
			initialX  = (int)map.getMapCenter().x;
			initialY  = (int)map.getMapCenter().y;
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

		//Animate queued scroll events
		while(!scrollQ.isEmpty()) {
			Point offset = scrollQ.remove();
			map.panBy(offset.x, offset.y);
			map.draw();
			g.drawImage(img, 0, 0, null);
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
				// g.drawImage(new IncidentMarker(this, incident),(int)incidentPoint.x,
				// (int)incidentPoint.y, this);
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

	private synchronized void firePointSelected(Point p) {
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		//LOG.debug("Image Update");
		repaint();
		return true;
	}
	
	/**
	 * Initializes the static location property
	 * 
	 * @param longitude Longitude of the area covered
	 * @param latitude Latitude of the are covered
	 */
	public synchronized void setLocation(double longitude, double latitude){
		if(location == null)
			location = new Location(latitude, longitude);
	}
	
	public synchronized void setIncidents(List<Incident> incidents){
		if(this.incidents != null){
			this.incidents = incidents;
			repaint();
		}else
			this.incidents = incidents;
	}
	
	/**
	 * Sets the MappingUIController
	 * 
	 * @param controller
	 */
	public synchronized void setMappingUIController(MappingUIController controller){
		this.mappingUIController = controller;
	}
	
	/**
	 * Updates the mapping tab with the current geographical coordinates
	 * 
	 * @param x Current x position of the mouse
	 * @param y Current y position of the mouse
	 */
	public void updateCoordinateDisplay(int x, int y){
		if(mappingUIController != null && map != null){
			Location location = map.pointLocation(new Point(x,y));
			mappingUIController.updateCoordinateLabel(location.lat, location.lon);
		}
	}
	
	public void setOfflineMapFile(String fileName){
		this.offlineMapFile = fileName;
	}
	
	public void setZoomValue(double zoomVal){
		LOG.debug("ZOOM VAL = " + zoomVal);
		pointSize = (zoomVal < map.getZoomLevel())? pointSize-4 : 
			((zoomVal >= DEFAULT_ZOOM_LEVEL)? DEFAULT_POINT_SIZE : pointSize+4);
		map.zoomBy(zoomVal-map.getZoomLevel());
		map.draw();
		repaint();
	}
}
