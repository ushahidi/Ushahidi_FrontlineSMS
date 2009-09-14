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
package net.frontlinesms.plugins.forms.ui.components;

import javax.swing.Icon;
import javax.swing.JLabel;



public class PaletteComponent extends JLabel {
	private static final long serialVersionUID = -4471979718212273601L;
	
	private FComponent component;
	
	public PaletteComponent(String str) {
		super(str);
	}
	
	public PaletteComponent(Icon icon) {
		super(icon);
	}

	public FComponent getComponent() {
		return component;
	}

	public void setComponent(FComponent component) {
		this.component = component;
	}
}
