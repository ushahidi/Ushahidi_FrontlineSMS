/**
 * 
 */
package net.frontlinesms.plugins.forms.response;

import java.util.Collection;

import net.frontlinesms.plugins.forms.request.SubmittedFormData;


/**
 * @author Alex
 */
public class SubmittedDataResponse implements FormsResponseDescription {
	/** Data ids submitted successfully. */
	private Collection<SubmittedFormData> submittedData;

	/**
	 * Create a new instance of this class.
	 * @param submittedData
	 */
	public SubmittedDataResponse(Collection<SubmittedFormData> submittedData) {
		this.submittedData = submittedData;
	}

	/** @return {@link #submittedData} */
	public Collection<SubmittedFormData> getSubmittedData() {
		return submittedData;
	}
}
