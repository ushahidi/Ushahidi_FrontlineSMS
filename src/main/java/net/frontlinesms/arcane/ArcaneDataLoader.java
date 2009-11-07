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
package net.frontlinesms.arcane;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import net.frontlinesms.Utils;
import net.frontlinesms.arcane.data.*;
import net.frontlinesms.arcane.split.*;

class ArcaneDataLoader {
	private static Logger LOG = Utils.getLogger(ArcaneDataLoader.class);
	private static final String FILENAME_AUTOREPLY_TRIGGERS = "autoreplies.fld";
	private static final String FILENAME_CONTACTS = "contacts.fld";
	private static final String FILENAME_SURVEYS = "surveys.fld";
	private static final String FILENAME_RECEIVED_MESSAGES__UNKNOWN_SENDER = "unknownrecd.fld";
	private static final String FILENAME_SENT_MESSAGES = "sentmessages.fld";
	private static final String FILENAME_AUTO_REPLIES = "replies.fld";
	private static final String FILENAME_RECEIVED_MESSAGES = "recdmessages.fld";
	
	static final HashMap<String, Integer> lineLengths = new HashMap<String, Integer>();
	static {
		lineLengths.put(FILENAME_AUTOREPLY_TRIGGERS, 43);
		lineLengths.put(FILENAME_CONTACTS, 468);
		lineLengths.put("contactsrecd.fld", 117);
		lineLengths.put("contactssent.fld", 156);
		lineLengths.put(FILENAME_RECEIVED_MESSAGES, 366);
		lineLengths.put(FILENAME_AUTO_REPLIES, 209);
		lineLengths.put(FILENAME_SENT_MESSAGES, 403);
		lineLengths.put(FILENAME_SURVEYS, 126);
		lineLengths.put(FILENAME_RECEIVED_MESSAGES__UNKNOWN_SENDER, 366);
	}

	/** NB. NOT thread-safe!!! */
	static String directory;
	
	public static void main(String[] args) throws Throwable {
		String directory;
		if(args.length > 0) directory = args[0];
		else directory = "data/kadu/";
		ArcaneDataLoader.directory = directory;
		tossSalad();
		loadBundleFromExternalDirectory(directory);
	}
	
	public static ArcaneDataBundle loadBundleFromExternalDirectory(String directoryName) throws IOException {
		if(directoryName.endsWith("/")) directory = directoryName;
		else directory = directoryName + '/';
		
		ArcaneDataBundle bundle = new ArcaneDataBundle();
		populateDataListFromExternalFile(ArcaneDataLoader.FILENAME_AUTOREPLY_TRIGGERS, AutoReplyTriggerPattern.getInstance(), bundle.getAutoReplyTriggers());
		populateDataListFromExternalFile(ArcaneDataLoader.FILENAME_CONTACTS, ContactPattern.getInstance(), bundle.getContacts());
		populateDataListFromExternalFile(ArcaneDataLoader.FILENAME_SURVEYS, SurveyPattern.getInstance(), bundle.getSurveys());
		populateDataListFromExternalFile(ArcaneDataLoader.FILENAME_RECEIVED_MESSAGES, ReceivedMessagePattern.getInstance(), bundle.getReceivedMessages());
		populateDataListFromExternalFile(ArcaneDataLoader.FILENAME_RECEIVED_MESSAGES__UNKNOWN_SENDER, ReceivedMessage_UnknownSenderPattern.getInstance(), bundle.getReceivedMessages_unknownSender());
		populateDataListFromExternalFile(ArcaneDataLoader.FILENAME_SENT_MESSAGES, SentMessagePattern.getInstance(), bundle.getSentMessages());
		populateDataListFromExternalFile(ArcaneDataLoader.FILENAME_AUTO_REPLIES, AutoReplyPattern.getInstance(), bundle.getAutoReplies());	
		
		return bundle;
	}
	
	/**
	 * Loads data from FrontlineSMS 1.0 data files.  These data are loaded into objects
	 * of a suitable type and placed in the supplied list.
	 * @param <T> The class to load the arcane data into.
	 * @param filename The name of the file containing the arcane object data. 
	 * @param pattern The pattern describing the data format of the arcane data.
	 * @param dataList The list in which the new objects should be loaded. 
	 * @throws IOException Thrown when there is an error accessing the saved file.
	 */
	private static <T extends BaseDataObject> void populateDataListFromExternalFile(String filename, SplitPattern pattern, List<T> dataList) throws IOException {
		for(String objectDataAsString : getDataStrings(filename)) {
			T newDataObject = pattern.<T>parse(objectDataAsString);
			dataList.add(newDataObject);
			LOG.debug(newDataObject.toString());
		}
	}
	/**
	 * Gets the length of one chunk of data as stored in a particular file.
	 * @param filename The name of the data file.
	 * @return The length of one chunk of data in the apecified data file.
	 */
	private static int getDataChunkLength(String filename) {
		filename = filename.toLowerCase();
		if(!lineLengths.containsKey(filename)) throw new IllegalArgumentException();
		return lineLengths.get(filename);
	}
	private static List<String> getDataStrings(String filename) throws IOException {
		int chunkLength = getDataChunkLength(filename);
		ArrayList<String> lines = new ArrayList<String>();

		String fileContents = getFileContentsAsString(directory + filename);
		
		int b = 0;
		int target = fileContents.length() - chunkLength;
		for(; b < target; b += chunkLength) {
			String objectDataAsString = fileContents.substring(b, b+chunkLength);
			if(objectDataAsString.trim().length() > 0) lines.add(objectDataAsString);
		}
		fileContents = fileContents.substring(b);
		if(fileContents.trim().length() > 0) lines.add(fileContents);
		
		return lines;
	}
	
