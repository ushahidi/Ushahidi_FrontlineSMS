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
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.ui.i18n.InternationalisationUtils;


@SuppressWarnings("serial")
public class CheckBox extends FComponent {
	@Override
	public String getDescription() {
		return InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_CHECKBOX);
	}

	@Override
	public String getIcon() {
		return "checkbox.png";
	}

	@Override
	public Container getDrawingComponent() {
		JPanel pn = new JPanel();
		pn.setLayout(new FlowLayout());
		JCheckBox cb = new JCheckBox(super.getDisplayLabel());
		cb.setPreferredSize(new Dimension(renderWidth, 20));
		cb.setEnabled(false);
		pn.add(cb);
		return pn;
	}
}
