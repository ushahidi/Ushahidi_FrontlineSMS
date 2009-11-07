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

public class SplitField {
	private FieldType type;
	/** Name to reference this field by internally */
	private String name;
	/** Maximum size of this field.  This is the column size in the data. */
	private int fieldSize;
	
	public SplitField(FieldType type, String name, int fieldSize) {
		this.type = type;
		this.name = name;
		this.fieldSize = fieldSize;
	}
	
	public String getName() {
		return name;
	}
	
	public int getFieldSize() {
		return fieldSize;
	}
	
	public FieldType getType() {
		return this.type;
	}
}
