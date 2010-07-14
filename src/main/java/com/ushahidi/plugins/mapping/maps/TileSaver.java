package com.ushahidi.plugins.mapping.maps;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import net.frontlinesms.FrontlineUtils;

import org.apache.log4j.Logger;

import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.geo.Location;
import com.ushahidi.plugins.mapping.maps.providers.microsoft.MicrosoftRoadProvider;

public class TileSaver implements TileRequestor {
		
	//Archive MANIFEST keys
	public static final String PROJECTION_ZOOM_PROPERTY = "projectionZoom";
	public static final String TRANSFORMATION_CY_PROPERTY = "TransformationCY";
	public static final String TRANSFORMATION_BY_PROPERTY = "TransformationBY";
	public static final String TRANSFORMATION_AY_PROPERTY = "TransformationAY";
	public static final String TRANSFORMATION_CX_PROPERTY = "TransformationCX";
	public static final String TRANSFORMATION_BX_PROPERTY = "TransformationBX";
	public static final String TRANSFORMATION_AX_PROPERTY = "TransformationAX";
	public static final String BTM_RIGHT_ZOOM_PROPERTY = "btmRightZoom";
	public static final String BTM_RIGHT_Y_PROPERTY = "btmRightY";
	public static final String BTM_RIGHT_X_PROPERTY = "btmRightX";
	public static final String TOP_LEFT_ZOOM_PROPERTY = "topLeftZoom";
	public static final String TOP_LEFT_Y_PROPERTY = "topLeftY";
	public static final String TOP_LEFT_X_PROPERTY = "topLeftX";
	public static final String MIN_ZOOM_PROPERTY = "minZoom";
	public static final String MANIFEST_FILE = "MANIFEST";
	public static final String MAX_ZOOM_PROPERTY = "maxZoom";
	
	private static final int MAP_HEIGHT = 1440;
	private static final int MAP_WIDTH = 900;
	private static final int THREAD_POOL_SIZE = 5;
	private static final int MAX_TILES = 256;

	private static final ExecutorService e = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

	public static final Logger LOG = FrontlineUtils.getLogger(TileSaver.class);

	private File tmpDir;
	private String targetFile;
	private boolean replace = true; // Flag whether to replace existing targetFile
	private int tiles2Download; // Total number of tiles requested
	private int tilesDownloaded; // Total number of tiles successfully downloaded
	private boolean queueing; // Flag whether tiles are still being queued
	private double minZoom;
	private double maxZoom;
	private Coordinate topLeft;
	private Coordinate btmRight;
	private TiledMap map;

	public TileSaver(TiledMap map, Coordinate topLeft, Coordinate btmRight, String targetFile, boolean replace) {
		this.targetFile = targetFile;
		this.replace = replace;
		this.topLeft = topLeft;
		minZoom = topLeft.zoom;
		this.btmRight = btmRight;
		this.map = map;
		tmpDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
	}
	
	public void startSave() {
		save(map, topLeft, btmRight, 0);
	}

	/**
	 * Recursively queue tile requests for the selected map area. Queues tile
	 * requests in the area specified by the topleft and btmRight coordinates,
	 * zooms in one level and repeats until the MAX_TILES or MAX_ZOOM limits are
	 * reached.
	 * 
	 * @param map
	 *            The map we're saving
	 * @param topLeft
	 *            top left coordinate of the map area we're saving in this
	 *            iteration
	 * @param btmRight
	 *            bottom right coordinate of the map area we're saving in this
	 *            iteration
	 * @param tileCount
	 *            total number of tiles saved in previous iterations
	 */
	public void save(TiledMap map, Coordinate topLeft, Coordinate btmRight,
			int tileCount) {

		Coordinate coord = topLeft.copy();

		// Check if we'll blow our tile count limit
		int tiles = (int) (btmRight.col - topLeft.col + 1) * (int) (btmRight.row - topLeft.row + 1) + tileCount;
		
		// Will we hit our cache limit or reached max zoom?
		if (tiles > MAX_TILES || coord.zoom > map.getProvider().getMaxZoom()) {
			// Flag we're done queueing
			queueing = false;
			return;
		}
		this.btmRight = btmRight;
		maxZoom = btmRight.zoom;

		// Queue tile requests
		int x = 0; // Tile offset
		int y = 0;
		queueing = true;
		while (coord.compareTo(btmRight) <= 0) {
			LOG.debug("Saving " + coord);
			TileRequest tile = new TileRequest(map.getProvider(), coord, new Point(
					x++, y), 0);
			tile.setRequestor(this);
			e.submit(tile);
			tiles2Download += 1;
			coord = coord.right();
			if (coord.col > btmRight.col) {
				coord = coord.down();
				x = 0;
				y += 1;
				coord.col = topLeft.col;
			}
		}

		// Save the next zoom level tiles.
		save(map, topLeft.zoomBy(1), btmRight.zoomBy(1).right().down(), tiles);
		LOG.debug("DONE");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ushahidi.plugins.mapping.maps.TileRequestor#renderTile(com.ushahidi
	 * .plugins.mapping.maps.TileRequest)
	 */
	public synchronized void renderTile(TileRequest tile) {
		Coordinate coord = tile.getCoord();
		LOG.debug("Got " + coord);
		File targetDir = new File(tmpDir, Integer.toString((int) coord.zoom));
		File targetFile = new File(targetDir, (int) tile.getCoord().row + "-"
				+ (int) tile.getCoord().col + ".png");
		if (!targetFile.getParentFile().exists()) {
			targetFile.getParentFile().mkdirs();
		}
		try {
			ImageIO.write(tile.getImage(), "PNG", targetFile);
			LOG.debug("Saved tile at " + targetFile.getAbsolutePath());
			if (++tilesDownloaded == tiles2Download & !queueing) {
				done();
			}
		} catch (IOException ex) {
			LOG.error("Error saving tile" + ex.toString());
		}
	}

