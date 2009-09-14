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

import java.awt.Container;

import net.frontlinesms.plugins.forms.data.domain.FormField;

public class PreviewComponent {
	private boolean selected;
	private FComponent component;
	private Container drawComponent;
	
	private FormField formField;
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public FComponent getComponent() {
		return component;
	}

	public void setComponent(FComponent component) {
		this.component = component;
		this.drawComponent = component.getDrawingComponent();
	}

	public Container getDrawComponent() {
		return drawComponent;
	}
	
	public void updateDrawComponent() {
		this.drawComponent = component.getDrawingComponent();
	}

	public FormField getFormField() {
		return formField;
	}

	public void setFormField(FormField formField) {
		this.formField = formField;
	}
}
