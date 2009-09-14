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
import java.util.ArrayList;
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
	private List<MapListener> mapListeners;
	private Dimension dimensions;
	private int initialX;
	private int initialY;
	private Queue<Point> scrollQ = new ArrayBlockingQueue<Point>(256);
	
	/** location bounds for the region covered by the map */
	private Location location;
	private List<Incident> incidents;
	private int offsetY = 0;
	private int offsetX = 0;
	private MappingUIController mappingUIController;
	private String offlineMapFile;
	
	public static final Logger LOG = Utils.getLogger(MapBean.class);

	public MapBean() {
		mapListeners = new ArrayList<MapListener>();

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
				
				// Zoom & center on double click
				if(me.getClickCount() == 2) {					
					LOG.debug("Double Clicked");
					map.zoomBy(1);
					map.panTo(me.getX(), me.getY());
					map.draw();
					
					repaint();
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
				scrollQ.add(new Point(initialX - me.getX(), initialY - me.getY()));
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
		LOG.debug("paint");
		if (dimensions == null) {
			dimensions = getSize();
		}

		//Check if the location has been set
		if(location == null){
			LOG.debug("Map location not set");
			LOG.trace("EXIT");
			return;
		}
		
		if (map == null && location != null ) {
			if(offlineMapFile == null)
				map = MapFactory.mapByCenterZoom(
						new MicrosoftRoadProvider(), location, 7, new Point(dimensions.getWidth(), 
								dimensions.getHeight()));
			else{
				try {
					map = MapFactory.mapByCenterZoom(new OfflineProvider(offlineMapFile),
							location, 7, new Point(dimensions.width, dimensions.height));
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
		//Have we been resized?offsetX)offsetX)offsetX)
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
			offsetX += (int)offset.x;
			offsetY += (int)offset.y;
		}
		g.drawImage(img, 0, 0, null);		
		plotIncidents(g);
		//Point point = map.locationPoint(new Location(-1.6791667,29.2227778));
		//System.out.println("Point A: " + point.x + ", " + point.y);
	}

	public void plotIncidents(Graphics g){
		if(location != null && map != null && incidents!=null){			
			//Make the points translucent				
			//g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
			g.setColor(Color.RED);
			for(Incident incident: incidents){
				double lat = incident.getLocation().getLatitude();
				double lon = incident.getLocation().getLongitude();
				Point incidentPoint = map.locationPoint(new Location(lat, lon));								
				g.fillOval((int)incidentPoint.x + offsetX, (int)incidentPoint.y + offsetY, 25, 25);
			}
		}		
	}
	
	public synchronized void addMapListener(MapListener listener) {
		mapListeners.add(listener);
	}

	public synchronized void removeMapListener(MapListener listener) {
		mapListeners.remove(listener);
	}

	private synchronized void firePointSelected(Point p) {
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		LOG.debug("Image Update");
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
}
