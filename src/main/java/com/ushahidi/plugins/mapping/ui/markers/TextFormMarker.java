package com.ushahidi.plugins.mapping.ui.markers;

import com.ushahidi.plugins.mapping.data.domain.Location;

import net.frontlinesms.plugins.textforms.data.domain.TextFormResponse;

/**
 * TextFormMarker
 * @author dalezak
 *
 */
@SuppressWarnings("serial")
public class TextFormMarker extends Marker {

	private TextFormResponse textformResponse;
	
	public TextFormMarker(TextFormResponse textformResponse, Location location){
		super("/icons/big_textform.png", location);
		this.textformResponse = textformResponse;
	}
		
	public TextFormResponse getTextFormResponse(){
		return textformResponse;
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", textformResponse.getContactPhoneNumber(), textformResponse.getTextFormName());
	}
}