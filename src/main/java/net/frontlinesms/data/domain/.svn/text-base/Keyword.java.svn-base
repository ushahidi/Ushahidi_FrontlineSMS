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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.*;

import net.frontlinesms.data.EntityField;

/**
 * 
 * @author Alex
 */
@Entity
public class Keyword {
//> CONSTANTS
	/** Pattern used for checking if a string contains any whitespace. */
	private static final Pattern CONTAINS_WHITESPACE = Pattern.compile("\\s");
	
	public static final int TYPE_BLACKLIST = 0;
	public static final int TYPE_WHITELIST = 1;
	
	public static final int REFERENCE_CONTACT = 0;
	public static final int REFERENCE_GROUP = 1;
	
//> Entity Fields
	/** Details of the fields that this class has. */
	public enum Field implements EntityField<Keyword> {
		KEYWORD("keyword"),
		PARENT("parent");
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
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(unique=true,nullable=false,updatable=false) @SuppressWarnings("unused")
	private long id;
	private String keyword;
	/** parent of this keyword */
	@ManyToOne private Keyword parent;
	@OneToMany(fetch=FetchType.EAGER) private Set<Keyword> children = new HashSet<Keyword>();
	/** description of this keyword */
	private String description;
	/** actions attached to this keyword */
	@OneToMany(fetch=FetchType.EAGER, mappedBy="keyword", targetEntity=KeywordAction.class)
	private Set<KeywordAction> actions = new HashSet<KeywordAction>();

//> CONSTRUCTORS
	/** Empty constructor for hibernate */
	Keyword() {}

	/**
	 * Creates a new keyword.
	 * @param parent the parent of this keyword
	 * @param keyword the keyword
	 * @param description A description of this keyword.
	 * @throws IllegalArgumentException thrown if supplied string contains any whitespace.
	 */
	public Keyword(Keyword parent, String keyword, String description) throws IllegalArgumentException {
		if (CONTAINS_WHITESPACE.matcher(keyword).find()) throw new IllegalArgumentException("Illegal keyword - contains whitespace: '" + keyword + "'");
		this.keyword = keyword;
		this.description = description;
		if(parent != null) {
			this.parent = parent;
			this.parent.children.add(this);
		}
	}
	
	/**
	 * Determines whether this keyword matches the supplied messageContent.
	 * N.B. it may be the case that this keyword matches BUT SO DOES A DESCENDANT KEYWORD, in which case
	 * actions are probably not activated on this keyword but rather on the descendant.
	 * @param messageContent message content to check this keyword against.
	 * @return <code>true</code> if the message content matches this keyword; <code>false</code> otherwise.
	 */
	public boolean matches(String messageContent) {
		if(messageContent != null) {
			String keywordString = this.getKeywordString();
			if(messageContent.startsWith(keywordString + ' ')
						|| messageContent.equals(keywordString)) {
				return true;
			}
		}
		return false;
	}
	
//> ACCESSOR METHODS
	/**
	 * Gets the single keyword this object represents.
	 * @return {@link #keyword}
	 */
	public String getKeyword() {
		return this.keyword;
	}
	
	/**
	 * Gets the full string that would appear in a message to activate this
	 * keyword string - i.e. this keyword preceded by all its antecedants.
	 * @return
	 */
	public String getKeywordString() {
		if(this.parent == null) {
			return this.keyword;
		} else {
			return this.parent.getKeywordString() + ' ' + this.keyword;
		}
	}
	
	/**
	 * Get the description of this keyword.
	 * @return {@link #description}.
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Set the description of this keyword.
	 * @param description new value for {@link #description}
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Removes the supplied action from this keyword.
	 * @param action
	 */
	public void removeAction(KeywordAction action) {
		this.actions.remove(action);
	}
	
	/**
	 * Returns the parent keyword to this Keyword, or null if it has none.
	 * @return {@link #parent}
	 */
	public Keyword getParent() {
		return this.parent;
	}
	
	/**
	 * Gets the keyword actions associated with this keyword.
	 * @return the parent keyword of this keyword, or NULL if this is top-level
	 */
	public Collection<KeywordAction> getActions() {
		return Collections.unmodifiableCollection(this.actions);
	}
	
	/**
	 * Gets all keywords directly below this one.  E.g. if the following keywords exist:
	 *   MASABI
	 *   MASABI JOIN
	 *   MASABI LEAVE
	 * then calling this method on the object representing MASABI should return an object
	 * representing MASABI JOIN and an object representing MASABI LEAVE.
	 * @return {@link #children}
	 */
	public Collection<Keyword> getDirectSubWords() {
		return this.children;
	}
	
	/**
	 * Returns an unordered list of all sub-keywords of this keyword.
	 * @return
	 */
	public Collection<Keyword> getAllSubWords() {
		HashSet<Keyword> subwords = new HashSet<Keyword>();
		addAllSubWords(subwords);
		return subwords;
	}
	
	/**
	 * Adds all subwords of this keyword to the supplied {@link Collection}
	 * @param subwords
	 */
	private void addAllSubWords(Collection<Keyword> subwords) {
		for(Keyword child : children) {
			subwords.add(child);
			child.addAllSubWords(subwords);
		}
	}

//> GENERATED CODE
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
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
		Keyword other = (Keyword) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	
}
