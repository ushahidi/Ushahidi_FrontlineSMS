/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.data.repository;

import java.util.Collection;
import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;

/**
 * Factory class for creating instances of the Contact class.
 * @author Alex
 */
public interface ContactDao {
	/** @return all countacts in the system */
	public List<Contact> getAllContacts();
	
	/**
	 * Returns all contacts from a particular start index, with a maximum number of returned contacts set.
	 * @param startIndex index of the first contact to fetch
	 * @param limit max number of contacts to fetch
	 * @return a subset of all the contacts
	 */
	public List<Contact> getAllContacts(int startIndex, int limit);
	
	/**
	 * Retrieves the contact with the specified msisdn, or returns NULL if none exists.
	 * @param phoneNumber a phone number
	 * @return contact with the specified msisdn, or returns <code>null</code> if none exists
	 */
	public Contact getFromMsisdn(String phoneNumber);
	
	/**
	 * Retrieves the contact with the specified name, or returns NULL if none exists.
	 * @param name the name of a contact
	 * @return the contact with the specified name, or returns <code>null</code> if none exists
	 */
	public Contact getContactByName(String name);
	
	/**
	 * Retrieve the page number that the specified group would appear on for
	 * getAllContacts()
	 * @param contact
	 * @param contactsPerPage
	 * @return page number that a particular contact appears on
	 */
	public int getPageNumber(Contact contact, int contactsPerPage);

	/** @return the total number of contacts saved in this dao */
	public int getContactCount();
	
	/**
	 * Deletes a contact from the system
	 * @param contact the contact to delete
	 */
	public void deleteContact(Contact contact);
	
	/**
	 * Saves a contact to the system
	 * @param contact the contact to save
	 * @throws DuplicateKeyException if the contact's phone number is already in use by another contact 
	 */
	public void saveContact(Contact contact) throws DuplicateKeyException;
	
	/**
	 * Updates a contact's details in the data source
	 * @param contact the contact whose details should be updated
	 * @throws DuplicateKeyException if the contact's phone number is already in use by another contact
	 */
	public void updateContact(Contact contact) throws DuplicateKeyException;
	
	/** @return all contacts who are not a member of any groups */
	public Collection<Contact> getUngroupedContacts();
	
	/** @return all contacts who do not have a name set for them */
	public Collection<Contact> getUnnamedContacts();
}
