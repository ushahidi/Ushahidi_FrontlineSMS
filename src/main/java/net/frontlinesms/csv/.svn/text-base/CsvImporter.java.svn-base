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
package net.frontlinesms.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.*;
import net.frontlinesms.data.repository.*;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.apache.log4j.Logger;

/**
 * This file contains methods for importing data to the FrontlineSMS service
 * from CSV files.
 * 
 * FIXME display a meaningful message if this fails!
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 * @author Alex Anderson 
 * <li> alex(at)masabi(dot)com
 */
public class CsvImporter {
	private static Logger LOG = Utils.getLogger(CsvImporter.class); 
	
	public static void importContacts(String filename, ContactDao contactFactory) {
		LOG.trace("ENTER");
		LOG.debug("File [" + filename + "]");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			
			String[] lineValues;
			while((lineValues = CsvUtils.readLine(reader)) != null) {
				String name = getString(lineValues, 0);
				if (name.equalsIgnoreCase("")) name = InternationalisationUtils.getI18NString(FrontlineSMSConstants.UNKNOWN_NAME);
				String number = getString(lineValues, 1);
				String email = getString(lineValues, 2);
				String notes = getString(lineValues, 3);
				try {
					Contact c = new Contact(name, number, "", email, notes, true);
					contactFactory.saveContact(c);
				} catch (DuplicateKeyException e) {
					// FIXME should actually pass details of this back to the user.
					LOG.debug("Contact already exist with this number [" + number + "]", e);
				}		
			}
		} catch (FileNotFoundException e) {
			LOG.debug("File not found [" + filename + "]", e);
		} catch (IOException e) {
			LOG.debug("Problem reading file [" + filename + "]", e);
		} catch(CsvParseException ex) {
			LOG.warn("There was an error reading the CSV file: " + filename, ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOG.debug("Closing file [" + filename + "]", e);
				}
			}
		}
		LOG.trace("EXIT");
	}

	/**
	 * Gets the string from a particular index of an array.  If the array is not long
	 * enough to contain that index, returns an empty string.
	 * @param values
	 * @param index
	 * @return
	 */
	private static String getString(String[] values, int index) {
		if(values.length > index) {
			return values[index];
		} else return "";
	}
}
