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

/**
 * Constraints describing the location and dimensions of an AWT component.
 * @author Alex
 */
public class SimpleConstraints {
	/** X co-ordinate of the component */
	private final int x;
	/** Y co-ordinate of the component */
	private final int y;
	/** Width of the component */
	private final Integer width;
	/** Width of the component */
	private final Integer height;
	
	public SimpleConstraints(int x, int y, Integer width, Integer height) {
		if((width != null && width < 0) || (height != null && height < 0))
			throw new IllegalArgumentException("Cannot constrain with negative width or height.  Supplied: ("+width+","+height+")");
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public SimpleConstraints(int x, int y) {
		this(x, y, null, null);
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height;
	}

}
