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
import net.frontlinesms.data.domain.Group;

/**
 * Data Access Object for {@link Group}s
 * @author Alex
 */
public interface GroupDao {
	/** @return all groups */
	public List<Group> getAllGroups();
	
	/**
	 * Fetches a list of groups in a specific range
	 * @param startIndex index of the first group to fetch
	 * @param limit max number of groups to fetch
	 * @return all groups with the supplied range.
	 */
	public List<Group> getAllGroups(int startIndex, int limit);
	
	/**
	 * Gets all groups with the specified parent.
	 * @param parent
	 * @return a list of groups with the specified parent
	 */
	public Collection<Group> getChildGroups(Group parent);
	
	/**
	 * Retrieve the page number that the specified group would appear on for
	 * getAllGroups()
	 * @param group
	 * @param groupsPerPage
	 * @return page number that a group appears on
	 */
	public int getPageNumber(Group group, int groupsPerPage);
	
	/** @return Total number of groups */
	public int getGroupCount();
	
	/**
	 * Retrieves the group with the specified name, or returns NULL if none exists.
	 * @param name the name of the group
	 * @return group with the requested name, or <code>null</code> if none exists
	 */
	public Group getGroupByName(String name);
	
	/**
	 * Deletes a group and its subgroups, optionally deleting members too.
	 * @param group the group to delete
	 * @param destroyContacts <code>true</code> to delete all members of the group and its subgroups; <code>false</code> otherwise
	 */
	public void deleteGroup(Group group, boolean destroyContacts);

	/**
	 * Saves a group to the data source
	 * @param group the group to save
	 * @throws DuplicateKeyException if a group already exists at this level with the supplied name
	 */
	public void saveGroup(Group group) throws DuplicateKeyException;
	
	/**
	 * Saves updates to a group to the data source
	 * @param group the group to update
	 */
	public void updateGroup(Group group);
}
