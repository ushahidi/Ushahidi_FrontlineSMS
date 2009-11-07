/**
 * 
 */
package net.frontlinesms.logging.logreader;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Alex
 *
 */
public class LocalLogFetcher {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> INSTANCE METHODS
	/**
	 * Gets all files from the supplied directory and its subdirectories.
	 * @param directory 
	 * @return  all files in the supplied directory and its subdirectories
	 */
	public Collection<File> getFiles(File directory) {
		HashSet<File> files = new HashSet<File>();
		
		for(File f : directory.listFiles()) {
			if(f.isDirectory()) {
				files.addAll(getFiles(f));
			} else {
				files.add(f);
			}
		}
		
		return files;
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
