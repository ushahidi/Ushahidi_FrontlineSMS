/**
 * 
 */
package net.frontlinesms.ui.i18n;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.plugins.forms.ui.FormsThinletTabController;
import net.frontlinesms.ui.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import thinlet.Thinlet;

/**
 * Tool for checking if all required internationalisation strings are available in all language bundles, and if there are any extraneous translation strings in the bundles.
 * @author Alex
 */
public class LanguageChecker {
	
//> STATIC CONSTANTS
	/** Directory to output generated files to */
	private static final File OUTPUT_DIRECTORY = new File("out");
	/** Directory of the language files, hardcoded for now */
	private static final String LANGUAGEBUNDLE_DIRECTORY = "src/main/resources/resources/languages";
	/** Names of the ui Controller classes, hardcoded for now */
	private static final Class<?>[] UI_JAVA_CONTROLLER_CLASS_NAMES = {
		DateSelecter.class,
		FirstTimeWizard.class,
		FormsThinletTabController.class,
		FrontlineSMSConstants.class,
		FrontlineUI.class,
		HomeTabController.class,
		MessagePanelController.class,
		SmsInternetServiceSettingsHandler.class,
		UiGeneratorController.class,
		UiGeneratorControllerConstants.class,
	};
	/** Directory of the XML files, hardcoded for now */
	private static final String UI_XML_LAYOUT_DIRECTORY = "src/main/resources";
	/** Filter for sorting XML layout files */
	private static final FileFilter LAYOUT_FILE_FILTER = new FileFilter() {
		public boolean accept(File file) {
			return file.isDirectory() || file.getAbsolutePath().endsWith(".xml");
		}
	};
	/** Filter for sorting language files */
	private static final FileFilter LANGUAGE_FILE_FILTER = new FileFilter() {
		public boolean accept(File file) {
			return file.getName().startsWith("frontlineSMS") && file.getName().endsWith(".properties");
		}
	};
	
	/** Things that may appear in code to look like i18n keys, but which are not. */
	private static final String[] IGNORED_KEYS = new String[] {
		".csv",
		"frontlinesupport@kiwanja.net",
		"logs.zip",
		"mail.kiwanja.net",
		"org.smslib.handler.CATHandler",
		"window.height",
		"window.width",
		"window.state",
		"user.home",
		"view.mode",
		
		// Database conf
		"database.config",
		"database.name",
		"database.type",
		"database.username",
		"database.password",

		"server.address",
		"server.password",
		"server.port",
		"server.username",
		
		"hometab.logo.source",
		"hometab.logo.visible",
		"language.filename",
		
		"first.time.wizard",
		"hometab.logo.source",
		"hometab.logo.visible",
		
		"sms.internet.icons",
		
	};

//> INSTANCE PROPERTIES
	/** Map of i18n keys found in code, with reference to their location */
	private final Map<String, Set<Field>> i18nKeysInCode = new HashMap<String, Set<Field>>();
	/** Map of i18n keys found in XML, with reference to their location */
	private final Map<String, Set<File>> i18nKeysInXml = new HashMap<String, Set<File>>();
	/** Map of text found in XML which is not internationalised, with reference to their location */
	private final Map<String, Set<File>> uni18nTextInXml = new HashMap<String, Set<File>>();
	/** Ignored fields.  <fieldName,classInWhichTheFieldIsFound> */
	private final Map<String, Set<Field>> ignoredFields = new HashMap<String, Set<Field>>();

//> CONSTRUCTORS
	/**
	 * @param uiJavaControllerClasses
	 * @param uiXmlLayoutDirectory
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private LanguageChecker(Class<?>[] uiJavaControllerClasses, File uiXmlLayoutDirectory) throws JDOMException, IOException, IllegalArgumentException, IllegalAccessException {
		// 2. parse the controller classes for i18n strings
		for(Class<?> controllerClass : uiJavaControllerClasses) {
			for(Field field : controllerClass.getDeclaredFields()) {
				addFieldReference(field);
			}
		}
		
		// 3. parse the XML layout files for i18n strings, making sure to check for non-i18n strings as well
		extractI18nKeys(uiXmlLayoutDirectory);
	}

//> MAIN METHOD
	/**
	 * Run the checker and produce a report.
	 * @param args 
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		LanguageChecker checker = new LanguageChecker(
				UI_JAVA_CONTROLLER_CLASS_NAMES,
				new File(UI_XML_LAYOUT_DIRECTORY));
		
		checker.output(System.out);
		
		TranslationEmitter emitter = new TranslationEmitter(InternationalisationUtils.getLanguageBundle(new File(LANGUAGEBUNDLE_DIRECTORY, "frontlineSMS.properties")), OUTPUT_DIRECTORY);
		
		for(File languageBundle : new File(LANGUAGEBUNDLE_DIRECTORY).listFiles(LANGUAGE_FILE_FILTER)) {
			I18nReport report = checker.produceReport(languageBundle);
			report.output(System.out, false);
			emitter.processBundle(languageBundle, report);
		}
	}

	/**
	 * 
	 * @param out
	 */
	public void output(PrintStream out) {
		out.println(this.getClass().getName() + " REPORT START ----------");
		out.println("\tin code: " + this.i18nKeysInCode.size());
		out.println("\tin XML : " + this.i18nKeysInXml.size());
		out.println("---------- REPORT START " + this.getClass().getName());
	}


//> ACCESSORS
	/**
	 * Gets all i18nKeys
	 * @return set of all i18n keys that are referenced
	 */
	public Set<String> getAllI18nKeys() {
		TreeSet<String> allKeys = new TreeSet<String>();
		allKeys.addAll(this.i18nKeysInCode.keySet());
		allKeys.addAll(this.i18nKeysInXml.keySet());
		return Collections.unmodifiableSet(allKeys);
	}
	

