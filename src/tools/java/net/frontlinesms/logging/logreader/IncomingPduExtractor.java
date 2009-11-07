/**
 * 
 */
package net.frontlinesms.logging.logreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;

/**
 * @author Alex
 */
public class IncomingPduExtractor implements LogProcessor {
	
//> STATIC CONSTANTS
	/** set <code>true</code> to generate extra console output */
	private static final boolean TRACE = false;
	/** String to search the logs for */
	private static final String SEARCH_STRING = "Read pdu:";

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/**
	 * Print to {@link System#out} all lines from logs which contain the {@link #SEARCH_STRING}
	 */
	public void processLogLine(String line) {
		if(line.indexOf(SEARCH_STRING) != -1) {
			System.out.println(".processLogLine() : " + line);
		}
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * Run {@link IncomingPduExtractor} over the local temp directory.
	 * @param args these are ignored
	 * @throws Exception when something goes wrong which cannot be handled
	 */
	public static void main(String[] args) throws Exception {
		Collection<File> logFiles = new LocalLogFetcher().getFiles(new File("temp"));
		for(File f : logFiles) {
			processLog(f, new IncomingPduExtractor());
		}
	}

	/**
	 * Reads a text file into separate lines and passes them to the {@link LogProcessor}.
	 * @param f a log file
	 * @param proc a {@link LogProcessor}
	 * @throws Exception
	 */
	private static void processLog(File f, LogProcessor proc) throws Exception {
		if(TRACE) System.out.println("LocalLogFetcher.processLog() : " + f.getName());
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String line;
		while((line = reader.readLine()) != null) {
			proc.processLogLine(line);
		}
	}
}
