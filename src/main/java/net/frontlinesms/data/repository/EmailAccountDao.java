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

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.EmailAccount;

/**
 * Data Access Object for {@link EmailAccount}.
 * @author Alex Anderson
 */
public interface EmailAccountDao {
	/**
	 * Returns all email accounts saved in the data source
	 * @return all email accounts in the data source
	 */
	public Collection<EmailAccount> getAllEmailAccounts();
	
	/**
	 * Delete the supplied account from the data source.
	 * @param account
	 */
	public void deleteEmailAccount(EmailAccount account);
	
	/**
	 * Save the supplied email account in the data source.
	 * @param account the email account to save
	 * @throws DuplicateKeyException if an email account with this name already exists 
	 */
	public void saveEmailAccount(EmailAccount account) throws DuplicateKeyException;
	
	/**
	 * Save changes to an account in the data source.
	 * @param account the email account to update
	 * @throws DuplicateKeyException if an email account with this name already exists 
	 */
	public void updateEmailAccount(EmailAccount account) throws DuplicateKeyException;
}
