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

import javax.persistence.*;

import net.frontlinesms.data.EntityField;
import net.frontlinesms.data.repository.memory.InMemoryKeywordDao;

/**
 * 
 * @author Alex
 */
@Entity
public class Keyword {

//> DATABASE COLUMN NAMES
	/**
	 * Database column name for field {@link #keyword}
	 * N.B. This cannot be private as it is referenced in the class annotation.  Otherwise, it *would* be private. 
	 */
	static final String COLUMN_KEYWORD = "keyword";
	
//> CONSTANTS
	
//> Entity Fields
	/** Details of the fields that this class has. */
	public enum Field implements EntityField<Keyword> {
		/** Refers to {@link #keyword} */
		KEYWORD(COLUMN_KEYWORD);
		
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

	/** Actual text of the keyword.  This is ALWAYS STORED IN UPPER CASE. */
	@Column(name=COLUMN_KEYWORD, nullable=false, updatable=false, unique=true)
	private String keyword;

	/** description of this keyword */
	private String description;

//> CONSTRUCTORS
	/** Empty constructor for hibernate */
	Keyword() {}

	/**
	 * Creates a new keyword.
	 * @param keyword the keyword
	 * @param description A description of this keyword.
	 */
	public Keyword(String keyword, String description) {
		if(!keyword.matches("((\\S)+( (\\S)+)*)?")) throw new IllegalArgumentException("Did not match required format for keyword - should be one or more words separated by a single space.");
		this.keyword = keyword.toUpperCase();
		this.description = description;
	}
	
	/**
	 * Determines whether this keyword matches the supplied messageContent.
	 * N.B. it may be the case that this keyword matches BUT SO DOES A LONGER KEYWORD.
	 * @param messageContent message content to check this keyword against.
	 * @return <code>true</code> if the message content matches this keyword; <code>false</code> otherwise.
	 * FIXME this is only used by {@link InMemoryKeywordDao} so should be moved there
	 */
	public boolean matches(String messageContent) {
		if(messageContent != null) {
			messageContent = messageContent.toUpperCase();
			String keywordString = this.getKeyword();
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

//> GENERATED CODE
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
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
		return true;
	}
}
