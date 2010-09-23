package com.ushahidi.plugins.mapping.ui.markers;

import com.ushahidi.plugins.mapping.data.domain.Location;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Marker
 * @author dalezak
 *
 */
@SuppressWarnings("serial")
public abstract class Marker extends ImageIcon {

	protected Location location;
	
	protected Marker(String imagePath, Location location){
		super(Marker.class.getResource(imagePath));
		this.location = location;
	}
	
	protected Marker(URL url, Location location){
		super(url);
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public abstract String toString();
	
}