/**
 * 
 */
package net.frontlinesms.plugins.forms;

import net.frontlinesms.resources.PropertySet;

/**
 * @author Alex
 *
 */
public class FormsProperties extends PropertySet {
//> STATIC CONSTANTS
	/** Singleton instance of this class. */
	private static FormsProperties instance;

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/**
	 * Create a new Forms properties file.
	 */
	private FormsProperties() {
		super("forms.plugin");
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES
	/**
	 * Lazy getter for {@link #instance}
	 * @return The singleton instance of this class
	 */
	public static synchronized FormsProperties getInstance() {
		if(instance == null) {
			instance = new FormsProperties();
		}
		return instance;
	}

	/** @return the name of the handler class */
	public String getHandlerClassName() {
		return super.getProperty("forms.handler.class");
	}

//> STATIC HELPER METHODS
}