	/**
	 * DEBUG METHOD
	 * Iterate over the files in a directory, and print out the contents, with a line break after each chunk.
	 * @param directory
	 * @throws IOException
	 */
	private static void tossSalad() throws IOException {
		LOG.debug("::  " + directory);
		for(File file : new File(directory).listFiles()) {
			try {
				String filename = file.getName();
				int chunkLength = getDataChunkLength(filename);
				String data = getFileContentsAsString(directory + filename);
				LOG.debug(filename + " :: " + chunkLength);
				LOG.debug("\t0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
				int b = 0;
				int target = data.length() - chunkLength;
				for(; b < target; b += chunkLength) {
					LOG.debug(chunkLength + "::\t" + data.substring(b, b+chunkLength));
				}
				LOG.debug(chunkLength + "::\t" + data.substring(b));
			} catch(IllegalArgumentException ex) {
				LOG.debug(file.getName() + " :: REJECTED\r\n");
			}
		}
	}
	
	/**
	 * Gets a file as a String.
	 * @return
	 */
	private static String getFileContentsAsString(String filename) throws IOException {
		StringBuilder bob = new StringBuilder();
		InputStream in = new FileInputStream(filename);
		int c;
		while((c=in.read())!=-1) bob.append((char)c);
		return bob.toString();
	}
	
//	private static void doKnife(int a, int b) throws IOException {
//		if(a <= 0) a = 1;
//		String vinegar = getFileContentsAsString(new File(contactsFile));
//		for(int i=a; i<=b; ++i) {
//			doFork(vinegar, i);
//			LOG.debug();
//			LOG.debug();
//			LOG.debug();
//		}
//	}
	
//	static void doSpoon() throws Throwable {
//		InputStream in = new FileInputStream(contactsFile);
//		int c;
//		while((c=in.read())!=-1) LOG.debug(c + "\t" + (char)c);
//	}
	
//	private static void doRead() throws Throwable {
//		BufferedReader reader = new BufferedReader(new FileReader(contactsFile));
//		String line;
//		while((line=reader.readLine())!=null) {
//			LOG.debug("[doRead] " + line);
//		}
//	}
//	
//	private static void doStringify() throws Throwable {
//		InputStream in = new FileInputStream(contactsFile);
//		List<String> spag = stringify(in);
//		LOG.debug();
//		LOG.debug();
//		LOG.debug("READ " + spag.size() + " STRINGS!");
//		LOG.debug("-------------");
//		for(String s : spag) {
//			LOG.debug("\t" + s);
//		}
//	}
	
//	private static List<String> stringify(InputStream in) throws IOException {
//		// c is the last byte read from the stream
//		int c;
//		// b is the byte before c :D
//		int b = -1;
//		// a is the byte before b :D
//		int a = -1;
//		int i = -1;
//		ArrayList<String> stringz = new ArrayList<String>();
//		StringBuilder bob = new StringBuilder();
//		StringBuilder wholeStream = new StringBuilder();
//		int consecutiveSpaces = 0;
//		while((c=in.read())!=-1) {
//			++i;
//			wholeStream.append((char)c);
//			if(c!=32) {
//				if(b==32) {
//					LOG.debug("CONSECUTIVE SPACES: " + consecutiveSpaces);
//				}
//			} else {
//				if(b==32) {
//					++consecutiveSpaces;
//				} else {
//					consecutiveSpaces = 1;
//				}
//			}
//			if(c == 0) {
//				// Zero: add the created string to the list,
//				// and reset the buff!
//				if(bob.length() > 0) stringz.add(bob.toString());
//				bob.delete(0, Integer.MAX_VALUE);
////			} else if(a=='9' && b=='/' && c=='0') {
////				LOG.debug("NULL!!! i%2 = " + (i%2));
////
////				if(bob.length() > 0) stringz.add(bob.toString());
////				bob.delete(0, Integer.MAX_VALUE);
////				
////				stringz.add("!!!BREAK!!!");
//			} else bob.append((char)c);
//			
//			a = b;
//			b = c;
//		}
//		LOG.debug("Main.stringify() : Read: " + wholeStream.toString());
//		stringz.add(bob.toString());
//		return stringz;
//	}
}
