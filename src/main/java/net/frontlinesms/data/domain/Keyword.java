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
@Table(uniqueConstraints={@UniqueConstraint(columnNames={Keyword.COLUMN_KEYWORD, Keyword.COLUMN_PARENT})})
public class Keyword {

//> DATABASE COLUMN NAMES
	/**
	 * Database column name for field {@link #keyword}
	 * N.B. This cannot be private as it is referenced in the class annotation.  Otherwise, it *would* be private. 
	 */
	static final String COLUMN_KEYWORD = "keyword";
	/**
	 * Database column name for field {@link #parent}.  N.B. {@link #parent} is not a {@link Column}, so this value must be the same as the field name.
	 * N.B. This cannot be private as it is referenced in the class annotation.  Otherwise, it *would* be private.
	 */
	static final String COLUMN_PARENT = "parent";
	
//> CONSTANTS
	/** Pattern used for checking if a string contains any whitespace. */
	private static final Pattern CONTAINS_WHITESPACE = Pattern.compile("\\s");
	
//> Entity Fields
	/** Details of the fields that this class has. */
	public enum Field implements EntityField<Keyword> {
		/** Refers to {@link #keyword} */
		KEYWORD(COLUMN_KEYWORD),
		/** Refers to {@link #parent} */
		PARENT(COLUMN_PARENT);
		
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
	@SuppressWarnings("unused")
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(unique=true,nullable=false,updatable=false)
	private long id;

	/** Actual text of the keyword */
	@Column(name=COLUMN_KEYWORD, nullable=false, updatable=false)
	private String keyword;

	/** parent of this keyword */
	@ManyToOne(optional=true, targetEntity=Keyword.class)
	@JoinColumn(name=COLUMN_PARENT, nullable=true, updatable=false)
	private Keyword parent;

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
		this.parent = parent;
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
	
//> GENERATED CODE
	

//> GENERATED CODE
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
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
