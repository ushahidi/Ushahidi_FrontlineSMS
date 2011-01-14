package com.ushahidi.plugins.mapping.maps;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("serial")
public class TileCache extends
		java.util.LinkedHashMap<String, ArrayList<BufferedImage>> {

	private static final long cacheSize = 500; // Cache size in number of tiles.
												// At a pesimistic 8KB per tile
												// this is ~4MB in memory.

	@Override
	protected boolean removeEldestEntry(
			Map.Entry<String, ArrayList<BufferedImage>> eldest) {
		return size() > cacheSize;
	}

}
