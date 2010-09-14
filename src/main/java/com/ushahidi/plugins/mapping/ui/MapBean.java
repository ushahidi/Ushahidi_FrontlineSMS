/* Copyright 2008 Robert Bajzat. All rights reserved. GPL v3. Use is subject to license terms. */
package com.ushahidi.plugins.mapping.ui;

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
import java.util.ArrayList;
import java.util.List;

import thinlet.CustomComponent;

import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.maps.TiledMap;
import com.ushahidi.plugins.mapping.maps.MapFactory;
import com.ushahidi.plugins.mapping.maps.geo.Location;
import com.ushahidi.plugins.mapping.maps.providers.MapProvider;
import com.ushahidi.plugins.mapping.maps.providers.openstreetmap.OpenStreetMapProvider;
import com.ushahidi.plugins.mapping.maps.providers.offline.OfflineProvider;
import com.ushahidi.plugins.mapping.utils.MappingLogger;

@SuppressWarnings("serial")
public class MapBean extends CustomComponent implements ImageObserver {

	public static MappingLogger LOG = MappingLogger.getLogger(MapBean.class);
	
    private TiledMap map = null;

    private Image image = null;
    private final List<MapListener> mapListeners = new ArrayList<MapListener>();
    private Dimension dimensions;

    private MapDragListener mouseListener = new MapDragListener();

    private MapProvider mapProvider;
    
    /** location bounds for the region covered by the map */
    private Location location;
    /** Incidents to be plotted on the map */
    private List<Incident> incidents;
    /** Instance of the UI controller for the mapping plugin */
    private MapPanelHandler mapPanelHandler;
    /** Name of the file containing the offline maps*/
    private String offlineMapFile;

    private static final int DEFAULT_ZOOM_LEVEL = 7;
    private static final int DEFAULT_POINT_SIZE = 18;
    private int pointSize = DEFAULT_POINT_SIZE;
    private int zoomLevel = DEFAULT_ZOOM_LEVEL;

