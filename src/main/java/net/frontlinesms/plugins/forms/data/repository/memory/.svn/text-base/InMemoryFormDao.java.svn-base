/**
 * 
 */
package net.frontlinesms.plugins.forms.data.repository.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.repository.FormDao;

/**
 * In-memory implementation of {@link FormDao}.
 * @author Alex
 */
public class InMemoryFormDao implements FormDao {
	/** All forms managed by this DAO */
	private HashSet<Form> allForms = new HashSet<Form>();

	/** @see FormDao#getFormsForUser(Contact, Collection) */
	public Collection<Form> getFormsForUser(Contact contact, Collection<Integer> currentFormMobileIds) {
		HashSet<Form> forms = new HashSet<Form>();
		for(Form f : this.allForms.toArray(new Form[0])) {
			if(f.getPermittedGroup().getAllMembers().contains(contact) && !currentFormMobileIds.contains(f.getMobileId())) {
				forms.add(f);
			}
		}
		return forms;
	}

	/** @see FormDao#getFromMobileId(int) */
	public Form getFromMobileId(int mobileId) {
		for(Form f : this.allForms.toArray(new Form[0])) {
			if(f.isFinalised() && f.getMobileId() == mobileId) {
				return f;
			}
		}
		return null;
	}

	/** @see FormDao#saveForm(Form) */
	public void saveForm(Form form) {
		this.allForms.add(form);
	}

	/** @see FormDao#deleteForm(Form) */
	public void deleteForm(Form form) {
		this.allForms.remove(form);
	}
	
	/** @see FormDao#getAllForms() */
	public Collection<Form> getAllForms() {
		return Collections.unmodifiableSet(this.allForms);
	}
	
	/** @see FormDao#finaliseForm(Form) */
	public void finaliseForm(Form form) throws IllegalStateException {
		TreeSet<Integer> usedMobileIds = new TreeSet<Integer>();
		for(Form f : allForms) {
			usedMobileIds.add(f.getMobileId());
		}
		
		int nextMobileId = usedMobileIds.size();
		for(int id=0; id<nextMobileId; ++id) {
			if(!usedMobileIds.contains(id)) {
				nextMobileId = id;
			}
		}
		
		form.setMobileId(nextMobileId);
	}
}
