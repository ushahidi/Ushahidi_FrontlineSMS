/**
 * 
 */
package net.frontlinesms.plugins.forms.response;

import java.util.Collection;

import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.plugins.forms.data.domain.Form;

/**
 * Response wrapping forms to send to a user.
 * @author Alex
 */
public class NewFormsResponse implements FormsResponseDescription {
	/** The contact that this response will be sent to. */
	private final Contact contact;
	/** New forms to send to a contact. */
	private final Collection<Form> newForms;

	/**
	 * Create a new response wrapping forms to send.
	 * @param contact
	 * @param newForms
	 */
	public NewFormsResponse(Contact contact, Collection<Form> newForms) {
		this.contact = contact;
		this.newForms = newForms;
	}

	/**
	 * @return the contact
	 */
	public Contact getContact() {
		return contact;
	}

	/**
	 * @return the newForms
	 */
	public Collection<Form> getNewForms() {
		return newForms;
	}

}
