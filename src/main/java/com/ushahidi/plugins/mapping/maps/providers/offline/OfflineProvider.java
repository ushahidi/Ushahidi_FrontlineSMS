package com.ushahidi.plugins.mapping.maps.providers.offline;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.frontlinesms.FrontlineUtils;

import org.apache.log4j.Logger;

import com.ushahidi.plugins.mapping.maps.TileSaver;
import com.ushahidi.plugins.mapping.maps.core.Coordinate;
import com.ushahidi.plugins.mapping.maps.geo.MercatorProjection;
import com.ushahidi.plugins.mapping.maps.geo.Transformation;
import com.ushahidi.plugins.mapping.maps.providers.MapProvider;

public class OfflineProvider extends MapProvider {

    public static final Logger LOG = FrontlineUtils.getLogger(OfflineProvider.class);

    private File tmpDir;

	@Override
	public String getTitle() {
		return "Offline Mapping Provider";
	}  
	
    /**
     * @param archive
     *            Filename of the map archive
     * @throws IOException
     */
    public OfflineProvider(String archive) throws IOException {
        super(MIN_ZOOM, MAX_ZOOM);

        // Unzip archive to temp dir
        tmpDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        unzipArchive(archive);

        // Get the map's properties from MANIFEST
        Properties manifest = new Properties();
        manifest.load(new FileInputStream(new File(tmpDir,TileSaver.MANIFEST_FILE)));
        MIN_ZOOM = Integer.parseInt(manifest.getProperty(TileSaver.MIN_ZOOM_PROPERTY));
        MAX_ZOOM = Integer.parseInt(manifest.getProperty(TileSaver.MAX_ZOOM_PROPERTY));

        // Projection
        int zoom = Integer.parseInt(manifest.getProperty(TileSaver.PROJECTION_ZOOM_PROPERTY));

        double ax = Double.parseDouble(manifest.getProperty(TileSaver.TRANSFORMATION_AX_PROPERTY));
        double bx = Double.parseDouble(manifest.getProperty(TileSaver.TRANSFORMATION_BX_PROPERTY));
        double cx = Double.parseDouble(manifest.getProperty(TileSaver.TRANSFORMATION_CX_PROPERTY));
        double ay = Double.parseDouble(manifest.getProperty(TileSaver.TRANSFORMATION_AY_PROPERTY));
        double by = Double.parseDouble(manifest.getProperty(TileSaver.TRANSFORMATION_BY_PROPERTY));
        double cy = Double.parseDouble(manifest.getProperty(TileSaver.TRANSFORMATION_CY_PROPERTY));

        Transformation t = new Transformation(ax, bx, cx, ay, by, cy);
        projection = new MercatorProjection(zoom, t);

        // Bounds
        double row = Double.parseDouble(manifest.getProperty(TileSaver.TOP_LEFT_Y_PROPERTY));
        double col = Double.parseDouble(manifest.getProperty(TileSaver.TOP_LEFT_X_PROPERTY));
        zoom = Integer.parseInt(manifest.getProperty(TileSaver.TOP_LEFT_ZOOM_PROPERTY));

        topLeftOutLimit = new Coordinate(row, col, zoom);

        row = Double.parseDouble(manifest.getProperty(TileSaver.BTM_RIGHT_Y_PROPERTY));
        col = Double.parseDouble(manifest.getProperty(TileSaver.BTM_RIGHT_X_PROPERTY));
        zoom = Integer.parseInt(manifest.getProperty(TileSaver.BTM_RIGHT_ZOOM_PROPERTY));

        bottomRightInLimit = new Coordinate(row, col, zoom);

    }

    private void unzipArchive(String archive) throws IOException {
        ZipFile zipFile = new ZipFile(archive);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            File targetFile = new File(tmpDir, entry.getName());
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }

            LOG.debug("Extracting file: " + targetFile.getAbsolutePath());
            copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(targetFile)));
        }
        zipFile.close();
    }

    private void copyInputStream(InputStream in, OutputStream out)
    throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
    }

    public int tileWidth() {
        return 256;
    }

    public int tileHeight() {
        return 256;
    }

    @Override
    public String getTileId(Coordinate coordinate) {		
        return tmpDir.getName() + coordinate;
    }

    @Override
    public List<String> getTileUrls(Coordinate coordinate) {	
        ArrayList<String> ret = new ArrayList<String>();		
        ret.add("file://" + tmpDir.getAbsolutePath() + File.separator + (int)coordinate.zoom + File.separator + (int)coordinate.row + "-" + (int)coordinate.col + ".png");
        return ret;
    }

}
