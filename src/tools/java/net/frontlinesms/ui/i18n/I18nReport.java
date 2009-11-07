/**
 * 
 */
package net.frontlinesms.ui.i18n;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A report about internationalisation
 * @author Alex
 */
public class I18nReport {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/** The name of the language bundle file */
	private final String bundleFileName;
	/** Keys missing from the language bundle which were referenced in code */
	private final Map<String, Set<Field>> missingCodeKeys = new TreeMap<String, Set<Field>>();
	/** Keys missing from the language bundle which were referenced in XML */
	private final Map<String, Set<File>> missingXmlKeys = new TreeMap<String, Set<File>>();
	/** Keys in the language bundle which are not needed */
	private final Set<String> unnecessaryKeys = new TreeSet<String>();

//> CONSTRUCTORS
	/**
	 * Processes the language bundle and checks it against the checker.
	 * @param languageChecker 
	 * @param languageBundleFile 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@SuppressWarnings("unchecked")
	public I18nReport(LanguageChecker languageChecker, File languageBundleFile) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		this.bundleFileName = languageBundleFile.getName();
		LanguageBundle languageBundle = InternationalisationUtils.getLanguageBundle(languageBundleFile);
		
		// Get all keys in the bundle
		Set<String> bundleKeys = new TreeSet<String>();
		Field bundleKeysField = languageBundle.getClass().getDeclaredField("properties");
		bundleKeysField.setAccessible(true);
		bundleKeys.addAll(((Map<String, String>) bundleKeysField.get(languageBundle)).keySet());
		
		// 1. Check the keys that are used in code
		for(String i18nKey : languageChecker.getI18nKeysInCode().keySet()) {
			try {
				// Try getting the value from the language bundle
				languageBundle.getValue(i18nKey);
				// Remove the key from the bundleKeys so we know if we have any unnecessary ones 
				bundleKeys.remove(i18nKey);
			} catch(MissingResourceException ex) {
				this.missingCodeKeys.put(i18nKey, languageChecker.getI18nKeysInCode().get(i18nKey));
			}
		}
		
		// 2. Check the keys that are used in XML
		for(String i18nKey : languageChecker.getI18nKeysInXml().keySet()) {
			try {
				// Try getting the value from the language bundle
				languageBundle.getValue(i18nKey);
				// Remove the key from the bundleKeys so we know if we have any unnecessary ones 
				bundleKeys.remove(i18nKey);
			} catch(MissingResourceException ex) {
				this.missingXmlKeys.put(i18nKey, languageChecker.getI18nKeysInXml().get(i18nKey));
			}
		}
		
		this.unnecessaryKeys.addAll(bundleKeys);
	}

//> ACCESSORS
	/** @return {@link #bundleFileName} */
	public String getBundleFileName() {
		return bundleFileName;
	}
	/** @return {@link #missingCodeKeys} */
	public Map<String, Set<Field>> getMissingCodeKeys() {
		return missingCodeKeys;
	}
	/** @return {@link #missingXmlKeys} */
	public Map<String, Set<File>> getMissingXmlKeys() {
		return missingXmlKeys;
	}
	/** @return {@link #unnecessaryKeys} */
	public Set<String> getUnnecessaryKeys() {
		return unnecessaryKeys;
	}
	/** @return all keys missing from the translation */
	public Set<String> getAllMissingKeys() {
		TreeSet<String> missingKeys = new TreeSet<String>();
		missingKeys.addAll(this.missingCodeKeys.keySet());
		missingKeys.addAll(this.missingXmlKeys.keySet());
		return missingKeys;
	}

//> INSTANCE HELPER METHODS

//> INSTANCE METHODS
	/**
	 * Prints details of the report to the supplied stream.
	 * @param out
	 * @param printKeys 
	 */
	public void output(PrintStream out, boolean printKeys) {
		out.println("> REPORT BEGINS ----------");
		out.println("> Filename: " + this.bundleFileName);
		out.println("> Missing keys (code): " + this.missingCodeKeys.size());
		if(printKeys) {
			for(String missingKey : this.missingCodeKeys.keySet()) {
				out.println("\t" + missingKey);
				out.println("\t\t" + this.missingCodeKeys.get(missingKey));
			}
		}
		out.println("> Missing keys (XML): " + this.missingXmlKeys.size());
		if(printKeys) {
			for(String missingKey : this.missingXmlKeys.keySet()) {
				out.println("\t" + missingKey);
				out.println("\t\t" + this.missingXmlKeys.get(missingKey));
			}
		}
		out.println("> Unnecessary keys: " + this.unnecessaryKeys.size());
		if(printKeys) {
			for(String missingKey : this.unnecessaryKeys) {
				out.println("\t" + missingKey);
			}
		}
		out.println("> REPORT ENDS ----------");
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
