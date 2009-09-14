/**
 * 
 */
package net.frontlinesms.plugins.forms.request;

import java.util.Collection;

/**
 * A request for new forms.
 * @author Alex
 */
public class NewFormRequest extends FormsRequestDescription {
	/** IDs of the forms that this user already has. */
	private final Collection<Integer> currentFormMobileIds;

	/**
	 * Create a new instance of this class.
	 * @param currentFormMobileIds
	 */
	public NewFormRequest(Collection<Integer> currentFormMobileIds) {
		this.currentFormMobileIds = currentFormMobileIds;
	}
	
	/** @return {@link #currentFormMobileIds} */
	public Collection<Integer> getCurrentFormMobileIds() {
		return this.currentFormMobileIds;
	}
}
