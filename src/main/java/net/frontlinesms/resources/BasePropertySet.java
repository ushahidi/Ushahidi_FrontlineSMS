/**
 * 
 */
package net.frontlinesms.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

/**
 * Base properties wrapper class.
 * @author Alex
 */
class BasePropertySet {
//> STATIC CONSTANTS
	/** Logging object for this instance. */
	public static final Logger LOG = Utils.getLogger(BasePropertySet.class);

//> INSTANCE PROPERTIES
	/** Map from property key to value */
	private final Map<String, String> properties;

//> CONSTRUCTORS
	/**
	 * @deprecated This constructor should be replaced with {@link BasePropertySet#BasePropertySet(Map)}
	 */
	BasePropertySet() {
		this.properties = null;
	}
	
	/**
	 * Create a new instance of this class.
	 * @param properties value for {@link #properties}. 
	 */
	BasePropertySet(Map<String, String> properties) {
		this.properties = properties;
	}

//> ACCESSORS
	/**
	 * @param propertyKey The key for the property to get
	 * @return value from {@link #properties}
	 */
	String getProperty(String propertyKey) {
		return this.properties.get(propertyKey);
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * Load properties from an {@link InputStream}.
	 * @param inputStream The input stream to load the properties from.
	 * @return The loaded properties
	 * @throws IOException If there was a problem loading the properties from the supplied {@link InputStream}.
	 */
	static HashMap<String, String> load(InputStream inputStream) throws IOException {
		if(inputStream == null) throw new NullPointerException("The supplied input stream was null.");
		
		BufferedReader in = null;
		try {
			HashMap<String, String> properties = new HashMap<String, String>();
			
			in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			
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
						LOG.warn("Bad line in properties file: '" + line + "'");
					} else {
						String key = line.substring(0, splitChar);					
						if(properties.containsKey(key)) {
							// This key has already been read from the language file.  Ignore the new value.
							LOG.warn("Duplicate key in properties file: ''");
						} else {
							String value = line.substring(splitChar + 1);
							properties.put(key, value);
						}
					}
				}
			}
			
			LOG.trace("EXIT");
			return properties;
		} finally {
			// Close all streams
			if(in != null) try { in.close(); } catch(IOException ex) {
				// nothing we can do except log the exception
				LOG.warn("Exception thrown while closing stream 'fis'.", ex);
			}
		}
	}
}
