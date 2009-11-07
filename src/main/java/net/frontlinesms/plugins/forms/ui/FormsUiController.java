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
package net.frontlinesms.plugins.forms.ui;

import java.awt.Frame;

import net.frontlinesms.plugins.forms.ui.components.FComponent;
import net.frontlinesms.plugins.forms.ui.components.VisualForm;

/**
 * This class is responsible for doing all actions triggered by UI classes.
 * This class implements the Singleton design pattern to ensure only one instance
 * of this class during the execution.
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 */
public class FormsUiController {
	private static FormsUiController instance;
	private FormsEditorDialog mainFrame;
	
	/**
	 * Show properties of the selected component.
	 */
	public void showProperties() {
		mainFrame.showProperties();
	}
	
	/**
	 * Method invoked when there is a change on the properties table. We just
	 * update the preview if there was a modification on the value.
	 * 
	 * @param property Property changed.
	 * @param value The new value.
	 */
	public void propertiesChanged(String property, String value) {
		if(property != null && property.equals(FComponent.PROPERTY_LABEL)) {
			FComponent comp = mainFrame.getSelectedComponent().getComponent();
			if(comp.getLabel() == null || !comp.getLabel().equals(value)) {
				comp.setLabel(value);
				mainFrame.refreshPreview();				
			}
		}
	}
	
	public VisualForm showFormsEditor(Frame owner, VisualForm form) {
		mainFrame = new FormsEditorDialog(owner);
		mainFrame.setForm(form);
		mainFrame.setVisible(true);
		return mainFrame.getVisualForm();
	}
	
	public static FormsUiController getInstance() {
		if (instance == null) instance = new FormsUiController();
		return instance;
	}
	
}
