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

import net.frontlinesms.arcane.data.AutoReplyTrigger;
 
public class AutoReplyTriggerPattern extends SplitPattern {
	private static AutoReplyTriggerPattern instance;
	
	public static synchronized AutoReplyTriggerPattern getInstance() {
		if(instance == null) instance = new AutoReplyTriggerPattern();
		return instance;
	}
	
	private AutoReplyTriggerPattern() {
		addField(FieldType.STRING, "msisdn", 15);
		addField(FieldType.STRING, "keyword", 20);
		addField(FieldType.STRING, "date", 8);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public AutoReplyTrigger parse(String dataString) {
		AutoReplyTrigger reply = new AutoReplyTrigger();
		
		String[] columns = getColumns(dataString);
		
		int index = -1;
		
		reply.setMsisdn(getString(columns[++index]));
		reply.setKeyword(getString(columns[++index]));
		reply.setDate(getDate(columns[++index]));
		
		return reply;
	}
}
