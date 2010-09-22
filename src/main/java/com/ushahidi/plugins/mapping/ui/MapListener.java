package com.ushahidi.plugins.mapping.ui;

/**
 * MapListener
 * @author dalezak
 *
 */
public interface MapListener {

	/**
	 * Called when a location is clicked on the map.
	 * @param latitude The latitude of the click.
	 * @param longitude The longitude of the click.
	 */
	public void locationSelected(double latitude, double longitude);
	
	/**
	 * Called when a location is hovered on the map
	 * @param latitude The latitude of the click.
	 * @param longitude The longitude of the click.
	 */
	public void locationHovered(double latitude, double longitude);
	
	/** 
	 * This method should be called when the map is zoomed; in or out
	 * @param zoom The new zoom level of the map
	 */
	public void zoomChanged(int zoom);
	
}
