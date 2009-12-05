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
package net.frontlinesms.data.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;

/**
 * Object representing a named group of contacts.  A group can contain sub-groups
 * whose membership is entirely independent of the main group.
 * @author Alex
 */
@Entity(name="frontline_group")
@Table(uniqueConstraints={@UniqueConstraint(columnNames={Group.COLUMN_NAME, Group.COLUMN_PARENT + "_" + Group.COLUMN_ID})})
public class Group {

//> DATABASE COLUMN NAMES
	/** Database column name for property: {@link #name} */
	static final String COLUMN_ID = "group_id";
	/** Database column name for property: {@link #name} */
	static final String COLUMN_NAME = "name";
	/** Database column name for property: {@link #directMembers} */
	static final String COLUMN_DIRECT_MEMBERS = "directMembers";
	/** Database column name for property: {@link #parent} */
	static final String COLUMN_PARENT = "parent";
	/** Database column name for property: {@link #parent} */
	static final String COLUMN_CHILDREN = "children";

//> ENTITY FIELDS
	/** Details of the fields that this class has. */
	public enum Field implements EntityField<Group> {
		ID(COLUMN_ID),
		/** Represents {@link #name} */
		NAME(COLUMN_NAME),
		/** Represents {@link #parent} */
		PARENT(COLUMN_PARENT),
		/** Represents {@link #directMembers} */
		DIRECT_MEMBERS(COLUMN_DIRECT_MEMBERS);
		
		/** name of a field */
		private final String fieldName;
		/**
		 * Creates a new {@link Field}
		 * @param fieldName name of the field
		 */
		Field(String fieldName) { this.fieldName = fieldName; }
		/** @see EntityField#getFieldName() */
		public String getFieldName() { return this.fieldName; }
	}
	
//> PROPERTIES
	/** Unique id for this entity.  This is for hibernate usage. */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name=COLUMN_ID,unique=true,nullable=false,updatable=false)
	@SuppressWarnings("unused")
	private long id;
	
	/** The name of this group. */
	@Column(name=COLUMN_NAME)
	private String name;
	
	/** Contacts who are direct members of this group */
	@ManyToMany(
			fetch=FetchType.EAGER)
	@JoinTable(
			name="group_contact",
			joinColumns=@JoinColumn(name=Group.COLUMN_ID),
			inverseJoinColumns=@JoinColumn(name=Contact.COLUMN_ID))
	private Set<Contact> directMembers = new HashSet<Contact>();

	/** Parent of this group */
	@ManyToOne(fetch=FetchType.EAGER, targetEntity=Group.class)
	private Group parent;

	/** Subgroups */
	@OneToMany(fetch=FetchType.EAGER, mappedBy=COLUMN_PARENT, targetEntity=Group.class, cascade=CascadeType.REMOVE)
	@Column(name=COLUMN_CHILDREN)
	private Set<Group> children = new HashSet<Group>();
	
//> CONSTRUCTORS
	/** Empty constructor for hibernate */
	Group() {}
	
	/**
	 * Creates a group with the specified parent and name.
	 * @param parent The parent of the group to be created.
	 * @param name The name of the new group.
	 */
	public Group(Group parent, String name) {
		this.name = name;
		this.parent = parent;
		if(this.parent != null) {
			this.parent.addChild(this);
		}
	}
	
//> ACCESSOR METHODS
	/**
	 * Checks if there are any subgroups of this group.
	 * @return TRUE if this group has sub-groups, or FALSE otherwise.
	 */
	public boolean hasDescendants() {
		return this.children.size() > 0;
	}
	
	/**
	 * Returns the direct subgroups of this group.
	 * @return {@link #children}
	 */
	public Collection<Group> getDirectSubGroups() {
		return this.children;
	}
	
	/**
	 * Gets members of this group.  Does NOT recurse into subgroups.
	 * @return {@link #directMembers}
	 */
	public Set<Contact> getDirectMembers() {
		return this.directMembers;
	}
	
	/** @param directMembers new value for {@link #directMembers} */
	public void setDirectMembers(Set<Contact> directMembers) {
		this.directMembers = directMembers;
	}
	
	/**
	 * Gets the name of this group.
	 * @return {@link #name}
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Adds the contact to this group if they are not already a member.
	 * @param contact The contact to add to this group.
	 * @return TRUE if the contact has been added to this group, or FALSE if the contact was already a member.
	 */
	public boolean addDirectMember(Contact contact) {
		contact.addToGroup(this);
		return this.directMembers.add(contact);
	}
	
	/**
	 * Removes the supplied contact from this group if they were already a member.
	 * @param contact
	 * @return <code>true</code> if the {@link Contact} was removed from the {@link Group}; <code>false</code> if he was never a member in the first place.
	 */
	public boolean removeContact(Contact contact) {
		contact.removeFromGroup(this);
		return this.directMembers.remove(contact);
	}

	/** @return  an unsorted list of all members of this group. */
	public Collection<Contact> getAllMembers() {
		Set<Contact> allMembers = new HashSet<Contact>();
		addAllMembers(allMembers);
		return allMembers;
	}
	
	/**
	 * Add all members of this group and its subgroups to the supplied {@link Set}.
	 * @param allMembers
	 */
	private void addAllMembers(Set<Contact> allMembers) {
		allMembers.addAll(this.directMembers);
		for(Group g : this.children) {
			g.addAllMembers(allMembers);
		}
	}
	
	/**
	 * Returns a sub-section of the list of members of this group.
	 * @param startIndex
	 * @param limit
	 * @return a page from the list of all members of this group
	 */
	public List<Contact> getAllMembers(int startIndex, int limit) {
		List<Contact> allMembers = new ArrayList<Contact>();
		allMembers.addAll(getAllMembers());
		return allMembers.subList(startIndex, Math.min(allMembers.size(), startIndex + limit));
	}
	
	/**
	 * Returns the number of members in this group. 
	 * @return the number of members in this group
	 */
	public int getAllMembersCount() {
		return getAllMembers().size();
	}
	
	/**
	 * Returns the parent of this group. 
	 * @return {@link #parent}
	 */
	public Group getParent() {
		return this.parent;
	}
	
	/**
	 * Set {@link #name}.
	 * @param name new value for {@link #name}
	 */
	public void setName(String name) {
		if(this.name != null) throw new IllegalStateException("Groups may not change name.");
		if(name == null) throw new IllegalArgumentException("Illegal group name: " + null);
		this.name = name;
	}

	/**
	 * Set {@link #parent}
	 * @param parent new value for {@link #parent}
	 */
	public void setParent(Group parent) {
		if(this.parent != null) throw new IllegalStateException("Groups may not change their parent.");
		this.parent = parent;
	}

	/**
	 * Add group to {@link #children}
	 * @param group group to add to {@link #children}
	 */
	public void addChild(Group group) {
		this.children.add(group);
	}
	
	/**
	 * Remove a group from {@link #children}
	 * @param group group to remove from {@link #children}
	 */
	public void removeChild(Group group) {
		this.children.remove(group);
	}


//> GENERATED CODE
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}
}
