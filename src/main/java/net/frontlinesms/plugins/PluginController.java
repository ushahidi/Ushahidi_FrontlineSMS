/**
 * 
 */
package net.frontlinesms.plugins;

import org.springframework.context.ApplicationContext;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.ui.UiGeneratorController;

/**
 * Basic interface that all FrontlineSMS plugins must implement.
 * @author Alex
 */
public interface PluginController {
	/**
	 * Gets the name for this plugin.  This should be internationalised if that is suitable.
	 * @return The name of this plugin.
	 */
	public String getName();
	
	/**
	 * Initialise the plugin from the {@link FrontlineSMS} controller instance. 
	 * @param frontlineController {@link FrontlineSMS} instance that this plugin is "plugged-in" to.
	 * @param applicationContext {@link ApplicationContext} for FrontlineSMS config
	 * @throws PluginInitialisationException if there was an identified problem initialising the plugin
	 */
	public void init(FrontlineSMS frontlineController, ApplicationContext applicationContext) throws PluginInitialisationException;
	
	/**
	 * Gets the tab for this plugin 
	 * @param uiController {@link UiGeneratorController} instance that will be the parent of this tab.
	 * @return the tab to display for this plugin
	 */
	public Object getTab(UiGeneratorController uiController);

	/**
	 * Gets the location of the Spring config for this plugin.
	 * 
	 * If the config is on the classpath, this should be detailed like:
	 * <code>classpath:package1/package2/pluginname-spring-hibernate.xml</code>
	 * 
	 * @return the location of the Spring config for this plugin, or <code>null</code> if none is required.
	 */
	public String getSpringConfigPath();
	
	/**
	 * Gets the location of the hibernate config for this plugin.
	 * 
	 * If the config is on the classpath, this should be detailed like:
	 * <code>classpath:package1/package2/pluginname.hibernate.cfg.xml</code>
	 * 
	 * @return the location of the hibernate config for this plugin, or <code>null</code> if none is required.
	 */
	public String getHibernateConfigPath();
	
	/**
	 * Modified by Emmanuel Kala ekala<at>gmail.com
	 * 
	 * Initializes the data of the plug-in so that the plug-in components are added to the UI with
	 * the data from the database 
	 */
	public void initializePluginData();
}
