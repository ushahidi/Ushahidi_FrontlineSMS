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
package net.frontlinesms.properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.frontlinesms.Utils;
import net.frontlinesms.resources.ResourceUtils;

import org.apache.log4j.Logger;

/**
 * Set of properties with String value. Each {@link PropertySet} is tied to a specific file located in
 * the properties directory of the application's config.
 * @author Alex
 */
public class PropertySet {
	
//> CONSTANTS
	/** Logging object for this instance. */
	private static final Logger LOG = Utils.getLogger(PropertySet.class);
	/** The location of {@link PropertySet} files. */
	private static final String DIRECTORY_NAME = ResourceUtils.DIRECTORY_PROPERTIES;
	/** The filename extension used for {@link PropertySet} files. */
	private static final String EXTENSION = ".properties";
	
//> INSTANCE PROPERTIES
	/** The name of this {@link PropertySet}.  This is the filename that this {@link PropertySet} will be deployed to. */
	private final String name;
	/** Map from property key to value */
	private Map<String, String> properties;
	
//> INSTANCE METHODS
	/**
	 * Create a new instance of this class with the supplied name and property values.
	 * @param name
	 * @param properties
	 */
	private PropertySet(String name, Map<String, String> properties) {
		this.name = name;
		this.properties = properties;
	}
	
	/**
	 * Gets the path at which this property file should be saved, and can be loaded from.
	 * @return the path to the file these properties are stored in
	 */
	private String getFilePath() {
		return getFilePath(name);
	}
	
	/**
	 * Save this {@link PropertySet} to disk.
	 * @return <code>true</code> if the properties file was successfully saved; <code>false</code> otherwise.
	 */
	public synchronized boolean saveToDisk() {
		LOG.trace("ENTER");
		File propFile = new File(getFilePath());
		
		BufferedWriter out = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(propFile);
			out = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			
			for(String propertyKey : this.properties.keySet()) {
				out.write(propertyKey + "=" + this.properties.get(propertyKey) + "\n");
			}
			
			out.flush();
			
			LOG.trace("EXIT");
			return true;
		} catch(IOException ex) {
			LOG.debug("Exception thrown while saving properties file: " + propFile.getAbsolutePath(), ex);
			LOG.trace("EXIT");
			return false;
		} finally {
			if(fos != null) { try { fos.close(); } catch(IOException ex) {} }
			if(out != null) { try { out.close(); } catch(IOException ex) {} }
		}
	}
	
	/** @return keys of all properties in this set */
	public Set<String> getAllKeys() {
		return this.properties.keySet();
	}
	
//> ACCESSOR METHODS
	/**
	 * Set a property in this property set.
	 * @param propertyName
	 * @param value
	 */
	public synchronized void setProperty(String propertyName, String value) {
		this.properties.put(propertyName, value);
	}
	
	/**
	 * Gets the {@link String} value of a property.
	 * @param propertyName
	 * @return The value of the property as a {@link String} or <code>null</code> if it is not set.
	 */
	public synchronized String getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}
	
	/**
	 * Gets the {@link Boolean} value of a property.
	 * @param propertyName
	 * @return The value of the property as a {@link Boolean} or <code>null</code> if it is not set.
	 */
	public Boolean getPropertyAsBoolean(String propertyName) {
		String value = getProperty(propertyName);
		if (value == null) return null;
		else  return Boolean.parseBoolean(value);
	}

//> FACTORY METHODS
	/**
	 * Load a property set with the given name.
	 * @param name The name of the {@link PropertySet} to load.
	 * @return
	 */
	public static PropertySet load(String name) {
		LOG.trace("ENTER");
		File propFile = new File(getFilePath(name));
		LOG.debug("File [" + propFile.getAbsolutePath() + "]");

		HashMap<String, String> properties = new HashMap<String, String>();
		
		FileInputStream fis = null;
		BufferedReader in = null;
		try {
			fis = new FileInputStream(propFile);
			in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			
			String line;
			while((line = in.readLine()) != null) {
				line = line.trim();				
				if(line.length() == 0 || line.charAt(0) == '#') {
					// This is a comment, so we should remember it to write back later
				} else {
					int splitChar =  line.indexOf('=');
					if(splitChar <= 0) {
						// there's no "key=value" pair on this line, but it does have text on it.  That's
						// not strictly legal, so we'll log a warning and carry on.
						LOG.warn("Bad line in properties file '" + name + "': '" + line + "'");
					} else {
						String key = line.substring(0, splitChar);					
						if(properties.containsKey(key)) {
							// This key has already been read from the language file.  Ignore the new value.
							LOG.warn("Duplicate key in properties file '" + name + "': ''");
						} else {
							String value = line.substring(splitChar + 1);
							properties.put(key, value);
						}
					}
				}
			}
		} catch(FileNotFoundException ex) {
			LOG.debug("Properties file not found [" + propFile.getAbsolutePath() + "]", ex);
		} catch(IOException ex) {
			LOG.debug("Exception thrown while loading properties file:" + propFile.getAbsolutePath(), ex);
		} finally {
			if(fis != null) { try { fis.close(); } catch(IOException ex) {} }
			if(in != null) { try { in.close(); } catch(IOException ex) {} }
		}
		
		LOG.trace("EXIT");
		return new PropertySet(name, properties);
	}
	
//> STATIC HELPER METHODS
	/**
	 * Gets the path of the file where a {@link PropertySet} is persisted.
	 * @param propertySetName
	 * @return the path to a particular property file
	 */
	private static final String getFilePath(String propertySetName) {
		return ResourceUtils.getConfigDirectoryPath() + DIRECTORY_NAME + File.separatorChar + propertySetName + EXTENSION;
	}
}
