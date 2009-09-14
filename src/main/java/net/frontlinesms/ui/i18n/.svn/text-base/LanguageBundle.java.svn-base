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
/**
 * 
 */
package net.frontlinesms.ui.i18n;

import java.util.Map;
import java.util.MissingResourceException;

/**
 * Bundle of translations for a language, and associated properties.
 * @author Alex
 */
public class LanguageBundle {
	/** Key used to extract the language's ISO-??? code. */
	// FIXME put in the ISO code for the language IDs we are using in this comment.
	public static final String KEY_LANGUAGE_CODE = "bundle.language";
	/** Key used to extract the human-readable name of this language, in this language! */
	public static final String KEY_LANGUAGE_NAME = "bundle.language.name";
	/** Key used to extract the ISO-???? 2-letter country code of the flag used for this language. */
	// FIXME put in the ISO code for the country IDs we are using in this comment.
	public static final String KEY_LANGUAGE_COUNTRY = "bundle.language.country";
	/** Key used to extract the  */
	public static final String KEY_RIGHT_TO_LEFT = "language.direction.right.to.left";
	
	/** Map of i18n string keys to internationalised strings. */
	private final Map<String, String> properties;
	/** The filename that this bundle was loaded from. */
	private final String filename;
	
	/**
	 * Instantiate a new {@link LanguageBundle} with the given properties.
	 * @param filename
	 * @param properties
	 */
	LanguageBundle(String filename, Map<String, String> properties) {
		this.properties = properties;
		this.filename = filename;

		checkRequiredProperty(KEY_LANGUAGE_CODE);
		checkRequiredProperty(KEY_LANGUAGE_NAME);
		checkRequiredProperty(KEY_LANGUAGE_COUNTRY);
	}
	
	/**
	 * Checks if a required property is present.  A {@link MissingResourceException} will
	 * be thrown if the value is not present.
	 * @param key
	 * @throws MissingResourceException if there are any required property keys missing.
	 */
	private void checkRequiredProperty(String key) {
		if(!this.properties.containsKey(key)) {
			throw new MissingResourceException("Language bundle missing required property: '" + key + "'", getClass().getName(), key);
		}
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getCountry() {
		return getValue(KEY_LANGUAGE_COUNTRY);
	}
	
	public String getLanguage() {
		return getValue(KEY_LANGUAGE_NAME);
	}
	
	public boolean isRightToLeft() {
		try {
			String r2l = getValue(KEY_RIGHT_TO_LEFT);
			return Boolean.parseBoolean(r2l.trim());
		} catch(MissingResourceException ex) {
			return false;
		}
	}
	
	public String getValue(String key) {
		String value = properties.get(key);
		if(value == null) {
			throw new MissingResourceException("Requested resource not found in language bundle '" + filename + "'", LanguageBundle.class.getName(), key);
		}
		return value;
	}

	/** Gets the mapping of keys to internationalised text contained in this bundle. */
	public Map<String, String> getProperties() {
		return this.properties;
	}
}
