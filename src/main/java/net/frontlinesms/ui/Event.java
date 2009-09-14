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
package net.frontlinesms.ui;

// FIXME Should be renamed to something less generic. 
/**
 * This represents an event instance that will appear on the latest events table.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 19/02/2009
 */
public class Event {
	public static final int TYPE_INCOMING_MESSAGE = 0;
	public static final int TYPE_OUTGOING_MESSAGE = 1;
	public static final int TYPE_OUTGOING_MESSAGE_FAILED = 2;
	public static final int TYPE_OUTGOING_EMAIL = 3;
	public static final int TYPE_PHONE_CONNECTED = 4; 
	public static final int TYPE_SMS_INTERNET_SERVICE_CONNECTED = 8;
	public static final int TYPE_SMS_INTERNET_SERVICE_RECEIVING_FAILED = 9;
	
	private int type;
	private String description;
	private long time;
	
	public Event(int type, String description) {
		this.type = type;
		this.description = description;
		this.time = System.currentTimeMillis();
	}

	public int getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public long getTime() {
		return time;
	}
}
