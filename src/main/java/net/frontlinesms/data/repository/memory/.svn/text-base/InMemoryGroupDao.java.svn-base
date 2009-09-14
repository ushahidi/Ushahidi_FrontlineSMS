/**
 * 
 */
package net.frontlinesms.data.repository.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.Group;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.data.repository.GroupDao;

/**
 * In memory implementation of {@link GroupDao}
 * @author Alex
 */
public class InMemoryGroupDao implements GroupDao {
	/** The contact dao, necessary for keeping interdependencies up to date. */
	private ContactDao contactDao;
	/** All the groups there are */
	private Set<Group> allGroups = new HashSet<Group>();

	/** @see GroupDao#deleteGroup(Group, boolean) */
	public void deleteGroup(Group group, boolean deleteContacts) {
		for(Group child : group.getDirectSubGroups()) {
			deleteGroup(child, deleteContacts);
		}
		if(deleteContacts) {
			for(Contact c : group.getAllMembers()) {
				contactDao.deleteContact(c);
			}
		}
		this.allGroups.remove(group);
	}
	
	/** @see GroupDao#getAllGroups() */
	public List<Group> getAllGroups() {
		ArrayList<Group> groups = new ArrayList<Group>();
		groups.addAll(allGroups);
		return groups;
	}

	/** @see GroupDao#getAllGroups(int, int) */
	public List<Group> getAllGroups(int startIndex, int limit) {
		List<Group> groups = getAllGroups();
		return groups.subList(startIndex, Math.min(groups.size(), startIndex + limit));
	}

	/** @see GroupDao#getGroupByName(java.lang.String) */
	public Group getGroupByName(String name) {
		for(Group g : allGroups) {
			if(g.getName().equals(name)) {
				return g;
			}
		}
		return null;
	}

	/** @see GroupDao#getGroupCount() */
	public int getGroupCount() {
		return allGroups.size();
	}

	/** @see GroupDao#getPageNumber(Group, int) */
	public int getPageNumber(Group group, int groupsPerPage) {
		ArrayList<Group> groups = new ArrayList<Group>();
		groups.addAll(allGroups);
		return groups.indexOf(group) / groupsPerPage;
	}

	/** @see GroupDao#saveGroup(Group) */
	public void saveGroup(Group group) throws DuplicateKeyException {
		Group parent = group.getParent();
		for(Group g : this.allGroups.toArray(new Group[0])) {
			if(g.getParent() == parent
					&& g.getName().equals(group.getName())) {
				throw new DuplicateKeyException();
			}
		}
		if(!this.allGroups.add(group)) {
			throw new DuplicateKeyException();
		}
		if(parent != null) {
			parent.addChild(group);
		}
	}

	/** @see GroupDao#updateGroup(Group) */
	public void updateGroup(Group group) {
		// do nothing for update in in-memory dao
	}

}