	/** @return {@link #i18nKeysInCode} */
	public Map<String, Set<Field>> getI18nKeysInCode() {
		return Collections.unmodifiableMap(this.i18nKeysInCode);
	}
	/** @return {@link #i18nKeysInXml} */
	public Map<String, Set<File>> getI18nKeysInXml() {
		return Collections.unmodifiableMap(this.i18nKeysInXml);
	}

//> INSTANCE HELPER METHODS	
	/**
	 * Adds a field reference to this {@link LanguageChecker}.
	 * @param field The field
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private void addFieldReference(Field field) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		if(shouldProcess(field)) {
			trace("Processing field: " + field.getName());
			if(field.getType().equals(String.class)) {
				String fieldValue = field.get(null).toString();
				addFieldValue(field, fieldValue);
			} else if(field.getType().equals(String[].class)) {
				String[] fieldValue = (String[]) field.get(null);
				for(String value : fieldValue) {
					addFieldValue(field, value);
				}
			} else {
				throw new IllegalStateException("Unknown field type: " + field.getType());
			}
		} else trace("Ignoring field: " + field.getName());
	}

	/**
	 * Adds an i18n key gleaned from a {@link Field}.
	 * @param field
	 * @param fieldValue
	 */
	private void addFieldValue(Field field, String fieldValue) {
		if(fieldValue.indexOf('.') != -1 && fieldValue.indexOf('/') == -1) {
			if(!this.i18nKeysInCode.containsKey(fieldValue)) {
				this.i18nKeysInCode.put(fieldValue, new HashSet<Field>());
			}
			this.i18nKeysInCode.get(fieldValue).add(field);
		} else {
			if(!this.ignoredFields.containsKey(fieldValue)) {
				this.ignoredFields.put(fieldValue, new HashSet<Field>());
			}
			this.ignoredFields.get(fieldValue).add(field);
		}
	}
	
	/**
	 * Produces a report about the specified language bundle with respect to this {@link LanguageChecker}.
	 * @param languageBundle the language bundle to compare to this {@link LanguageChecker} 
	 * @return a report
	 * @throws IllegalAccessException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	private I18nReport produceReport(File languageBundle) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		I18nReport report = new I18nReport(this, languageBundle);
		return report;
	}

	/**
	 * Searches for XML layout files, and when they are found they are parsed for i18n keys,
	 * and text that is not internationalised.
	 * @param layoutFile
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	private void extractI18nKeys(File layoutFile) throws JDOMException, IOException {
		if(layoutFile.isDirectory()) {
			// Pass directory contents back into this method
			for(File child : layoutFile.listFiles(LAYOUT_FILE_FILTER)) {
				extractI18nKeys(child);
			}
		} else if(layoutFile.isFile()) {
			// Parse file for text attributes
			Document xmlLayoutDocument = new SAXBuilder().build(layoutFile);
			extractI18nKeys(xmlLayoutDocument.getRootElement(), layoutFile);
		} else throw new IllegalStateException("Cannot understand file: " + layoutFile);
	}
	
	/**
	 * Parses XML elements, and when they are found they are parsed for i18n keys,
	 * and text that is not internationalised.
	 * @param element
	 * @param xmlFile The XML file.  Provided here for reference purposes.
	 */
	private void extractI18nKeys(Element element, File xmlFile) {
		// parse any children this element has
		for(Object kid : element.getChildren()) {
			if(kid instanceof Element) {
				extractI18nKeys((Element) kid, xmlFile);
			}
		}
		
		// Check for text attribute
		String textValue = element.getAttributeValue(Thinlet.TEXT);
		if(textValue != null) {
			if(!textValue.startsWith(Thinlet.TEXT_I18N_PREFIX)) {
				// Found a string that was NOT internationalised
				if(!this.uni18nTextInXml.containsKey(textValue)) {
					this.uni18nTextInXml.put(textValue, new HashSet<File>());
				}
				this.uni18nTextInXml.get(textValue).add(xmlFile);
			} else {
				// Found a string that WAS internationalised
				String i18nKey = textValue.substring(Thinlet.TEXT_I18N_PREFIX.length());
				if(!this.i18nKeysInXml.containsKey(i18nKey)) {
					this.i18nKeysInXml.put(i18nKey, new HashSet<File>());
				}
				this.i18nKeysInXml.get(i18nKey).add(xmlFile);
			}
		}
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * @param s 
	 */
	private void trace(String s) {
		if(false) System.out.println(s);
	}
	
	/**
	 * @param field 
	 * @return <code>true</code> if the field should be processed
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private static final boolean shouldProcess(Field field) throws IllegalArgumentException, IllegalAccessException {
		if(Modifier.isStatic(field.getModifiers())
				&& Modifier.isFinal(field.getModifiers())) {
			if(field.getType().equals(String.class)) {
				String fieldValue = field.get(null).toString();
				
				// Check the key is not in the ignore list
				for(String ignoreValue : IGNORED_KEYS) {
					if(fieldValue.equals(ignoreValue)) {
						return false;
					}
				}
				
				// Not in ignore list, so should be processed
				return true;
			} else if(field.getType().equals(String[].class)) {
				// allow all string arrays
				return true;
			}
		}
		// not static final String, so should be ignored
		return false;
	}
}
