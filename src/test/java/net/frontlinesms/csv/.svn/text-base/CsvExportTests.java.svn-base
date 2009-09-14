package net.frontlinesms.csv;


import junit.framework.TestCase;

public class CsvExportTests extends TestCase {
	public void testCsvEscape() {
		test("one", "one", "\"one\"");
		test("on\"e", "\"on\"\"e\"");
		test("column0\tcolumn1", "column0\tcolumn1", "\"column0\tcolumn1\"");
		test("line0\rline1", "\"line0\rline1\"");
	}
	
	private static void test(String unescaped, String... acceptedEscaped) {
		String escaped = CsvUtils.escapeValue(unescaped);
		for(String expectedEscaped : acceptedEscaped) {
			if(escaped.equals(expectedEscaped)) return;
		}
		
			throw new IllegalArgumentException("Expected result not achieved:" +
					"\nunescaped: " + unescaped +
					"\nescaped  : " + escaped + 
					"\naccepted : " + toString(acceptedEscaped));
	}
	
	private static final String toString(String[] strings) {
		String ret = "{";
		for(String s : strings) {
			ret += s + ", ";
		}
		return ret.substring(0, ret.length()-2) + "}";
	}
}
