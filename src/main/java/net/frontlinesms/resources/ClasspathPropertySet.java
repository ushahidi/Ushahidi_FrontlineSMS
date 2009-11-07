/**
 * 
 */
package net.frontlinesms.resources;

import java.io.IOException;

import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

/**
 * @author Alex
 *
 */
public class ClasspathPropertySet extends BasePropertySet {
//> STATIC CONSTANTS
	/** Logging object for this instance. */
	public static final Logger LOG = Utils.getLogger(ClasspathPropertySet.class);
	
//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/**
	 * Load a {@link BasePropertySet} from the classpath.
	 * @param path The classpath path of the resource
	 * @throws IOException 
	 */
	protected ClasspathPropertySet(String path) throws IOException {
		super(BasePropertySet.load(BasePropertySet.class.getResourceAsStream(path)));
	}

//> ACCESSORS
	/**
	 * @param propertyKey The key for the property to get
	 * @return value from {@link #properties}
	 */
	protected String getProperty(String propertyKey) {
		return super.getProperty(propertyKey);
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
