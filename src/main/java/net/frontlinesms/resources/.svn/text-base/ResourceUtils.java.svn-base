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
package net.frontlinesms.resources;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.*;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;

import org.apache.log4j.Logger;

/**
 * Utility methods used for loading resources from the file system. 
 * @author Alex
 */
public class ResourceUtils {
	
//> CONSTANTS
	/** Logging object for this class */
	private static Logger LOG = Utils.getLogger(ResourceUtils.class);
	/** The size of byte buffers used in this class. */
	private static final int BUFFER_SIZE = 2048;
	/** Name of directory that discarded resources are put in after an upgrade of FrontlineSMS. */
	private static final String GRAVEYARD = "old";

	/** Name of the resource directory containing properties. */
	public static final String DIRECTORY_PROPERTIES = "properties";
	
//> STATIC UTILITY METHODS
	/**
	 * Unzips a compressed archive to the specified output directory.  The archive's directory
	 * structure is rebuilt in the output directory if it does not already exist.  Optionally,
	 * old versions of files can be kept if they are present.
	 * @param inputArchive
	 * @param outputDirectory
	 * @param overwrite
	 * @throws IOException
	 */
	public static final void unzip(File inputArchive, File outputDirectory, boolean overwrite) throws IOException {
		if(!inputArchive.exists() || !inputArchive.isFile()) throw new IllegalArgumentException("Input archive not found: " + inputArchive.getPath());
		if(!outputDirectory.exists() || !outputDirectory.isDirectory()) throw new IllegalArgumentException("Output directory does not exist: " + outputDirectory.getPath());
		
		unzip(new FileInputStream(inputArchive), outputDirectory, overwrite);
	}

