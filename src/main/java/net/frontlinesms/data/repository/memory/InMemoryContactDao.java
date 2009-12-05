package net.frontlinesms.data.repository.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.ContactDao;

/**
 * In-memory implementation of {@link ContactDao}.
 * @author Alex
 */
public class InMemoryContactDao implements ContactDao {
	/** All the contacts who have been saved. */
	private final HashMap<String, Contact> allContacts = new HashMap<String, Contact>();

	/** Gets a list of all the contacts. */
	public List<Contact> getAllContacts() {
		ArrayList<Contact> contactsList = new ArrayList<Contact>();
		contactsList.addAll(this.allContacts.values());
		return Collections.unmodifiableList(contactsList);
	}

	/**
	 * Gets a sublist of all the contacts.
	 * @see ContactDao#getAllContacts(int, int)
	 */
	public List<Contact> getAllContacts(int startIndex, int limit) {
		List<Contact> allContacts = this.getAllContacts();
		return allContacts.subList(startIndex, Math.min(allContacts.size(), startIndex+limit));
	}

	/**
	 * Gets a contact given his name.
	 * @see ContactDao#getContactByName(String)
	 */
	public Contact getContactByName(String name) {
		for(Contact c : allContacts.values()) {
			if(name.equals(c.getName())) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Gets the total number of saved contacts.
	 * @see ContactDao#getContactCount()
	 */
	public int getContactCount() {
		return allContacts.size();
	}

	/** @see ContactDao#getFromMsisdn(String) */
	public Contact getFromMsisdn(String msisdn) {
		for(Contact c : allContacts.values()) {
			if(msisdn.equals(c.getMsisdn())) {
				return c;
			}
		}
		return null;
	}

	/** @see ContactDao#getPageNumber(Contact, int) */
	public int getPageNumber(Contact contact, int contactsPerPage) {
		return getAllContacts().indexOf(contact) / contactsPerPage;
	}

	/** @see ContactDao#saveContact(Contact) */
	public void saveContact(Contact contact) throws DuplicateKeyException {
		if(allContacts.containsKey(contact.getMsisdn())) {
			throw new DuplicateKeyException();
		} else {
			allContacts.put(contact.getMsisdn(), contact);
		}
	}
	
	/** @see ContactDao#deleteContact(Contact) */
	public void deleteContact(Contact contact) {
		for(Group g : contact.getGroups()) {
			g.removeContact(contact);
		}
		allContacts.remove(contact.getMsisdn());
	}
	
	/** @see ContactDao#updateContact(Contact) */
	public void updateContact(Contact contact) throws DuplicateKeyException {
		Contact contactWithThisMsisdn = allContacts.get(contact.getMsisdn());
		if(contactWithThisMsisdn == contact) {
			// no worries, do nothing
		} else if(contactWithThisMsisdn == null) {
			// we need to move the contact
			String oldMsisdn = null;
			for(String msisdn : allContacts.keySet()) {
				if(allContacts.get(msisdn) == contact) {
					oldMsisdn = msisdn;
					break;
				}
			}
			if(oldMsisdn == null) {
				throw new IllegalStateException("Trying to update unsaved contact.");
			}
			allContacts.remove(oldMsisdn);
			allContacts.put(contact.getMsisdn(), contact);
		} else {
			throw new DuplicateKeyException();
		}
	}

	/** @see ContactDao#getUngroupedContacts() */
	public Collection<Contact> getUngroupedContacts() {
		HashSet<Contact> ungrouped = new HashSet<Contact>();
		for(Contact c : allContacts.values().toArray(new Contact[0])) {
			Collection<Group> groups = c.getGroups();
			if(groups.size() == 0) {
				ungrouped.add(c);
			}
		}
		return ungrouped;
	}

	/** @see ContactDao#getUnnamedContacts() */
	public Collection<Contact> getUnnamedContacts() {
		HashSet<Contact> ungrouped = new HashSet<Contact>();
		for(Contact c : allContacts.values().toArray(new Contact[0])) {
			String name = c.getName();
			if(name == null || name.length() == 0) {
				ungrouped.add(c);
			}
		}
		return ungrouped;
	}
}
