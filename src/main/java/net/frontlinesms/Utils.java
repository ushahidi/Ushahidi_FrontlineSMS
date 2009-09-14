/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import net.frontlinesms.data.domain.*;
import net.frontlinesms.resources.ResourceUtils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import static net.frontlinesms.FrontlineSMSConstants.*;

/**
 * Class containing general helper methods that have nowhere better to live.
 * 
 * @author Alex Anderson alex(at)masabi(dot)com
 */
public class Utils {
	
//> CONSTANTS
	/** Logging object */
	private static Logger LOG = Utils.getLogger(Utils.class);
	/** Date formatter used in logs. */
	private static final SimpleDateFormat LOG_DATE_FORMATTER = new SimpleDateFormat();

	static {
		loadLogConfiguration();
	}

	/**
	 * Reloads the log configuration.
	 */
	static void loadLogConfiguration() {
		File f = new File(ResourceUtils.getConfigDirectoryPath() + ResourceUtils.DIRECTORY_PROPERTIES + File.separatorChar + "log4j.properties");
		if (f.exists()) {
			PropertyConfigurator.configure(f.getAbsolutePath());
		} else {
			PropertyConfigurator.configure(Utils.class.getResource("/log4j.properties"));
		}

	}

	/**
	 * Gets the logging object for a {@link Class}, making sure the expected configuration is used.
	 * @param clazz
	 * @return logging object for the supplied class
	 */
	public static Logger getLogger(Class<? extends Object> clazz) {
		Logger log = null;
		try {
			log = Logger.getLogger(clazz);
		} catch (Throwable t) {
			log = Logger.getRootLogger();
			log.removeAllAppenders();
			log = Logger.getLogger(clazz);
			ConsoleAppender app = new ConsoleAppender(new PatternLayout("[%t] %-5p [%d{dd/MM/yy HH:mm:ss}] %l - %m%n"));
			log.addAppender(app);
		}
		return log;
	}

	/**
	 * Converts a device manufacturer and model into a human-readable string, with extraneous information removed.
	 * @param manufacturer
	 * @param model
	 * @return string containing manufacturer and model information
	 */
	public static String getManufacturerAndModel(String manufacturer, String model) {
		if (model.startsWith(manufacturer)) model = model.substring(model.indexOf(manufacturer) + manufacturer.length()).trim();
		return manufacturer + ' ' + model;
	}

