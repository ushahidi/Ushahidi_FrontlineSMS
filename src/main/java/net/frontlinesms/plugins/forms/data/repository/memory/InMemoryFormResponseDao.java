/**
 * 
 */
package net.frontlinesms.plugins.forms.data.repository.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.domain.FormResponse;
import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;

/**
 * In-memory implementation of {@link FormResponseDao}.
 * @author Alex
 */
public class InMemoryFormResponseDao implements FormResponseDao {
	/** All responses ever saved. */
	private HashSet<FormResponse> allResponses = new HashSet<FormResponse>();
	
	/** @see FormResponseDao#getFormResponseCount(Form) */
	public int getFormResponseCount(Form form) {
		return getFormResponses(form).size();
	}

	/** @see FormResponseDao#getFormResponses(Form, int, int) */
	public List<FormResponse> getFormResponses(Form form, int startIndex, int limit) {
		List<FormResponse> responses = getFormResponses(form);
		return responses .subList(startIndex, Math.min(responses.size(), startIndex+limit));
	}

	/** @see FormResponseDao#saveResponse(FormResponse) */
	public void saveResponse(FormResponse formResponse) {
		this.allResponses.add(formResponse);
	}

	/**
	 * Gets all responses for a form.
	 * @param form the form whose responses to fetch
	 * @return all responses for the supplied form
	 */
	private List<FormResponse> getFormResponses(Form form) {
		ArrayList<FormResponse> responses = new ArrayList<FormResponse>();
		for(FormResponse response : this.allResponses.toArray(new FormResponse[0])) {
			if(response.getParentForm().equals(form)) {
				responses.add(response);
			}
		}
		return responses;
	}
}
