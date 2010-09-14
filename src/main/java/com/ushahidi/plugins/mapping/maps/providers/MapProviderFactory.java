package com.ushahidi.plugins.mapping.maps.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import com.ushahidi.plugins.mapping.utils.MappingLogger;

/**
 * MapProviderFactory
 * @author dalezak
 *
 */
public class MapProviderFactory {
	
	private static MappingLogger LOG = MappingLogger.getLogger(MapProviderFactory.class);	
	
	public static List<MapProvider> getMapProviders() {
		if (mapProviders == null) {
			mapProviders = new ArrayList<MapProvider>();
			LOG.debug("Loading MapProviders...");
			try {
				for (MapProvider mapProvider : ServiceLoader.load(MapProvider.class)) {
					LOG.debug("Loaded MapProvider: %s", mapProvider.getTitle());
					mapProviders.add(mapProvider);
			    }
			}
			catch (ServiceConfigurationError ex) {
				LOG.error("ServiceConfigurationError: %s", ex);
			}
		}
		return mapProviders;
	}private static List<MapProvider> mapProviders = null;
	
}