    public MapBean() {
        //Trap for mouse motion on the map
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);
    }
    
    public void setMapProvider(MapProvider mapProvider) {
    	this.mapProvider = mapProvider;
    	destroyMap();
    	map = null;
    	repaint();
    }
    
    public MapProvider getMapProvider() {
    	if (this.mapProvider == null) {
    		//Default to Open Street Map
    		this.mapProvider = new OpenStreetMapProvider();
    	}
    	return this.mapProvider;
    }

    public void setOfflineMapFile(String fileName){
        this.offlineMapFile = fileName;
    }

    public synchronized void setIncidents(List<Incident> incidents){
    	this.incidents = incidents;
        repaint();
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
        }
        else{
            location = new Location(latitude, longitude);
        }
    }
    
    public synchronized void setLocationAndZoomLevel(double longitude, double latitude, int zoom){		
        if(location != null){
            map = null;
            zoomLevel = zoom;
            location = new Location(latitude, longitude);
            repaint();
        }
        else{
        	zoomLevel = zoom;
            location = new Location(latitude, longitude);
        }
    }

    /**
     * Sets the MappingUIController
     * 
     * @param controller
     */
    public synchronized void setMapPanelHandler(MapPanelHandler mapPanelHandler){
        this.mapPanelHandler = mapPanelHandler;
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
        if (dimensions == null) {
            dimensions = getSize();
        }
        if(location == null){
            return;
        }
        if (map == null && location != null) {
            if(offlineMapFile == null){
                map = MapFactory.mapByCenterZoom(this.getMapProvider(), location, zoomLevel, dimensions);
            }
            else{
                try {
                    map = MapFactory.mapByCenterZoom(new OfflineProvider(offlineMapFile), location, zoomLevel, dimensions);
                } catch (IOException e) {
                    LOG.debug(e);
                }				
            }
            map.setObserver(this);
            image = map.draw();
        }
        if(isResized()) {
            dimensions = getSize();
            map.resize(dimensions.width, dimensions.height);
            image = map.draw();
        }
        g.drawImage(image, 0, 0, null);		
        plotIncidents(g);
    }

    public void plotIncidents(Graphics g){
        if(location != null && map != null && incidents != null){			
            for(Incident incident: incidents){
            	g.setColor(incident.getCategoryColor());
            	if (incident.getLocation() != null) {
            		double latitude = incident.getLocation().getLatitude();
                    double longitude = incident.getLocation().getLongitude();
                    Point incidentPoint = map.locationPoint(new Location(latitude, longitude));
                    g.fillOval((int) incidentPoint.x, (int) incidentPoint.y, pointSize, pointSize);	
            	}
            }
        }		
    }

    public synchronized void addMapListener(MapListener listener) {
    	this.mapListeners.add(listener);
    }

    public synchronized void removeMapListener(MapListener listener) {
    	this.mapListeners.remove(listener);
    }
    
    public synchronized void removeMapListeners() {
    	this.mapListeners.removeAll(this.mapListeners);
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
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
        if (mapPanelHandler != null && map != null){			
            Location location = map.pointLocation(getCursorPosition());
            mapPanelHandler.updateCoordinateLabel(location.lat, location.lon);
        }
    }

    public void setZoomLevel(int zoomLevel){
    	this.zoomLevel = zoomLevel;
        LOG.debug("Zoome Lavel = %d", zoomLevel);
        pointSize = (zoomLevel < map.getZoom()) ? pointSize-2 : 
            ((zoomLevel >= DEFAULT_ZOOM_LEVEL)? DEFAULT_POINT_SIZE : pointSize+2);
        map.zoomTo(zoomLevel, new Point(this.getWidth() / 2, this.getHeight() / 2));
        map.draw();
        repaint();
    }

    //>	HELPERS
    /**
     * @see {@link TiledMap#getZoom()}
     */
    public int getZoomLevel(){
        return map.getZoom();
    }

    /**
     * Shuts down the map
     */
    public void destroyMap(){
        if(map != null) {
            map.destroy();
        }
    }

    /**
     * Private class to handle the map dragging events
     * Credit To: http://mappanel.sourceforge.net 
     */
    private class MapDragListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener{
    //> PROPERTIES
        /** Saves the current screen coordinates when the mouse is moved or dragged */
        private Point mouseCoords;
        /** Saves the current screen coordinates when the mouse is clicked or pressed */
        private Point downCoords;
        
    //> CONSTRUCTOR
        public MapDragListener(){
            mouseCoords = new Point();
        }

        public void mouseClicked(MouseEvent e){
            //LOG.info("Zooming map");
            // Increase the current zoom level by 1
            int zoom = map.getZoom() + 1;
            
            // Initiate zoom
            setZoomLevel(zoom);
            for (MapListener mapListener : mapListeners) {
            	mapListener.mapZoomed(zoom);
            }
        }

        public void mousePressed(MouseEvent e){
            // If the click count >= 2, zoom the map
            if(e.getClickCount() >= 2){
                mouseClicked(e);
                return;
            }
            // Save the screen coordinates of the clicked location
            downCoords = e.getPoint();

            for (MapListener mapListener : mapListeners) {
                Location location = map.pointLocation(e.getPoint());
                mapListener.pointSelected(location.lat, location.lon);
            }
        }

        public void mouseReleased(MouseEvent e){            
            downCoords = null;
        }

        public void mouseMoved(MouseEvent e){
            handlePosition(e);
            
            // Update display of the current map coordinates on the UI
            updateCoordinateDisplay();
        }

        public void mouseDragged(MouseEvent e){
            handlePosition(e);
            handleDrag(e);
        }

        public void mouseExited(MouseEvent e){

        }

        public void mouseEntered(MouseEvent e){
            super.mouseEntered(e);
        }

        public void mouseWheelMoved(MouseWheelEvent e){
            LOG.debug("Mouse wheel moved");
        }

        /**
         * Saves the current position of the mouse on the map canvas for purposes
         * of updating the coordinate display on the UI
         * 
         * @param e
         */
        private void handlePosition(MouseEvent e){
            mouseCoords = e.getPoint();
        }

        /**
         * Drags the map across the canvas
         * A drag is only initiated if the (x,y) values in @param e are different
         * from the ones in {@link #downCoords}
         * 
         * @param e {@link java.awt.event.MouseEvent} reference
         */
        private void handleDrag(MouseEvent e){
            if(downCoords != null){
                int tx = downCoords.x  - e.getX();
                int ty = downCoords.y - e.getY();
                
                // Save the current position to prevent extra dragging
                downCoords = e.getPoint();
                
                // Only pan the map if the change is non-zero
                if(tx != 0 && ty != 0){
                    //LOG.debug("Panning map");
                    map.panBy(tx, ty);
                    repaint();
                }
            }
        }
    }
}