	/**
	 * Unzips a {@link ZipInputStream} to a directory.  If unzipped files already exist in the destination
	 * directory, they can be optionally overridden.
	 * @param inputArchiveAsStream
	 * @param outputDirectory
	 * @param overwriteOverwriteables
	 * @throws IOException
	 */
	public static final void unzip(InputStream inputArchiveAsStream, File outputDirectory, boolean overwriteOverwriteables) throws IOException {
		ZipInputStream in = new ZipInputStream(new BufferedInputStream(inputArchiveAsStream));
		byte[] buffer = new byte[BUFFER_SIZE];
		ZipEntry entry;
		String graveyardName = GRAVEYARD + "_" + generateGraveyardTimestamp() + File.separator;
		while((entry=in.getNextEntry()) != null) {
			if(!entry.isDirectory()) {
				boolean remove = false;
				File outputFile = new File(outputDirectory, entry.getName());
				createDirectoryTree(outputFile);
				if (outputFile.exists() && overwriteOverwriteables && isOverwriteable(outputFile)) {
					File graveyard = new File(outputFile.getParentFile(), graveyardName);
					graveyard.mkdir();
					File destination = new File(graveyard, outputFile.getName());
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destination), BUFFER_SIZE);
					ResourceUtils.stream2stream(new FileInputStream(outputFile), out, buffer);
					out.close();
					remove = true;
				}
				if (!outputFile.exists() || remove) {
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile), BUFFER_SIZE);
					ResourceUtils.stream2stream(in, out, buffer);
					out.close();
				}
			}
		}
		in.close();
	}
	
	/**
	 * Generate a timestamp to be appended to the graveyard directories' names.
	 * @return
	 */
	private static String generateGraveyardTimestamp() {
		Calendar cal = Calendar.getInstance();
		return "" + cal.get(Calendar.YEAR)
			+ (cal.get(Calendar.MONTH) + 1)
			+ cal.get(Calendar.DAY_OF_MONTH)
			+ cal.get(Calendar.HOUR_OF_DAY)
			+ Integer.toString(100 + cal.get(Calendar.MINUTE)).substring(1)
			+ Integer.toString(1000 + cal.get(Calendar.MILLISECOND)).substring(1);
	}

	/**
	 * Checks if a configuration file should be over-ridden by a new version when FrontlineSMS is upgraded. 
	 * @param outputFile
	 * @return
	 */
	private static boolean isOverwriteable(File outputFile) {
		// Overwrite all files, as we dump old files in the graveyard.  This should remove any painful
		// upgrade procedures we might have to go through
		return true;
	}

	/**
	 * Creates a directory and all directories above it.  This method calls itself recursively
	 * in order to create the directory at the top of a tree first.
	 * @param file
	 */
	public static void createDirectoryTree(File file) {
		file = file.getParentFile();
		if((!file.exists() || !file.isDirectory())) {
			createDirectoryTree(file);
			boolean success = file.mkdir();
			if(!success) LOG.warn("Failed to create directory: " + file.getAbsolutePath());
		}
	}
	
	/**
	 * Zips the contents of a directory into a new archive.
	 * @param dataDirectoryPath
	 * @param outputArchive
	 * @throws IOException
	 */
	public static void zip(String dataDirectoryPath, File outputArchive) throws IOException {
		File dataDirectory = new File(dataDirectoryPath);
		LOG.debug("Bundling: " + dataDirectory.getPath());
		LOG.debug("      to: " + outputArchive.getPath());
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputArchive)));
		if(!dataDirectory.exists() || !dataDirectory.isDirectory()) throw new IllegalArgumentException("Not a directory: " + dataDirectory.getPath());
		
		byte[] buffer = new byte[ResourceUtils.BUFFER_SIZE];
		addDirectoryToZip(out, dataDirectory, dataDirectoryPath, buffer);
		out.close();
	}
	
	/**
	 * Recursively adds a directory and its contents to a {@link ZipOutputStream}.
	 * @param out The {@link ZipOutputStream} to zip the directory to
	 * @param directory directory to zip
	 * @param rootDirectory 
	 * @param buffer
	 * @throws IOException
	 */
	private static final void addDirectoryToZip(ZipOutputStream out, File directory, String rootDirectory, byte[] buffer) throws IOException {
		LOG.debug("Adding dir to zip: " + directory.getPath());
		for(File file : directory.listFiles()) {
			if(file.isDirectory()) addDirectoryToZip(out, file, rootDirectory, buffer);
			else addFileToZip(out, file, rootDirectory, buffer);
		}
	}
	
	/**
	 * Adds a file to a ZipOutputStream.
	 * @param out
	 * @param file
	 * @param rootDirectory base directory being zipped. Necessary here so that the zipped file can be given a relative path
	 * @param buffer
	 * @throws IOException
	 */
	private static final void addFileToZip(ZipOutputStream out, File file, String rootDirectory, byte[] buffer) throws IOException {
		LOG.debug("Adding file to zip: " + file.getPath());
		ZipEntry entry = new ZipEntry(file.getPath().substring(rootDirectory.length()));
		out.putNextEntry(entry);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
		ResourceUtils.stream2stream(in, out, buffer);
		in.close();
	}
	
	/**
	 * Writes the entire contents of an InputStream to an OutputStream.
	 * @param in
	 * @param out
	 * @param buffer
	 * @throws IOException
	 */
	public static final void stream2stream(InputStream in, OutputStream out, byte[] buffer) throws IOException {
		int bytesRead;
		while ((bytesRead=in.read(buffer, 0, BUFFER_SIZE)) != -1) {
		   out.write(buffer, 0, bytesRead);
		}
	}
	

	/**
	 * Loads a list from a textfile, ignoring any blank lines, or lines that
	 * start with a # character.
	 * @param filename
	 * @return
	 */
	public static final String[] getUsefulLines(String filename) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		ArrayList<String> lines = new ArrayList<String>();
		try {
			fis = new FileInputStream(filename);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);

			String line;
			while((line = br.readLine()) != null) {
				line = line.trim();
				// Don't forget to ignore empty lines and comments
				if (line.length() > 0 && line.charAt(0) != '#') {
					lines.add(line);
				}
			}
		} catch (IOException ex) {
			LOG.debug("Error reading file '" + filename + "'", ex);
		} finally {
			// close any streans, readers etc.
			if (br != null) try { br.close(); } catch(IOException ex) {}
			if (isr != null) try { isr.close(); } catch(IOException ex) {}
			if (fis != null) try { fis.close(); } catch(IOException ex) {}
		}
		return lines.toArray(new String[lines.size()]);
	}

	/**
	 * Gets the path to the configuration directory in which languages, conf, and properties directories all lie.
	 * @return
	 */
	public static String getConfigDirectoryPath() {
		return System.getProperty("user.home") + File.separatorChar + "FrontlineSMS" + File.separatorChar;
	}
}
