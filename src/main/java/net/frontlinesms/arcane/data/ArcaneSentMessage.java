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
package net.frontlinesms.arcane.data;

public class ArcaneSentMessage extends BaseDataObject {
	private int id;
	private long timestamp;
	private String content;
	private String group;
	private int recipients;
	
	
	
	public int getId() {
		return id;
	}



	public String getGroup() {
		return group;
	}



	public void setGroup(String group) {
		this.group = group;
	}



	public int getRecipients() {
		return recipients;
	}



	public void setRecipients(int recipients) {
		this.recipients = recipients;
	}



	public void setId(int id) {
		this.id = id;
	}



	public long getTimestamp() {
		return timestamp;
	}



	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}



	public String getContent() {
		return content;
	}



	public void setContent(String content) {
		this.content = content;
	}



	@Override
	public String toString() {
		return "SentMessage\t        id : " + id + "\n"
			 + "       \t   content : " + content + "\n"
			 + "       \t timestamp : " + DATE_FORMAT.format(timestamp) + "\n"
			 + "       \t     group : " + group + "\n"
			 + "       \trecipients : " + recipients + "\n";
	}
}