	/**
	 * Make the current thread sleep; ignore InterruptedExceptions.
	 * @param millis number of milliseconds to sleep the thread for.
	 */
	public static void sleep_ignoreInterrupts(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException ex) {
			LOG.debug("", ex);
		}
	}

	/**
	 * Returns a string with all contact groups.
	 * @param contact
	 * @param groups_delimiter
	 * @return
	 */
	public static String contactGroupsAsString(Contact contact, String groups_delimiter) {
		String groups = "";
		for (Group g : contact.getGroups()) {
			groups += g.getName() + groups_delimiter;
		}
		if (groups.endsWith(groups_delimiter)) {
			groups = groups.substring(0, groups.length() - groups_delimiter.length());
		}
		return groups;
	}

	/**
	 * Compares the two supplied dates.
	 * @param startDate Start date
	 * @param endDate End Data
	 * @return <code>true</code> if the start date is before the end date; <code>false</code> otherwise.
	 */
	public static boolean validateDates(Date startDate, Date endDate) {
		return startDate.compareTo(endDate) <= 0;
	}

	/**
	 * Reads the version of FrontlineSMS from MANIFEST file
	 * @return FrontlineSMS version, in whatever form it is specified in the manifest.
	 */
	public static String getVersion() {
		String ret = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(ResourceUtils.class.getResourceAsStream("/MANIFEST.MF")));
			String line;
			while ( (line = reader.readLine()) != null) {
				if (line.toLowerCase().startsWith("version")) {
					String[] parts = line.split(":", 2);
					if (parts.length == 2) {
						ret = parts[1].trim();
					}
				}
			}
		} catch (Exception e) {
			LOG.debug("Problem reading manifest", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					LOG.debug("Closing manifest", e);
				}
		}
		return ret;
	}

	/**
	 * Parse the supplied string into a date.
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDateForKeyword(String date) throws ParseException {
		return Dependants.DEFAULT_KEYWORD_ACTION_DATE_FORMAT.parse(date);
	}

	/**
	 * This method makes a http request and returns the response according to the supplied parameter.
	 * @param url URL to connect.
	 * @param waitForResponse
	 * @return
	 * @throws IOException
	 */
	public static String makeHttpRequest(String url, boolean waitForResponse) throws IOException {
		LOG.trace("ENTER");
		String str = "";
		URL hp = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) hp.openConnection();
		int rc = conn.getResponseCode();
		LOG.debug("RC = " + rc);
		if (rc == HttpURLConnection.HTTP_OK) {
			InputStream input = conn.getInputStream();
			LOG.debug("Wait for response [" + waitForResponse + "]");
			if (waitForResponse) {
				// Don't check the MIME type here - we don't want to confuse anybody
				// Get response data.
				BufferedReader inputData = new BufferedReader(new InputStreamReader(input));
				StringBuilder sb = new StringBuilder();
				while (null != (str = inputData.readLine())) {
					sb.append(str + "\n");
				}
				str = sb.toString();
			}
		}
		LOG.trace("EXIT");
		return str;
	}

	/**
	 * This method makes a http request and returns the input stream.
	 * @param url URL to connect.
	 * @return
	 * @throws IOException
	 */
	public static InputStream makeHttpRequest(String url) throws IOException {
		LOG.trace("ENTER");
		URL hp = new URL(url);
		URLConnection conn = hp.openConnection();
		conn.connect();
		LOG.trace("EXIT");
		return conn.getInputStream();
	}

	/**
	 * This method executes a external command and returns the input stream.
	 * @param cmd Command to be executed.
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static InputStream executeExternalProgram(String cmd) throws IOException, InterruptedException {
		LOG.trace("ENTER");
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
		LOG.trace("EXIT");
		return p.getInputStream();
	}

	/**
	 * This method executes a external command and returns the response according to the supplied parameter.
	 * @param cmd Command to be executed.
	 * @param waitForResponse
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static String executeExternalProgram(String cmd, boolean waitForResponse) throws IOException, InterruptedException {
		LOG.trace("ENTER");
		String str = "";
		Process p = Runtime.getRuntime().exec(cmd);
		LOG.debug("Wait for response [" + waitForResponse + "]");
		if (waitForResponse) {
			int exit = p.waitFor();
			LOG.debug("Process exit value [" + exit + "]");
			if (exit == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				StringBuilder sb = new StringBuilder();
				while (null != ((str = br.readLine()))) {
					sb.append(str + "\n");
				}
				str = sb.toString();
			}
		}
		LOG.trace("EXIT");
		return str;
	}

	/**
	 * Encodes the supplied string into Base64.
	 * FIXME should re-implement this so we are not relying on com.sun package
	 * @param password the string to encode
	 * @return base64-encoded string
	 */
	public static String encodeBase64(String password) {
		return new BASE64Encoder().encode(password.getBytes());
	}

	/**
	 * Decodes the supplied string from Base64.
	 * FIXME should re-implement this so we are not relying on com.sun package
	 * @param passwordEncrypted
	 * @return decoded value of the supplied base64 string
	 */
	public static String decodeBase64(String passwordEncrypted) {
		BASE64Decoder decoder = new BASE64Decoder();
		String decoded = null;
		try {
			decoded = new String(decoder.decodeBuffer(passwordEncrypted));
		} catch (IOException e) {
			LOG.debug("Error decoding", e);
		}
		return decoded;
	}

	/**
	 * Finds the first action for the supplied type.
	 * @param keyword
	 * @param keyType
	 * @return first action of the specified type attached to the keyword, or <code>null</code> if none could be found
	 */
	public static KeywordAction findKeywordAction(Keyword keyword, int keyType) {
		for (KeywordAction action : keyword.getActions()) {
			int type = action.getType();
			if (type == keyType) {
				return action;
			}
		}
		return null;
	}

	/**
	 * This class compares files and directories, giving higher priority to directories.
	 * 
	 * @author Carlos Eduardo Genz
	 */
	public static class FileComparator implements Comparator<File> {
		public int compare(File arg0, File arg1) {
			if (arg0.isDirectory() && arg1.isDirectory()) {
				return 0;
			} else if (arg0.isDirectory() && !arg1.isDirectory()) {
				return -1;
			} else return 1;
		}
	}

	/** prioritised guess of users' browser preference */
	private static final String[] BROWSERS = {"epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links", "lynx"};

	/**
	 * This method assumes that any URLs starting with something other than http:// are
	 * links to the FrontlineSMS help manual.  On Linux and Windows machines, this is
	 * assumed to be local.  On Mac OSX, we link to a website.
	 * FIXME where did this code come from?
	 * @param url
	 */
	@SuppressWarnings("unchecked")
	public static void openExternalBrowser(String url) {
		String os = System.getProperty("os.name").toLowerCase();
		Runtime rt = Runtime.getRuntime();
		try {
			if (os.startsWith("win")) {
				LOG.info("Attempting to open URL with Windows-specific code");
				String[] cmd = new String[4];
				cmd[0] = "cmd.exe";
				cmd[1] = "/C";
				cmd[2] = "start";
				cmd[3] = url;
				rt.exec(cmd);
			} else if (os.startsWith("mac")) {
				LOG.info("Attempting to open URL with Mac-specific code");

				if(!url.startsWith("http://")) {
					// It seems like we don't have permission to open a file inside a .app package
					// on OSX.  While we are packaging the mac version as a .app, we access help on
					// the FrontlineSMS website.
					LOG.debug("Rewriting local url '" + url + "'...");
					String workingDirectory = System.getProperty("user.dir");
					LOG.debug("Working directory: '" + workingDirectory + "'");
//					url = workingDirectory + "/FrontlineSMS.app/Contents/Resources/" + url;
					url = "http://www.frontlinesms.com/" + url;
					LOG.debug("URL rewritten as '" + url + "'");
				}

				// TODO here, we are trying to launch the browser twice.  This looks to only open
				// one browser instance, so I'd guess one of them was failing.  If it's the same
				// one every time, we can get rid of the other method.
				LOG.debug("Trying to open with rt.exec....");
				rt.exec("open " + url);

				LOG.debug("Trying to open with FileManager...");
				Class fileManager = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileManager.getDeclaredMethod("openURL", String.class);
				openURL.invoke(null, new Object[] {url});
			} else {
				LOG.info("Attempting to open URL with default code");
				StringBuilder cmd = new StringBuilder();
				for (int i=0; i < BROWSERS.length; i++)
					cmd.append( (i==0  ? "" : " || " ) + BROWSERS[i] +" \"" + url + "\" ");

				rt.exec(new String[] { "sh", "-c", cmd.toString() });
			}
		} catch (Throwable t) {
			LOG.warn("Could not open browser (" + url + ")", t);
		}
	}

	/**
	 * Creates an image from the specified resource.
	 * To speed up loading the same images use a cache (a simple hashtable).
	 * And flush the resources being used by an image when you won't use it henceforward
	 *
	 * @param path is relative or the classpath, or an URL
	 * @param clazz TODO
	 * @return the loaded image or null
	 */
	public static Image getImage(String path, Class<?> clazz) {
		if ((path == null) || (path.length() == 0)) {
			return null;
		}
		Image image = null; //(Image) imagepool.get(path);
		try {
			URL url = clazz.getResource(path); //ClassLoader.getSystemResource(path)
			if (url != null) { // contributed by Stefan Matthias Aust
				image = Toolkit.getDefaultToolkit().getImage(url);
			}
		} catch (Throwable e) {}
		if (image == null) {
			try {
				InputStream is = clazz.getResourceAsStream(path);
				//InputStream is = ClassLoader.getSystemResourceAsStream(path);
				if (is != null) {
					byte[] data = new byte[is.available()];
					is.read(data, 0, data.length);
					image = Toolkit.getDefaultToolkit().createImage(data);
					is.close();
				}
				else { // contributed by Wolf Paulus
					image = Toolkit.getDefaultToolkit().getImage(new URL(path));
				}
			} catch (Throwable e) {}
		}
		return image;
	}

	/**
	 * Formats a date for use in logging - should use default SimpleDateFormat
	 * style rather than a localised format.  For this reason, this should not
	 * be used for dates that are to be displayed to the user.
	 * @param date the date to be formatted
	 * @return date string represetnation of a date, suitably formatted for use in logs
	 */
	public static String log_formatDate(long date) {
		return LOG_DATE_FORMATTER.format(new Date(date));
	}

	/**
	 * Parses a string, and substitutes markers for replacements.  The replacement is fairly
	 * simplistic, so it is recommended that markers are provided in the form ${marker_name}
	 * so that they are unlikely to overlap.  If a marker's replacement is <code>null</code>,
	 * then this method will not attempt to replace that marker.
	 * @param initialString 
	 * @param markersAndReplacements List of markers and their replacements.  Each marker should be followed directly by its replacement in this list.
	 * @return string with markers replaced with their respective values
	 */
	public static String replace(String initialString, String... markersAndReplacements) {
		if((markersAndReplacements.length&1) == 1) throw new IllegalArgumentException("Each marker must have a replacement!  Odd number of markers+replacements provided: " + markersAndReplacements.length);
		for (int i = 0; i < markersAndReplacements.length; i+=2) {
			String replacement = markersAndReplacements[i+1];
			if(replacement != null) {
				initialString = initialString.replace(markersAndReplacements[i], replacement);
			}
		}
		return initialString;
	}

	/**
	 * Calls {@link URLEncoder#encode(String, String)} using UTF-8 as the encoding.  If somehow
	 * an {@link UnsupportedEncodingException} is thrown, this method will just return the original
	 * {@link String} supplied.  This method will also ignore <code>null</code> inputs, rather than
	 * throwing a {@link NullPointerException}.
	 * @param string
	 * @return url-encoded string
	 */
	public static String urlEncode(String string) {
		if(string == null) return null;
		try {
			string = URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) { /* This will never happen - UTF-8 should always be supported by every JVM. */ }
		return string;
	}
}
