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

import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.*;

/**
 * Factory for creating Keywords.
 * @author Alex
 *
 */
public interface KeywordDao {
	/** @return an alphabetically-sorted list of all keywords without parents. */
	public List<Keyword> getRootKeywords();
	
	/** @return an alphabetically-sorted list of all keywords. */
	public List<Keyword> getAllKeywords();
	
	/**
	 * Returns all keywords with the supplied range.
	 * @param startIndex index in the total list of keywords of the first keyword to return
	 * @param limit maximum number of keywords to return
	 * @return A subset of the total keywords
	 */
	public List<Keyword> getAllKeywords(int startIndex, int limit);
	
	/**
	 * Gets the page number that a keyword will appear on when getAllKeywords is called.
	 * @param keyword the keyword we are searching for
	 * @param keywordsPerPage number of keywords per page
	 * @return page number that the supplied keyword is found on
	 */
	public int getPageNumber(Keyword keyword, int keywordsPerPage);
		
	/**
	 * Creates all keywords in the hierarchy that do not already exist, and returns the
	 * last keyword in the hierarchy.
	 * @param keywordHierarchy
	 * @param classicMode TODO
	 * @param description description of the lowest keyword in this hierarchy.
	 * @return last keyword in the supplied list.
	 * @throws DuplicateKeyException if all keywords in the hierarchy already exist
	 */
	public abstract Keyword createKeywordsHierarchically(String[] keywordHierarchy, String description, boolean classicMode) throws DuplicateKeyException;

	/**
	 * Get's the lowest-generation keyword from the supplied message text.
	 * E.g. if message text is "masabi join random", will search for keyword
	 * "masabi".  If this is found, will then search it's subkeywords for
	 * "join".  If this is found, will search "join"'s subkeywords for "random",
	 * and if not, will return Keyword object representing "masabi".
	 * 
	 * @param messageText
	 * @return keyword that matches the supplied text, or <code>null</code> if no keyword matched
	 */
	public Keyword getFromMessageText(String messageText);
	
	/** @return the total number of keywords in the system */
	public int getTotalKeywordCount();

	/**
	 * Deletes a keyword from the system.
	 * @param keyword The keyword to be deleted
	 */
	public void deleteKeyword(Keyword keyword);
	
	/**
	 * Saves a keyword in the system
	 * @param keyword the keyword to be saved
	 * @throws DuplicateKeyException
	 */
	public void saveKeyword(Keyword keyword) throws DuplicateKeyException;
}
