/**
 * 
 */
package net.frontlinesms.plugins.forms.request;

import java.util.Set;

/**
 * A request containing details of forms filled in.
 * @author Alex
 */
public class DataSubmissionRequest extends FormsRequestDescription {
	/** Set of data submitted in this request. */
	private final Set<SubmittedFormData> submittedData;

	/**
	 * Create a new instance of this class.
	 * @param submittedData
	 */
	public DataSubmissionRequest(Set<SubmittedFormData> submittedData) {
		super();
		this.submittedData = submittedData;
	}

	/**
	 * @return the submittedData
	 */
	public Set<SubmittedFormData> getSubmittedData() {
		return submittedData;
	}
}
