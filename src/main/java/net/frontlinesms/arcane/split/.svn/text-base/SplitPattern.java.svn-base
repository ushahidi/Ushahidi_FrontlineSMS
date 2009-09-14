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
package net.frontlinesms.arcane.split;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import net.frontlinesms.Utils;
import net.frontlinesms.arcane.data.BaseDataObject;

public abstract class SplitPattern {
	/** A String containing a single char, the NULL character ((char)0) */
	private final String NULL = new String(new char[]{0});
	
	private final List<SplitField> fields = new ArrayList<SplitField>();
	
	public abstract <T extends BaseDataObject> T parse(String dataString);
	
	private static Logger LOG;
	
	protected String[] getColumns(String dataString) {
		String[] columns = new String[fields.size()];
		
		int stringIndex = 0;
		int columnIndex = -1;
		
		for(SplitField field : fields) {
			columns[++columnIndex] = dataString.substring(stringIndex, stringIndex += field.getFieldSize());
			LOG.debug("COLUMN " + columnIndex + " :: " + columns[columnIndex]);
		}
		
		return columns; 
	}
	
	protected void addField(FieldType type, String name, int fieldSize) {
		fields.add(new SplitField(type, name, fieldSize));
	}
	
	protected boolean getBoolean(BoolString bool, String data) {
		return bool.isTrue(data);
	}
	
	protected String getString(String data) {
		if(data.contains(NULL)) return "";
		else return data.trim();
	}
	
	protected int getInt(String data) {
		if(data.contains(NULL)) return -1;
		else return Integer.parseInt(data.trim());
	}

	static final Calendar CALENDAR = Calendar.getInstance();
	static { 
		LOG = Utils.getLogger(SplitPattern.class);
		CALENDAR.setTimeInMillis(0);
	}
	
	protected long getDate(String data) {
		if(data.contains(NULL)) return 0;
		else {
			String[] date = data.trim().split("/");
			CALENDAR.set(Calendar.HOUR, 0);
			CALENDAR.set(Calendar.MINUTE, 0);
			CALENDAR.set(Calendar.SECOND, 0);
			CALENDAR.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
			CALENDAR.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
			CALENDAR.set(Calendar.YEAR, 2000 + Integer.parseInt(date[2]));
			return CALENDAR.getTimeInMillis();
		}
	}
	
	protected long getTimestamp(String dateData, String timeData) {
		if(timeData.contains(NULL)) {
			CALENDAR.set(Calendar.HOUR, 0);
			CALENDAR.set(Calendar.MINUTE, 0);
			CALENDAR.set(Calendar.SECOND, 0);
		} else {
			String[] time = timeData.trim().split(":");
			CALENDAR.set(Calendar.HOUR, Integer.parseInt(time[0]));
			CALENDAR.set(Calendar.MINUTE, Integer.parseInt(time[1]));
			CALENDAR.set(Calendar.SECOND, Integer.parseInt(time[2]));
		}
		if(dateData.contains(NULL)) {
			CALENDAR.set(Calendar.DAY_OF_MONTH, 0);
			CALENDAR.set(Calendar.MONTH, 0);
			CALENDAR.set(Calendar.YEAR, 0);
		} else {
			String[] date = dateData.trim().split("/");
			CALENDAR.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
			CALENDAR.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
			CALENDAR.set(Calendar.YEAR, 2000 + Integer.parseInt(date[2]));
		}
		return CALENDAR.getTimeInMillis();
	}
	
	protected static class BoolString {
		private final String trueString;
		private final String falseString;
		
		public BoolString(String trueString, String falseString) {
			this.trueString = trueString;
			this.falseString = falseString;
		}
		
		boolean isTrue(String test) {
			test = test.trim();
			if(trueString.equals(test)) return true;
			else if(falseString.equals(test)) return false;
			else throw new IllegalArgumentException(test);
		}
	}
}
