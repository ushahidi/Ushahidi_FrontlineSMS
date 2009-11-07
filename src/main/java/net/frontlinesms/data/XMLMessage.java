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
package net.frontlinesms.data;

import java.util.LinkedList;

/**
 * Data object representing an SMS message, which is part of the command list described in
 * XML files.
 * @author Carlos Eduardo Genz
 *
 */
public class XMLMessage {
	public static final int TYPE_BINARY = 0;
	public static final int TYPE_TEXT = 1;
	
	private int type;
	private String data;
	private LinkedList<String> toNumbers;
	private LinkedList<String> toContacts;
	private LinkedList<String> toGroups;
	
	public XMLMessage() {
		type = TYPE_TEXT;
		toNumbers = new LinkedList<String>();
		toContacts = new LinkedList<String>();
		toGroups = new LinkedList<String>();
	}

	public void addNumber(String number) {
		toNumbers.add(number);
	}
	
	public void addContact(String contact) {
		toContacts.add(contact);
	}
	
	public void addGroup(String group) {
		toGroups.add(group);
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public LinkedList<String> getToNumbers() {
		return toNumbers;
	}

	public LinkedList<String> getToContacts() {
		return toContacts;
	}

	public LinkedList<String> getToGroups() {
		return toGroups;
	}

}
