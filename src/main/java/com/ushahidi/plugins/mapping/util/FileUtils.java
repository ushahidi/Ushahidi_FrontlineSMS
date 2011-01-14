package com.ushahidi.plugins.mapping.util;

import java.io.File;

/**
 * FileUtils
 * @author dalezak
 *
 */
public class FileUtils {
	
	private static MappingLogger LOG = new MappingLogger(FileUtils.class);
	
	/**
	 * Get directory size
	 * @param directory directory
	 * @return file size
	 */
	public static float getDirectorySize(File directory) {
		float size = 0;
		if (directory.isFile()) {
			size = (float)directory.length();
		} 
		else {
			for (File file : directory.listFiles()) {
				if (file.isFile()) {
					size += (float)file.length();
				} 
				else {
					size += getDirectorySize(file);
				}
			}
		}
		return size / 1024 / 1024;
	}
	
	/**
	 * Delete files in directory
	 * @param directory directory
	 * @return number of files deleted
	 */
	public static int deleteFiles(String directory) {
		return deleteFiles(new File(directory));
	}
	
	/**
	 * Delete files in directory
	 * @param directory directory
	 * @return number of files deleted
	 */
	public static int deleteFiles(File directory) {
		int count = 0;
		if (directory.exists()) {
			for(String fileName : directory.list()) {
				try {
					File file = new File(directory, fileName);
					LOG.debug("File %s deleted %s ", file, file.delete());
					count++;	
				}
				catch(Exception ex) {
					LOG.error("Error deleting cache file: %s", ex);
				}
			}
		}
		return count;
	}

}