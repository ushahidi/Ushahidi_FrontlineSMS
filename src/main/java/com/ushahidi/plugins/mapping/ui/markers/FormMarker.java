package com.ushahidi.plugins.mapping.ui.markers;

import com.ushahidi.plugins.mapping.data.domain.Location;

import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.domain.FormResponse;

/**
 * FormMarker
 * @author dalezak
 *
 */
@SuppressWarnings("serial")
public class FormMarker extends Marker {

	private FormResponse formResponse;
	private Form form;
	
	public FormMarker(FormResponse formResponse, Location location, Form form){
		super("/icons/big_form.png", location);
		this.formResponse = formResponse;
		this.form = form;
	}
		
	public FormResponse getFormResponse(){
		return formResponse;
	}
	
	public Form getForm() {
		return form;
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", formResponse.getSubmitter(), formResponse.getParentForm().getName());
	}
}