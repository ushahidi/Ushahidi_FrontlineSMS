package com.ushahidi.plugins.mapping.ui;

import com.ushahidi.plugins.mapping.data.domain.Incident;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class IncidentMarker extends BufferedImage implements MouseListener{

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_MARKER_SIZE = 25;
	private Incident incident;
	private int markerSize = DEFAULT_MARKER_SIZE;
	private ImageObserver observer;
	
	public IncidentMarker(ImageObserver observer, Incident incident){
		super(DEFAULT_MARKER_SIZE, DEFAULT_MARKER_SIZE, BufferedImage.TYPE_INT_RGB);
		this.incident = incident;
		this.observer = observer;
		
		Graphics2D graphics = createGraphics();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
		graphics.setColor(incident.getCategoryColor());
		graphics.fillOval(0, 0, markerSize, markerSize);
		
		observer.imageUpdate(this, 0, 0, 0, 0, 0);

	}
		
	public void setMarkerSize(int size){
		this.markerSize = size;	
	}
	
	public Incident getIncident(){
		return incident;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mousePressed(MouseEvent e) {
		System.out.println("clicked");
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	
}