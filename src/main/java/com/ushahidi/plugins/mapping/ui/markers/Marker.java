package com.ushahidi.plugins.mapping.ui.markers;

import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.util.MappingLogger;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Marker
 * @author dalezak
 *
 */
public abstract class Marker extends ImageIcon implements MouseListener {

	private static MappingLogger LOG = MappingLogger.getLogger(Marker.class);
	
	private static final long serialVersionUID = 1L;
	
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
	
	public void mouseClicked(MouseEvent e) {
		LOG.debug("mouseClicked: %s", this.getClass().getSimpleName());
	}

	public void mousePressed(MouseEvent e) {
		LOG.debug("mousePressed: %s", this.getClass().getSimpleName());
	}
	
	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}
	
}