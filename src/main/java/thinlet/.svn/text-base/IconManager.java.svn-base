/**
 * 
 */
package thinlet;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Manages icons used by a Thinlet instance.
 * @author Alex
 */
public class IconManager {
	/** Thinlet object that owns this manager. */
	private final Thinlet thinlet;
	/** Icons stored by the manager. */
	private final HashMap<String, Image> icons = new HashMap<String, Image>();

//> CONSTRUCTORS
	/**
	 * Create a new instance of this class.
	 * @param thinlet Thinlet that owns this manager
	 */
	IconManager(Thinlet thinlet) {
		this.thinlet = thinlet;
	}
	
//> ACCESSOR METHODS
	/**
	 * Gets an icon from {@link #icons}.
	 * @param location path to the icon
	 * @return The icon, or <code>null</code> if it could not be found.
	 */
	public synchronized Image getIcon(String location) {
		if(location == null) {
			return null;
		}
		
		if(!icons.containsKey(location)) {
			// The icon is not loaded yet, so load it now.
			this.icons.put(location, this.getIcon(location, true));
		}
		
		return this.icons.get(location);
	}

	/**
	 * Creates an image from the specified resource.
	 * To speed up loading the same images use a cache (a simple hashtable).
	 * And flush the resources being used by an image when you won't use it henceforward
	 *
	 * @param path is relative to your thinlet instance or the classpath, or an URL
	 * @param preload waits for the whole image if true, starts loading
	 * (and repaints, and updates the layout) only when required (painted, or size requested) if false
	 * @return the loaded image or null
	 */
	private Image getIcon(String path, boolean preload) {
		if ((path == null) || (path.length() == 0)) {
			return null;
		}
		Image image = null;
		try {
			URL url = getClass().getResource(path); //ClassLoader.getSystemResource(path)
			if (url != null) { // contributed by Stefan Matthias Aust
				image = Toolkit.getDefaultToolkit().getImage(url);
			}
		} catch (Throwable e) {}
		if (image == null) {
			try {
				InputStream is = getClass().getResourceAsStream(path);
				if (is != null) {
					byte[] data = new byte[is.available()];
					is.read(data, 0, data.length);
					image = thinlet.getToolkit().createImage(data);
					is.close();
				}
				else { // contributed by Wolf Paulus
					image = Toolkit.getDefaultToolkit().getImage(new URL(path));
				}
			} catch (Throwable e) {}
		}
		if (preload && (image != null)) {
			MediaTracker mediatracker = new MediaTracker(this.thinlet);
			mediatracker.addImage(image, 1);
			try {
				mediatracker.waitForID(1, 5000);
			} catch (InterruptedException ie) { }
			//imagepool.put(path, image);
		} 
		return image;
	}
}
