package com.ushahidi.plugins.mapping.managers;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.plugins.PluginController;

/**
 * Abstract Manager
 * @author dalezak
 *
 */
public abstract class Manager implements EventObserver {

	/**
	 * Get PluginController
	 * @param frontlineController FrontlineSMS
	 * @return PluginController
	 */
	protected static <T extends PluginController> T getPluginController(FrontlineSMS frontlineController, Class<T> clazz) {
		for (PluginController pluginController : frontlineController.getPluginManager().getPluginControllers()) {
			if (pluginController.getClass() == clazz) {
				return clazz.cast(pluginController);
			}
		}
		return null;
	}
}