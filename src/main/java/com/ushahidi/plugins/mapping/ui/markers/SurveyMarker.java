package com.ushahidi.plugins.mapping.ui.markers;

import com.ushahidi.plugins.mapping.data.domain.Location;

import net.frontlinesms.plugins.surveys.data.domain.SurveyResponse;

/**
 * SurveyMarker
 * @author dalezak
 *
 */
@SuppressWarnings("serial")
public class SurveyMarker extends Marker {

	private SurveyResponse surveyResponse;
	
	public SurveyMarker(SurveyResponse surveyResponse, Location location){
		super("/icons/big_survey.png", location);
		this.surveyResponse = surveyResponse;
	}
		
	public SurveyResponse getSurveyResponse(){
		return surveyResponse;
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", surveyResponse.getContactPhoneNumber(), surveyResponse.getSurveyName());
	}
}