/**
 * 
 */
package net.frontlinesms.csv;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Builder class for CSV export row formats.
 * This class is not threadsafe.
 * @author Alex
 */
public final class CsvRowFormat {
//> INSTANCE PROPERTIES
	/** Substitution markers */
	private final List<String> markers = new ArrayList<String>();

//> ACCESSORS
	/**
	 * Add the marker to the row format.
	 * @param marker Marker to add
	 */
	public void addMarker(String marker) {
		this.markers.add(marker);
	}

	/**
	 * @param marker A marker in this row format
	 * @return the index of the marker in the row format, or <code>null</code> if the marker does not occur in this format.
	 */
	public Integer getIndex(String marker) {
		int index = this.markers.indexOf(marker);
		return index == -1 ? null : index;
	}
	
	/**
	 * Check if this format has any markers yet
	 * @return <code>true</code> if markers have been added; <code>false</code> otherwise
	 */
	public boolean hasMarkers() {
		return this.markers.size() > 0;
	}

	/**
	 * Returns a CSV row formatted in the suggested way.
	 * @param markersAndReplacements
	 * @return formatted and escaped row of CSV
	 */
	public List<String> format(String... markersAndReplacements) {
		if((markersAndReplacements.length&1) == 1) throw new IllegalArgumentException("Each marker must have a replacement!  Odd number of markers+replacements provided: " + markersAndReplacements.length);

		LinkedList<String> formattedList = new LinkedList<String>();
		formattedList.addAll(this.markers);
		
		for (int markerAndReplacementIndex = 0; markerAndReplacementIndex < markersAndReplacements.length; markerAndReplacementIndex+=2) {
			String marker = markersAndReplacements[markerAndReplacementIndex];
			String replacement = CsvUtils.escapeValue(markersAndReplacements[markerAndReplacementIndex + 1]);
			for (int markerIndex = 0; markerIndex < formattedList.size(); markerIndex++) {
				if(formattedList.get(markerIndex).equals(marker)) {
					formattedList.set(markerIndex, replacement);
				}
			}
		}
		
		return formattedList;
	}
	
	/** @return the row format string */
	@Override
	public String toString() {
		StringBuilder bob = new StringBuilder();
		
		for(String marker : this.markers) {
			bob.append(',');
			bob.append(marker);
		}
		
		// Every marker is preceded with a ',' so we must remove the first one at this stage
		return bob.toString().substring(1);
	}
}