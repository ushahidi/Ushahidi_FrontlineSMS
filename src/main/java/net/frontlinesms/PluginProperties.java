/**
 * 
 */
package net.frontlinesms;

import java.util.Collection;

import net.frontlinesms.resources.PropertySet;

/**
 * @author Alex
 *
 */
public class PluginProperties extends PropertySet {
//> STATIC CONSTANTS
	/** Singleton instance of this class. */
	private static PluginProperties instance;

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/** Create a new Plugin properties file. */
	private PluginProperties() {
		super("plugins");
	}

//> ACCESSORS
	/**
	 * @param pluginClassName The name of the class of the plugin.
	 * @return <code>true</code> if the plugin is explicitly enabled; <code>false</code> otherwise.
	 */
	public boolean isPluginEnabled(String pluginClassName) {
		Boolean enabled = super.getPropertyAsBoolean(pluginClassName);
		return enabled != null && enabled.booleanValue();
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES
	/**
	 * Lazy getter for {@link #instance}
	 * @return The singleton instance of this class
	 */
	public static synchronized PluginProperties getInstance() {
		if(instance == null) {
			instance = new PluginProperties();
		}
		return instance;
	}

	/** @return get the class names of all plugins available */
	public Collection<String> getPluginClassNames() {
		return super.getPropertyKeys();
	}

//> STATIC HELPER METHODS
}
