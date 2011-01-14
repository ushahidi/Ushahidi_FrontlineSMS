package com.ushahidi.plugins.mapping.maps;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.imageio.ImageIO;

import net.frontlinesms.resources.ResourceUtils;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.providers.MapProvider;
import com.ushahidi.plugins.mapping.util.MappingLogger;

/**
 * TileRequest
 * @author dalezak
 *
 */
public class TileRequest implements Runnable {

	public static final int MAX_ATTEMPTS = 5;
	private Point offset;
	private long updateId;

	int attempts = 1;
	Boolean done;
	MapProvider provider;
	private Coordinate coordinate;
	private TileRequestor requestor;

	private static final MappingLogger LOG = new MappingLogger(TileRequest.class);	
	
	private static final Map<String, ArrayList<BufferedImage>> tileCache = Collections.synchronizedMap(new TileCache());

	public TileRequest(MapProvider provider, Coordinate coordinate, Point offset, long updateId) {
		this.done = false;
		this.provider = provider;
		this.setCoordinate(coordinate);
		this.setOffset(offset);
		this.updateId = updateId;
	}

	public boolean isLoaded() {
		String tileKey = provider.getTileId(getCoordinate());
		if (!tileCache.containsKey(tileKey)) {
			tileCache.put(tileKey, new ArrayList<BufferedImage>());
			LOG.debug("Tile cache size now at %d tiles. Free memory at %dKB", tileCache.size(), Runtime.getRuntime().freeMemory()/1024);
		}
		return !tileCache.get(tileKey).isEmpty();
	}

	public BufferedImage getImage() {
		BufferedImage image = null;
		String tileKey = provider.getTileId(getCoordinate());
		if (tileCache.containsKey(tileKey)) {
			image = tileCache.get(tileKey).get(0);
		}
		return image;
	}

	/**
	 * Loads the tile's bytes if not already in the cache and calls the
	 * requestor's renderTile method when done.
	 */
	public void load() {
		String tileKey = provider.getTileId(getCoordinate());
		if (!tileCache.containsKey(tileKey)) {
			tileCache.put(tileKey, new ArrayList<BufferedImage>());
			LOG.debug("Tile cache size now at %d tiles. Free memory at %dKB", tileCache.size(), Runtime.getRuntime().freeMemory()/1024);
		}
		ArrayList<BufferedImage> images = tileCache.get(tileKey);
		File mapsDirectory = new File(ResourceUtils.getConfigDirectoryPath(), "maps");
		if (mapsDirectory.exists() == false) {
			mapsDirectory.mkdir();
			LOG.debug("Directory Created: %s", mapsDirectory.getAbsolutePath());
		}
		// Acquire lock on the tile images. Thread with lock will load tile if not already loaded
		synchronized (images) {
			if (images.isEmpty()) {
				Coordinate coordinate = getCoordinate();
				tileCache.put(tileKey, images);
				for (String url : provider.getTileUrls(coordinate)) {
					File file = new File(mapsDirectory, provider.getTileName(coordinate));
					if (file.exists()) {
						try {
							BufferedImage image = ImageIO.read(file);
							LOG.debug("Tile Loaded: %s", file.getAbsolutePath());
							images.add(image);
							done = true;
						} 
						catch (IOException e) {
							LOG.error(e);
						}
					}
					else {
						try {
							LOG.debug("Tile Downloading: %s", url);
							BufferedImage image = ImageIO.read(new URL(url));
							LOG.debug("Tile Downloaded: %s", url);
							ImageIO.write(image, "png", file);
							LOG.debug("Tile Saved: %s", file.getAbsolutePath());
							images.add(image);
							done = true;
						} 
						catch (MalformedURLException e) {
							LOG.error(e);
						} 
						catch (IOException e) {
							LOG.error(e);
						}	
					}
				}
			}
		}
		if (requestor != null) {
			requestor.renderTile(this);
		}
	}

	public void run() {
		load();
	}

	public void setRequestor(TileRequestor requestor) {
		this.requestor = requestor;
	}

	public void setCoordinate(Coordinate coord) {
		this.coordinate = coord;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setOffset(Point offset) {
		this.offset = offset;
	}

	public Point getOffset() {
		return offset;
	}

	public long getUpdateId() {
		return updateId;
	}

	public void setUpdateId(long updateId) {
		this.updateId = updateId;
	}
	
}
