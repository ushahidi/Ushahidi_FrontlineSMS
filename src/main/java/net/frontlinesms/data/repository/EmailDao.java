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

import net.frontlinesms.data.Order;
import net.frontlinesms.data.domain.*;

/**
 * Data access object interface for {@link Email}
 * @author Carlos Eduardo Genz
 * <li> kadu(at)masabi(dot)com
 * @author Alex Anderson
 */
public interface EmailDao {
	/**
	 * Gets all emails within a specified range, sorted as required.
	 * @param sortBy Email Field to sort the results by
	 * @param order direction to order results in
	 * @param startIndex the result index of the emails to recover
	 * @param limit the maximum number of emails to recover
	 * @return all emails within the specified range
	 */
	public Collection<Email> getEmailsWithLimit(Email.Field sortBy, Order order, int startIndex, int limit);
	
	/**
	 * Gets all emails wihtin a specified range
	 * @param startIndex the result index of the emails to recover
	 * @param limit the maximum number of emails to recover
	 * @return a collection of emails within the specified ranges
	 */
	public Collection<Email> getEmailsWithLimitWithoutSorting(int startIndex, int limit);
	
	/**
	 * Gets all email with particular statuses.
	 * @param status
	 * @return all emails with the supplied statuses 
	 */
	public Collection<Email> getEmailsForStatus(Integer[] status);
	
	/** @return all emails */
	public Collection<Email> getAllEmails();
	
	/** @return number of emails in the data source */
	public int getEmailCount();
	
	/**
	 * Delete an email from the data source. 
	 * @param email email to delete.
	 */
	public void deleteEmail(Email email);

	/**
	 * Save this email to the data source.
	 * @param email the email to save
	 */
	public void saveEmail(Email email);

	/**
	 * Update this email to the data source.
	 * @param email the email to update
	 */
	public void updateEmail(Email email);
}
