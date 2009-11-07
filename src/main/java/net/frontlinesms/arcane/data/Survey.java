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


public class Survey extends BaseDataObject {
	private boolean isLive;
	private String keyword;
	private String description;
	private long startDate;
	private long endDate;
	private int hits;

	
	
	public boolean isLive() {
		return isLive;
	}



	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}



	public String getKeyword() {
		return keyword;
	}



	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public long getStartDate() {
		return startDate;
	}



	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}



	public long getEndDate() {
		return endDate;
	}



	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}



	public int getHits() {
		return hits;
	}



	public void setHits(int hits) {
		this.hits = hits;
	}



	@Override
	public String toString() {
		return "Survey\n"
			+ "\t     isLive : " + isLive + "\n"
			+ "\t    keyword : " + keyword + "\n"
			+ "\tdescription : " + description + "\n"
			+ "\t  startDate : " + DATE_FORMAT.format(startDate) + "\n"
			+ "\t    endDate : " + DATE_FORMAT.format(endDate) + "\n"
			+ "\t       hits : " + hits + "\n";
	}
}
