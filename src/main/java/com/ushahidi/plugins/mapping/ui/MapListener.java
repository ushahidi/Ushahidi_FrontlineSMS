package com.ushahidi.plugins.mapping.ui;

public interface MapListener {

	/**
	 * Called when a point is clicked on the map.
	 * @param lat The latitude of the click.
	 * @param lon The longitude of the click.
	 */
	public void pointSelected(double lat, double lon);
	
}
