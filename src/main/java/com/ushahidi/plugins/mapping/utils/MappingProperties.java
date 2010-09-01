package com.ushahidi.plugins.mapping.utils;

import net.frontlinesms.resources.UserHomeFilePropertySet;

public class MappingProperties extends UserHomeFilePropertySet {

	private static MappingLogger LOG = MappingLogger.getLogger(MappingProperties.class);
	
	private static MappingProperties instance;
	
	private static final String DEBUG_MODE = "debug.mode";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	
	protected MappingProperties() {
		super("mapping");
	}
	
	private static synchronized MappingProperties getInstance() {
		if (instance == null) {
			instance = new MappingProperties();
		}
		return instance;
	}
	
	public static boolean isDebugMode() {
		return TRUE.equalsIgnoreCase(getInstance().getProperty(DEBUG_MODE));
	}
	
	public static void setDebugMode(boolean debug) {
		LOG.debug("setDebugMode: %s", debug);
		if (debug) {
			getInstance().setProperty(DEBUG_MODE, TRUE);
		}
		else {
			getInstance().setProperty(DEBUG_MODE, FALSE);
		}
		getInstance().saveToDisk();
	}
}