/**
 * 
 */
package net.frontlinesms;

import net.frontlinesms.resources.PropertySet;

/**
 * @author Alex
 *
 */
public final class AppProperties extends PropertySet {
//> STATIC CONSTANTS
	
//> PROPERTY STRINGS
	/** Property key (String): the filename of the language file */
	private static final String KEY_LANGUAGE_FILENAME = "language.filename";
	/** Property key (boolean): show the first time wizard or not */
	private static final String KEY_SHOW_WIZARD = "first.time.wizard";
	/** Property key (String): the version of the application last time it was run */
	private static final String KEY_VERSION_LAST_RUN = "version";
	/** Property key (String): Path to the database config file to use */
	private static final String KEY_DATABASE_CONFIG_PATH = "database.config";
	
//> DEFAULT VALUES
	/** Default value for {@link #KEY_DATABASE_CONFIG_PATH} */
	private static final String DEFAULT_DATABASE_CONFIG_PATH = "hsql.database.xml";
	
	/** Singleton instance of this class. */
	private static AppProperties instance;

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/** Create a new App properties file. */
	private AppProperties() {
		super("app");
	}

//> ACCESSORS
	/** @return the name of the language filename */
	public String getLanguageFilename() {
		return super.getProperty(KEY_LANGUAGE_FILENAME);
	}
	/** @param filename the name of the language filename */
	public void setLanguageFilename(String filename) {
		super.setProperty(KEY_LANGUAGE_FILENAME, filename);
	}
	/** @return <code>true</code> if first time wizard should be shown; <code>false</code> otherwise */
	public boolean isShowWizard() {
		Boolean showWizard = super.getPropertyAsBoolean(KEY_SHOW_WIZARD);
		return showWizard == null || showWizard;
	}
	/** @param showWizard <code>true</code> if the wizard should be shown, <code>false</code> otherwise */
	public void setShowWizard(boolean showWizard) {
		super.setProperty(KEY_SHOW_WIZARD, Boolean.toString(false));
	}
	/** @return the last version of the app that was run */
	public String getLastRunVersion() {
		return super.getProperty(KEY_VERSION_LAST_RUN);
	}
	/** @param version the version of the app currently running */
	public void setLastRunVersion(String version) {
		super.setProperty(KEY_VERSION_LAST_RUN, version);
	}
	/** @return the path to the database config file */
	public String getDatabaseConfigPath() {
		return super.getProperty(KEY_DATABASE_CONFIG_PATH, DEFAULT_DATABASE_CONFIG_PATH);
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES
	/**
	 * Lazy getter for {@link #instance}
	 * @return The singleton instance of this class
	 */
	public static synchronized AppProperties getInstance() {
		if(instance == null) {
			instance = new AppProperties();
		}
		return instance;
	}

//> STATIC HELPER METHODS
}
