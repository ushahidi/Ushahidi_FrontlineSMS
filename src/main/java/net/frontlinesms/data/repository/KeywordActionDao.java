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

import net.frontlinesms.data.domain.*;

/**
 * Factory for the creation of KeywordActions.
 * @author Alex
 */
public interface KeywordActionDao {
	/**
	 * Gets all keyword actions of TYPE_REPLY.
	 * @return all {@link KeywordAction} of type {@link KeywordAction#TYPE_REPLY}
	 */
	public abstract Collection<KeywordAction> getReplyActions();
	
	/**
	 * Gets the survey action attached to this keyword.
	 * @return all {@link KeywordAction} of type {@link KeywordAction#TYPE_SURVEY}
	 */
	public abstract Collection<KeywordAction> getSurveysActions();

	/**
	 * Deletes a {@link KeywordAction}.
	 * @param action action to delete
	 */
	public void deleteKeywordAction(KeywordAction action);
	
	/**
	 * Saves a {@link KeywordAction}
	 * @param action action to delete
	 */
	public void saveKeywordAction(KeywordAction action);
}
