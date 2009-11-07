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

import net.frontlinesms.arcane.data.*;

public class AutoReplyPattern extends SplitPattern {
	private static final BoolString IS_LIVE = new BoolString("Live", "Dormant");

	private static AutoReplyPattern instance;
	
	public static synchronized AutoReplyPattern getInstance() {
		if(instance == null) instance = new AutoReplyPattern();
		return instance;
	}
	
	private AutoReplyPattern() {
		addField(FieldType.BOOLEAN, "live", 7);
		addField(FieldType.STRING, "keyword", 20);
		addField(FieldType.STRING, "replyText", 160);
		addField(FieldType.STRING, "startDate", 8);
		addField(FieldType.STRING, "endDate", 8);
		addField(FieldType.STRING, "hits", 2);
	}
	
	@SuppressWarnings("unchecked")
	public AutoReply parse(String dataString) {
		AutoReply reply = new AutoReply();
		
		String[] columns = getColumns(dataString);
		
		int index = -1;
		
		reply.setLive(getBoolean(IS_LIVE, columns[++index]));
		reply.setKeyword(getString(columns[++index]));
		reply.setReplyText(getString(columns[++index]));
		reply.setStartDate(getDate(columns[++index]));
		reply.setEndDate(getDate(columns[++index]));
		reply.setHits(getInt(columns[++index]));
		
		return reply;
	}
}
