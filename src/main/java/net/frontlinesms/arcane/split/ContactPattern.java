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

import net.frontlinesms.arcane.data.ArcaneContact;

public class ContactPattern extends SplitPattern {
	private static final BoolString IS_ACTIVE = new BoolString("Active", "Dormant");

	private static ContactPattern instance;
	
	public static synchronized ContactPattern getInstance() {
		if(instance == null) instance = new ContactPattern();
		return instance;
	}
	
	private ContactPattern() {
		addField(FieldType.INT, "id", 5);
		addField(FieldType.STRING, "name", 36);
		addField(FieldType.STRING, "group", 50);
		addField(FieldType.STRING, "location", 30);
		addField(FieldType.STRING, "msisdn", 15);
		addField(FieldType.STRING, "notes", 315);
		addField(FieldType.STRING, "date", 10);
		addField(FieldType.BOOLEAN, "isActive", 7);
	}
	
	@SuppressWarnings("unchecked")
	public ArcaneContact parse(String dataString) {
		ArcaneContact contact = new ArcaneContact();
		
		String[] columns = getColumns(dataString);
		
		int index = -1;
		
		contact.setId(getInt(columns[++index]));
		contact.setName(getString(columns[++index]));
		contact.setGroup(getString(columns[++index]));
		contact.setLocation(getString(columns[++index]));
		contact.setMsisdn(getString(columns[++index]));
		contact.setNotes(getString(columns[++index]));
		contact.setDate(getDate(columns[++index]));
		contact.setActive(getBoolean(IS_ACTIVE, columns[++index]));
		
		return contact;
	}
}
