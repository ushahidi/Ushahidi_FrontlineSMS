/**
 * 
 */
package net.frontlinesms.plugins;

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
	 */
	public void init(FrontlineSMS frontlineController);
	
	/**
	 * Gets the tab for this plugin 
	 * @param uiController {@link UiGeneratorController} instance that will be the parent of this tab.
	 * @return the tab to display for this plugin
	 */
	public Object getTab(UiGeneratorController uiController);
	
	/**
	 * Initiliazes UI data for the plugin
	 */
	public void initializePluginData();
}
