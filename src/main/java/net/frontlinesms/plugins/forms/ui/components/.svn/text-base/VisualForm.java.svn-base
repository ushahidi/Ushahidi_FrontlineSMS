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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.frontlinesms.Utils;
import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.domain.FormField;

@SuppressWarnings("serial")
public class VisualForm extends FComponent {
	private static final Logger LOG = Utils.getLogger(VisualForm.class);
	
	private List<PreviewComponent> components = new ArrayList<PreviewComponent>();

	private String name;
	
	public void addComponent(PreviewComponent component) {
		components.add(component);
	}
	
	public void removeComponent(PreviewComponent component) {
		components.remove(component);
	}

	public List<PreviewComponent> getComponents() {
		return components;
	}

	@Override
	public String getDescription() {
		return "Form";
	}

	@Override
	public Container getDrawingComponent() {
		return null;
	}

	@Override
	public String getIcon() {
		return "form.png";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static VisualForm getVisualForm(Form f) {
		VisualForm ret = new VisualForm();
		ret.setName(f.getName());
		try {
			for (FormField field : f.getFields()) {
				PreviewComponent comp = new PreviewComponent();
				FComponent component;
				Class<? extends FComponent> clazz = FComponent.getComponentClass(field.getType());
				component = clazz.newInstance();
				component.setLabel(field.getLabel());
				comp.setComponent(component);
				comp.setFormField(field);
				ret.addComponent(comp);
			}
		} catch (InstantiationException e) {
			LOG.debug("", e);
		} catch (IllegalAccessException e) {
			LOG.debug("", e);
		}
		return ret;
	}
}
