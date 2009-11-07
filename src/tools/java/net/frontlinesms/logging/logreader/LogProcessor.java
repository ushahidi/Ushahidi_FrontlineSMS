/**
 * 
 */
package net.frontlinesms.logging.logreader;

/**
 * @author Alex
 *
 */
public interface LogProcessor {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> INSTANCE METHODS
	/**
	 * Process a line of logs.
	 * @param line processes the line of the logs
	 */
	public void processLogLine(String line);
	
//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
