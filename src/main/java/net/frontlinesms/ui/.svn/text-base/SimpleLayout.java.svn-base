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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.HashMap;
import java.util.Map;

/**
 * A very simple layout manager.
 * @author Alex
 */
public class SimpleLayout implements LayoutManager2 {
	private Map<Component, SimpleConstraints> components = new HashMap<Component, SimpleConstraints>();

	public synchronized void addLayoutComponent(Component comp, Object constraints) {
		// Non SimpleConstraints'd components are ignored
		if (constraints instanceof SimpleConstraints) {
			SimpleConstraints simpleConstraints = (SimpleConstraints)constraints;
			if(components.containsKey(comp)) {
				// Can a component be added in two places?  Seems a little sketchy...
			} else {
				components.put(comp, simpleConstraints);
			}
		}
	}

	/**
	 * Simple layout is always aligned along the origin, so this method always returns 0.
	 */
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	/**
	 * Simple layout is always aligned along the origin, so this method always returns 0.
	 */
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	/**
	 * Uncaches all data from this layout.
	 */
	public void invalidateLayout(Container target) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Get's the largest possible dimension for this layout.
	 */
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * This method should not be used :)
	 */
	public void addLayoutComponent(String name, Component comp) {
		throw new IllegalArgumentException("This method should not be used.");
	}

	public synchronized void layoutContainer(Container parent) {
		for(Component component : components.keySet()) {
			SimpleConstraints con = components.get(component);
			Integer width = con.getWidth();
			if(width == null) width = (int)component.getPreferredSize().getWidth();
			Integer height = con.getHeight();
			if(height == null) height = (int)component.getPreferredSize().getHeight();
			component.setBounds(con.getX(), con.getY(), width, height);
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		return getDimensions();
	}

	public Dimension preferredLayoutSize(Container parent) {
		return getDimensions();
	}
	
	/**
	 * Works out the width and height required to see all components.
	 * @return
	 */
	private Dimension getDimensions() {
		int width = 0;
		int height = 0;
		
		for(Component comp : components.keySet()) {
			SimpleConstraints con = components.get(comp);
			int right = con.getX();
			if(con.getWidth() != null) right += con.getWidth();
			else right += comp.getPreferredSize().getWidth();
			width = Math.max(width, right);
			
			int bottom = con.getY();
			if(con.getWidth() != null) bottom += con.getHeight();
			else bottom += comp.getPreferredSize().getHeight();
			height = Math.max(height, bottom);
		}
		
		return new Dimension(width, height);
	}

	/**
	 * Attempts to remove the supplied component from this layout.  If the component
	 * does not exist, this method will fail silently.
	 */
	public synchronized void removeLayoutComponent(Component comp) {
		components.remove(comp);
	}

}
