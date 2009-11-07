/**
 * 
 */
package net.frontlinesms.ui.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;

/**
 * File for printing out updated translation files
 * @author Alex
 */
public class TranslationEmitter {
//> STATIC CONSTANTS
	/** Translation value for a string that still needs doing */
	private static final String TRANSLATION_TODO = "(TODO)";
	/** File encoding name: UTF-8 */
	private static final String ENCODING_UTF8 = "UTF-8";
	
	/** Translation keys that are protected.  These keys are embedded in a {@link LanguageBundle} but are not actually for translation, e.g. {@link LanguageBundle#KEY_LANGUAGE_CODE} */
	private static final String[] PROTECTED_KEYS = {
		LanguageBundle.KEY_FONT_NAME,
		LanguageBundle.KEY_LANGUAGE_CODE,
		LanguageBundle.KEY_LANGUAGE_COUNTRY,
		LanguageBundle.KEY_LANGUAGE_NAME,
		LanguageBundle.KEY_RIGHT_TO_LEFT,
	};

//> INSTANCE PROPERTIES
	/** The english language bundle to take all missing keys from */
	private final LanguageBundle defaultLanguageBundle;
	/** Directory to output translations to */
	private final File outputDirectory;

//> CONSTRUCTORS

//> ACCESSORS

//> INSTANCE HELPER METHODS

	/**
	 * @param languageBundle
	 * @param outputDirectory 
	 */
	public TranslationEmitter(LanguageBundle languageBundle, File outputDirectory) {
		this.defaultLanguageBundle = languageBundle;
		this.outputDirectory = outputDirectory;
	}

	/**
	 * Processes a language bundle to remove all unnecessary keys and add all missing ones with {@link #toString()}
	 * @param languageBundle
	 * @param report
	 * @throws UnsupportedEncodingException 
	 * @throws IOException 
	 */
	public void processBundle(File languageBundle, I18nReport report) throws UnsupportedEncodingException, IOException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(languageBundle), ENCODING_UTF8));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.outputDirectory, languageBundle.getName())), ENCODING_UTF8));
			String line;
			while((line = reader.readLine()) != null) {
				// Check that the line we have read does not start with one of the unnecessary keys
				if(isNecessary(report, line)) {
					writer.write(line + "\r\n");
				}
			}
			
			// Now add all missing lines to the writer
			writer.write("\r\n");
			writer.write("\r\n");
			writer.write("### MISSING TRANSLATIONS ###\r\n");
			writer.write("# (please remove these comments when the translation has been inserted) ###\r\n");
			writer.write("# Remove " + TRANSLATION_TODO + " and translate the English into the correct language. ###\r\n");
			writer.write("# If there is no English text you'd better check what it's meant to say. ###\r\n");
			for(String key : report.getAllMissingKeys()) {
				String value = TRANSLATION_TODO;
				try {
					value += " " + this.defaultLanguageBundle.getValue(key);
					
				} catch(MissingResourceException ex) { /* ignore missing keys */ }
				writer.write(key + "=" + value + "\r\n");
			}
			writer.write("### MISSING TRANSLATIONS ###\r\n");
		} finally {
			// Close streams
			if(reader != null) try { reader.close(); } catch(IOException ex) { /* do nothing */ }
			if(writer != null) try { writer.close(); } catch(IOException ex) { /* do nothing */ }
		}
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * Checks if a line from a language bundle contains an unnecessary translation or not.
	 * @param report
	 * @param bundleLine
	 * @return <code>true</code> if the line should be kept; <code>false</code> if it should be removed
	 */
	private static boolean isNecessary(I18nReport report, String bundleLine) {
		if(bundleLine.length() == 0 || bundleLine.charAt(0) == '#') {
			return true;
		}
		
		for(String protectedKey : PROTECTED_KEYS) {
			if(bundleLine.startsWith(protectedKey + "=")) return true;
		}
		
		// Check all unnecessary keys, and if this is in them, return false
		for(String key : report.getUnnecessaryKeys()) {
			if(bundleLine.startsWith(key + "=")) return false;
		}
		
		return true;
	}
}
