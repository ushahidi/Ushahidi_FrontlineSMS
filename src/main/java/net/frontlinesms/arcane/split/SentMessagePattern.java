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

import net.frontlinesms.arcane.data.ArcaneSentMessage;

public class SentMessagePattern extends SplitPattern {
	private static SentMessagePattern instance;
	
	public static synchronized SentMessagePattern getInstance() {
		if(instance == null) instance = new SentMessagePattern();
		return instance;
	}
	
	private SentMessagePattern() {
		addField(FieldType.INT, "id", 10);
		addField(FieldType.DATE, "date", 10);
		addField(FieldType.TIME, "time", 8);
		addField(FieldType.STRING, "content", 320);
		addField(FieldType.STRING, "group", 48);
		addField(FieldType.INT, "recipients", 4);
	}
	
	@SuppressWarnings("unchecked")
	public ArcaneSentMessage parse(String dataString) {
		ArcaneSentMessage message = new ArcaneSentMessage();
		
		String[] columns = getColumns(dataString);
		
		int index = -1;
		
		message.setId(getInt(columns[++index]));
		message.setTimestamp(getTimestamp(columns[++index], columns[++index]));
		message.setContent(getString(columns[++index]));
		message.setGroup(getString(columns[++index]));
		message.setRecipients(getInt(columns[++index]));
		
		return message;
	}
}
