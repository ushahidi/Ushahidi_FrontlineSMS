package net.frontlinesms.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import thinlet.Thinlet;

import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.junit.BaseTestCase;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.i18n.LanguageBundle;

/**
 * Test class for {@link CsvExporter}
 * @author Alex
 */
public class CsvExportTests extends BaseTestCase {
	
//> STATIC CONSTANTS
	/** Default row format used in {@link #testContactExport()}.  N.B. This will have to be kept up-to-date if there
	 * is a change to how the export works in the UI, as this may change the export order. 
	 * @return default row format for exporting contacts */
	private static final CsvRowFormat getContactExportRowFormat() {
		CsvRowFormat rowFormat = new CsvRowFormat();
		rowFormat.addMarker(CsvUtils.MARKER_CONTACT_NAME);
		rowFormat.addMarker(CsvUtils.MARKER_CONTACT_PHONE);
		rowFormat.addMarker(CsvUtils.MARKER_CONTACT_OTHER_PHONE);
		rowFormat.addMarker(CsvUtils.MARKER_CONTACT_EMAIL);
		rowFormat.addMarker(CsvUtils.MARKER_CONTACT_STATUS);
		rowFormat.addMarker(CsvUtils.MARKER_CONTACT_NOTES);
		rowFormat.addMarker(CsvUtils.MARKER_CONTACT_GROUPS);
		return rowFormat;
	}

//> TEST METHODS
	/**
	 * Test {@link CsvExporter#exportContacts(File, List, CsvRowFormat)}.
	 * @throws IOException If there was an unexpected error writing or reading to a file
	 */
	public void testContactExport() throws IOException {
		// Export a number of contacts to a file, and check that the generated file is as expected.
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		contacts.add(new Contact("Test Number", "000", "", "", "", true));
		contacts.add(new Contact("شئهة", "07890123456", "0987654321", "azim@mo.jo", "", false));
		contacts.add(new Contact("Sly Eddie", "01234567890", "554466221133", "sly.eddie@ramprakash.co.uk", "Sly Eddie is a sneaky chap.", true));
		contacts.add(new Contact("Richard E. Grant", "+44852774", "+1800-RICH-ARDE", "", "\"What a piece of work is a man!\"", true));

		// Make sure the English i18n bundle is available to provision the export column names
		LanguageBundle englishBundle = InternationalisationUtils.getLanguageBundleFromClasspath("/resources/languages/frontlineSMS.properties");
		Thinlet.DEFAULT_ENGLISH_BUNDLE = englishBundle.getProperties();
		
		File generatedFile = super.getOutputFile(this.getClass().getSimpleName() + ".contacts.csv");
		CsvExporter.exportContacts(generatedFile, contacts, getContactExportRowFormat());
		//assertEquals("Generated CSV file did not contain the expected values.", this.getClass().getResourceAsStream(this.getClass().getSimpleName() + ".contacts.csv"), new FileInputStream(generatedFile));
	}
	
	/**
	 * Test {@link CsvUtils#escapeValue(String)}
	 * TODO move this to CsvUtilsTest class
	 */
	public void testCsvEscape() {
		testCsvEscaped("one", "one", "\"one\"");
		testCsvEscaped("on\"e", "\"on\"\"e\"");
		testCsvEscaped("column0\tcolumn1", "column0\tcolumn1", "\"column0\tcolumn1\"");
		testCsvEscaped("line0\rline1", "\"line0\rline1\"");
	}
	
//> STATIC HELPER METHODS
	/**
	 * Internal test method for {@link #testCsvEscape()}
	 * @param unescaped
	 * @param acceptedEscaped
	 */
	private static void testCsvEscaped(String unescaped, String... acceptedEscaped) {
		String escaped = CsvUtils.escapeValue(unescaped);
		for(String expectedEscaped : acceptedEscaped) {
			if(escaped.equals(expectedEscaped)) return;
		}
		
		throw new IllegalArgumentException("Expected result not achieved:" +
				"\nunescaped: " + unescaped +
				"\nescaped  : " + escaped + 
				"\naccepted : " + toString(acceptedEscaped));
	}
	
	/**
	 * Convert an array of {@link String}s into a comma-separated list of {@link String}s.
	 * @param strings
	 * @return A list of the strings provided in the array separated by commas and surrounded by curly braces
	 */
	private static final String toString(String[] strings) {
		String ret = "{";
		for(String s : strings) {
			ret += s + ", ";
		}
		return ret.substring(0, ret.length()-2) + "}";
	}
}