	/**
	 * Called when all tiles are downloaded. Creates the targetFile archive
	 * containing the downloaded tiles.
	 */
	public void done() {
		e.shutdown();
		// Create our archive
		try {
			//Prepare the archive's manifest			
			Properties manifest = new Properties();			
			manifest.setProperty(MAX_ZOOM_PROPERTY, Double.toString(maxZoom));
			manifest.setProperty(MIN_ZOOM_PROPERTY, Double.toString(maxZoom));
			manifest.setProperty(TOP_LEFT_X_PROPERTY, Double.toString(topLeft.col));
			manifest.setProperty(TOP_LEFT_Y_PROPERTY, Double.toString(topLeft.row));
			manifest.setProperty(TOP_LEFT_ZOOM_PROPERTY, Double.toString(topLeft.zoom));
			manifest.setProperty(BTM_RIGHT_X_PROPERTY, Double.toString(btmRight.col));
			manifest.setProperty(BTM_RIGHT_Y_PROPERTY, Double.toString(btmRight.row));
			manifest.setProperty(BTM_RIGHT_ZOOM_PROPERTY, Double.toString(btmRight.zoom));
			manifest.setProperty(PROJECTION_ZOOM_PROPERTY, Double.toString(map.getProvider().getProjection().getZoom()));			
			manifest.setProperty(TRANSFORMATION_AX_PROPERTY, Double.toString(map.getProvider().getProjection().getTransformation().ax));
			manifest.setProperty(TRANSFORMATION_BX_PROPERTY, Double.toString(map.getProvider().getProjection().getTransformation().bx));
			manifest.setProperty(TRANSFORMATION_CX_PROPERTY, Double.toString(map.getProvider().getProjection().getTransformation().cx));
			manifest.setProperty(TRANSFORMATION_AY_PROPERTY, Double.toString(map.getProvider().getProjection().getTransformation().ay));
			manifest.setProperty(TRANSFORMATION_BY_PROPERTY, Double.toString(map.getProvider().getProjection().getTransformation().by));
			manifest.setProperty(TRANSFORMATION_CY_PROPERTY, Double.toString(map.getProvider().getProjection().getTransformation().cy));
			FileOutputStream manifestFile = new FileOutputStream(new File(tmpDir, MANIFEST_FILE));
			manifest.store(manifestFile, "---MAP PROPERTIES---");
			manifestFile.close();
			LOG.debug("Creating archive at: " + targetFile);
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFile));
			zipDir(tmpDir, zos);
			zos.close();
			LOG.debug("Done creating archive at: " + targetFile);
		} catch (FileNotFoundException ex) {
			LOG.debug("File not found exception " + e);
		} catch (IOException e) {
			LOG.debug("IOException while saving archive " + e);
		}
		LOG.debug("Done downloading tiles!");
	}

	private void zipDir(File dir2zip, ZipOutputStream zos) throws FileNotFoundException, IOException {
		// get a listing of the directory content
		String[] dirList = dir2zip.list();
		byte[] readBuffer = new byte[2156];
		int bytesIn = 0;
		// loop through dirList, and zip the files
		for (int i = 0; i < dirList.length; i++) {
			File f = new File(dir2zip, dirList[i]);
			if (f.isDirectory()) {
				// if the File object is a directory, call this
				// function again to add its content recursively
				zipDir(f, zos);
				// then loop again
				continue;
			}
				// if we reached here, the File object f was not a directory
				// create a FileInputStream on top of f
				FileInputStream fis = new FileInputStream(f);
				// create a new zip entry
				ZipEntry anEntry = new ZipEntry(f.getPath().substring(tmpDir.getPath().length()));
				// place the zip entry in the ZipOutputStream object
				zos.putNextEntry(anEntry);
				// now write the content of the file to the ZipOutputStream
				while ((bytesIn = fis.read(readBuffer)) != -1) {
					zos.write(readBuffer, 0, bytesIn);
				}
				// close the Stream
				fis.close();
		}	
	}
}
