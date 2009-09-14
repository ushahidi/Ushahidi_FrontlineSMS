package com.ushahidi.plugins.mapping.maps;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.core.Point;
import com.ushahidi.plugins.mapping.maps.providers.AbstractMapProvider;

public class TileRequest implements Runnable {

	public static final int MAX_ATTEMPTS = 5;
	private Point offset;
	private long updateId;

	int attempts = 1;
	Boolean done;
	AbstractMapProvider provider;
	private Coordinate coord;
	private TileRequestor requestor;

	public static Logger LOG = Utils.getLogger(TileRequest.class);

	private static final java.util.Map<String, ArrayList<BufferedImage>> tileCache = Collections
			.synchronizedMap(new HashMap<String, ArrayList<BufferedImage>>());

	public TileRequest(AbstractMapProvider provider, Coordinate coord,
			Point offset, long updateId) {
		this.done = false;
		this.provider = provider;
		this.setCoord(coord);
		this.setOffset(offset);
		this.updateId = updateId;
	}

	public boolean isLoaded() {
		String tileKey = provider.getTileId(getCoord());
		if (!tileCache.containsKey(tileKey)) {
			tileCache.put(tileKey, new ArrayList<BufferedImage>());
		}
		ArrayList<BufferedImage> imgs = tileCache.get(tileKey);
		return !imgs.isEmpty();
	}

	public BufferedImage getImage() {
		BufferedImage ret = null;
		String tileKey = provider.getTileId(getCoord());
		if (tileCache.containsKey(tileKey)) {
			// FIXME tiles can be several layers of images...
			ret = tileCache.get(tileKey).get(0);
		}
		return ret;
	}

	/**
	 * Loads the tile's bytes if not already in the cache and calls the
	 * requestor's renderTile method when done.
	 */
	public void load() {
		String tileKey = provider.getTileId(getCoord());
		if (!tileCache.containsKey(tileKey)) {
			tileCache.put(tileKey, new ArrayList<BufferedImage>());
		}

		ArrayList<BufferedImage> imgs = tileCache.get(tileKey);
		// Acquire lock on the tile images. Thread with lock will load
		// tile if not already loaded
		LOG.debug("Acquiring lock on " + getCoord().toString());
		synchronized (imgs) {
		LOG.debug("Lock acquired ");
			if (imgs.isEmpty()) {
				List<String> urls = provider.getTileUrls(getCoord());
				tileCache.put(tileKey, imgs);

				for (String url : urls) {
					LOG.debug("Loading tile at " + url);

					// Read the Image
					BufferedImage image;
					try {
						image = ImageIO.read(new URL(url));
						LOG.debug("Tile loaded " + url);
						imgs.add(image);
						done = true;
					} catch (MalformedURLException e) {
						LOG.debug(e);
					} catch (IOException e) {
						LOG.debug(e);
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

	public void setCoord(Coordinate coord) {
		this.coord = coord;
	}

	public Coordinate getCoord() {
		return coord;
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
