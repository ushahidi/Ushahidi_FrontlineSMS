package com.ushahidi.plugins.mapping.ui.markers;

import com.ushahidi.plugins.mapping.data.domain.Location;

import net.frontlinesms.plugins.forms.data.domain.FormResponse;

/**
 * FormMarker
 * @author dalezak
 *
 */
public class FormMarker extends Marker {

	private static final long serialVersionUID = 1L;
	private FormResponse formResponse;
	
	public FormMarker(FormResponse formResponse, Location location){
		super("/icons/big_form.png", location);
		this.formResponse = formResponse;
	}
		
	public FormResponse getFormResponse(){
		return formResponse;
	}
	
}