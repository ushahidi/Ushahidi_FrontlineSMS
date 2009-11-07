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
package net.frontlinesms.resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

/**
 * Set of properties with String value. Each {@link PropertySet} is tied to a specific file located in
 * the properties directory of the application's config.
 *
 * TODO This class should be renamed FilePropertySet
 * 
 * @author Alex
 */
public abstract class PropertySet extends BasePropertySet {
	
//> CONSTANTS
	/** Logging object for this instance. */
	public static final Logger LOG = Utils.getLogger(PropertySet.class);
	
//> INSTANCE PROPERTIES
	/** The {@link File} that this {@link PropertySet} is loaded from and saved to. */
	private final File file;
	/** Map from property key to value */
	private Map<String, String> properties;
	
//> INSTANCE METHODS
	/**
	 * Create a new instance of this class from the supplied name.
	 * @param name The name of the {@link PropertySet} from which is derived the file it is persisted to
	 */
	protected PropertySet(String name) {
		super();
		this.file = ResourceUtils.getPropertiesFile(name);
		this.properties = PropertySet.load(this.file);
	}
	
	/**
	 * Save this {@link PropertySet} to disk.
	 * @return <code>true</code> if the properties file was successfully saved; <code>false</code> otherwise.
	 */
	public synchronized boolean saveToDisk() {
		LOG.trace("ENTER");
		File propFile = this.file;
		
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
	
//> ACCESSOR METHODS
	/**
	 * Set a property in this property set.
	 * @param propertyName
	 * @param value
	 */
	protected synchronized void setProperty(String propertyName, String value) {
		this.properties.put(propertyName, value);
	}
	
	/**
	 * Gets the {@link String} value of a property.
	 * @param propertyName
	 * @return The value of the property as a {@link String} or <code>null</code> if it is not set.
	 */
	protected synchronized String getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}
	
	/**
	 * Gets the {@link Boolean} value of a property.
	 * @param propertyName
	 * @return The value of the property as a {@link Boolean} or <code>null</code> if it is not set.
	 */
	protected Boolean getPropertyAsBoolean(String propertyName) {
		String value = getProperty(propertyName);
		if (value == null) return null;
		else return Boolean.parseBoolean(value);
	}
	
	/** @return the property keys in {@link #properties} */
	protected Collection<String> getPropertyKeys() {
		return this.properties.keySet();
	}
	
//> GETTERS WITH DEFAULT VALUES
	/**
	 * Gets the {@link String} value of a property.  If no value is set, the default value is set and then returned.
	 * @param propertyName The name of this property
	 * @param defaultValue The value to use for this property if none is yet set
	 * @return The value to be used for this property
	 */
	protected synchronized String getProperty(String propertyName, String defaultValue) {
		if(!this.properties.containsKey(propertyName)) {
			this.properties.put(propertyName, defaultValue);
		}
		return this.properties.get(propertyName);
	}
	
//> STATIC FACTORY METHODS
	/**
	 * Loads a {@link PropertySet} from the supplied file
	 * @param propFile The file to load the {@link PropertySet} from
	 * @return new map of properties loaded from the requested file, or an empty map if no properties could be loaded. 
	 */
	public static HashMap<String, String> load(File propFile) {
		LOG.debug("File [" + propFile.getAbsolutePath() + "]");

		HashMap<String, String> properties = null;
		
		FileInputStream fis = null;
		BufferedReader in = null;
		try {
			fis = new FileInputStream(propFile);
			
			properties = BasePropertySet.load(fis);
		} catch(FileNotFoundException ex) {
			LOG.debug("Properties file not found [" + propFile.getAbsolutePath() + "]", ex);
		} catch(IOException ex) {
			LOG.debug("Exception thrown while loading properties file:" + propFile.getAbsolutePath(), ex);
		} finally {
			// Close all streams
			if(fis != null) try { fis.close(); } catch(Exception ex) {
				// nothing we can do except log the exception
				LOG.warn("Exception thrown while closing stream 'fis'.", ex);
			}
			if(in != null) try { in.close(); } catch(IOException ex) {
				// nothing we can do except log the exception
				LOG.warn("Exception thrown while closing stream 'fis'.", ex);
			}
		}
		
		if(properties == null) {
			properties = new HashMap<String, String>();
		}
		
		LOG.trace("EXIT");
		return properties;
	}
}
