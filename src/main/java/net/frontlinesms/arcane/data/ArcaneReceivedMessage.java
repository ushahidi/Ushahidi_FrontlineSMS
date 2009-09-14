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

public class ArcaneReceivedMessage extends BaseDataObject {
	private int id;
	private long timestamp;
	private String msisdn;
	private String content;
	
	
	
	public int getId() {
		return id;
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



	public String getMsisdn() {
		return msisdn;
	}



	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}



	public String getContent() {
		return content;
	}



	public void setContent(String content) {
		this.content = content;
	}



	@Override
	public String toString() {
		return "ReceivedMessage\t        id : " + id + "\n"
			 + "       \t    msisdn : " + msisdn + "\n"
			 + "       \t   content : " + content + "\n"
			 + "       \t timestamp : " + DATE_FORMAT.format(timestamp) + "\n";
	}
}
