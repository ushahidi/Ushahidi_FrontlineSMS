/**
 * 
 */
package net.frontlinesms.smsdevice;

import net.frontlinesms.resources.PropertySet;

/**
 * @author Alex
 *
 */
public class CommProperties extends PropertySet {
//> STATIC CONSTANTS
	/** Singleton instance of this class. */
	private static CommProperties instance;

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/**
	 * Create a new Comm properties file.
	 */
	private CommProperties() {
		super("comm");
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES
	/**
	 * Lazy getter for {@link #instance}
	 * @return The singleton instance of this class
	 */
	public static synchronized CommProperties getInstance() {
		if(instance == null) {
			instance = new CommProperties();
		}
		return instance;
	}

	/** @return the list of Comm ports to ignore. */
	public String[] getIgnoreList() {
		String ignore = super.getProperty("ignore");
		if (ignore == null) return new String[0]; 
		else return ignore.toUpperCase().split(",");
	}

//> STATIC HELPER METHODS
}
