package com.ushahidi.plugins.mapping.util;

import com.ushahidi.plugins.mapping.maps.providers.MapProvider;
import com.ushahidi.plugins.mapping.maps.providers.MapProviderFactory;

import net.frontlinesms.resources.UserHomeFilePropertySet;

public class MappingProperties extends UserHomeFilePropertySet {

	private static MappingLogger LOG = MappingLogger.getLogger(MappingProperties.class);
	
	private static MappingProperties instance;
	
	private static final String DEBUG_MODE = "debug.mode";
	private static final String DEFAULT_LATITUDE = "default.latitude";
	private static final String DEFAULT_LONGITUDE = "default.longitude";
	private static final String DEFAULT_ZOOM = "default.zoom";
	private static final String DEFAULT_MAP_PROVIDER = "default.map.provider";
	private static final String TRUE = "true";
	private static final String YES = "yes";
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
		return 	TRUE.equalsIgnoreCase(getInstance().getProperty(DEBUG_MODE)) ||
				YES.equalsIgnoreCase(getInstance().getProperty(DEBUG_MODE));
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
	
	public static int getDefaultZoomLevel() {
		String defaultZoom = getInstance().getProperty(DEFAULT_ZOOM);
		return defaultZoom != null ? Integer.parseInt(defaultZoom) : 7;
	}
	
	public static void setDefaultZoomLevel(int zoomLevel) {
		getInstance().setProperty(DEFAULT_ZOOM, String.valueOf(zoomLevel));
		getInstance().saveToDisk();
	}
	
	public static void setDefaultMapProvider(MapProvider mapProvider) {
		if (mapProvider != null) {
			getInstance().setProperty(DEFAULT_MAP_PROVIDER, mapProvider.getTitle());
		}
		else {
			getInstance().setProperty(DEFAULT_MAP_PROVIDER, "");
		}
		getInstance().saveToDisk();
	}
	
	public static MapProvider getDefaultMapProvider() {
		String defaultMapProviderTitle = getInstance().getProperty(DEFAULT_MAP_PROVIDER);
		for(MapProvider mapProvider : MapProviderFactory.getMapProviders()){
			if ("".equalsIgnoreCase(defaultMapProviderTitle) || mapProvider.getTitle().equalsIgnoreCase(defaultMapProviderTitle)) {
				return mapProvider;
			}
		}
		return null;
	}
	
	public static void setDefaultLatitude(String latitude) {
		getInstance().setProperty(DEFAULT_LATITUDE, latitude);
		getInstance().saveToDisk();
	}
	
	public static double getDefaultLatitude() {
		try {
			return Double.parseDouble(getDefaultLatitudeString());
		}
		catch(Exception ex) {
			return 37.4419;
		}
	}
	
	public static String getDefaultLatitudeString() {
		String latitude = getInstance().getProperty(DEFAULT_LATITUDE);
		return latitude != null ? latitude : "37.4419";
	}
	
	public static void setDefaultLongitude(String longitude) {
		getInstance().setProperty(DEFAULT_LONGITUDE, longitude);
		getInstance().saveToDisk();
	}
	
	public static double getDefaultLongitude() {
		try {
			return Double.parseDouble(getDefaultLongitudeString());
		}
		catch(Exception ex) {
			return -100.1419;
		}
	}
	
	public static String getDefaultLongitudeString() {
		String longitude = getInstance().getProperty(DEFAULT_LONGITUDE);
		return longitude != null ? longitude : "-100.1419";
	}
}