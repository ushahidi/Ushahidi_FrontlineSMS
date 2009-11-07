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

public class SurveyPattern extends SplitPattern {
	private static final BoolString IS_LIVE = new BoolString("Live", "Dormant");

	private static SurveyPattern instance;
	
	public static synchronized SurveyPattern getInstance() {
		if(instance == null) instance = new SurveyPattern();
		return instance;
	}
	
	private SurveyPattern() {
		addField(FieldType.BOOLEAN, "live", 7);
		addField(FieldType.STRING, "keyword", 12);
		addField(FieldType.STRING, "description", 85);
		addField(FieldType.STRING, "startDate", 8);
		addField(FieldType.STRING, "endDate", 8);
		addField(FieldType.STRING, "hits", 2);
	}
	
	@SuppressWarnings("unchecked")
	public Survey parse(String dataString) {
		Survey survey = new Survey();
		
		String[] columns = getColumns(dataString);
		
		int index = -1;
		
		survey.setLive(getBoolean(IS_LIVE, columns[++index]));
		survey.setKeyword(getString(columns[++index]));
		survey.setDescription(getString(columns[++index]));
		survey.setStartDate(getDate(columns[++index]));
		survey.setEndDate(getDate(columns[++index]));
		survey.setHits(getInt(columns[++index]));
		
		return survey;
	}
}
