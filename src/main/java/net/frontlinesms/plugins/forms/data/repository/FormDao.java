/**
 * 
 */
package net.frontlinesms.plugins.forms.data.repository;

import java.util.Collection;

import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.plugins.forms.data.domain.Form;

/**
 * Data Access Object for {@link Form}.
 * @author Alex
 */
public interface FormDao {
	/**
	 * Get all forms that a user does not already have.
	 * @param contact
	 * @param currentFormMobileIds
	 * @return list of forms to send to this user
	 */
	public Collection<Form> getFormsForUser(Contact contact, Collection<Integer> currentFormMobileIds);

	/**
	 * Get a form from its mobile ID.
	 * @param mobileId
	 * @return {@link Form} with the specified mobile id or <code>null</code> if none could be found.
	 */
	public Form getFromMobileId(int mobileId);

	/**
	 * Saves a form to the data source.
	 * @param form form to save
	 */
	public void saveForm(Form form);

	/**
	 * Updates the form in the data source
	 * @param form the form to update
	 */
	public void updateForm(Form form);

	/**
	 * Deletes a form from the data source.
	 * @param form form to save
	 */
	public void deleteForm(Form form);
	
	/** @return all forms saved in the data source */
	public Collection<Form> getAllForms();
	
	
	/**
	 * Finalise a form to prevent it being edited again.
	 * @param form The form to finalise
	 * @throws IllegalStateException If the form could not be finalised, either because it has no group set, or because the data source has run out of mobile IDs
	 */
	public void finaliseForm(Form form) throws IllegalStateException;
}
