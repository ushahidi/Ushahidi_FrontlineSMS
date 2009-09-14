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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.ui.i18n.InternationalisationUtils;


@SuppressWarnings("serial")
public class TextArea extends FComponent {
	public TextArea() {
		super();
		renderHeight = 100;
	}

	@Override
	public String getDescription() {
		return InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_TEXT_AREA);
	}

	@Override
	public String getIcon() {
		return "textarea.png";
	}

	@Override
	public Container getDrawingComponent() {
		JTextArea tf = new JTextArea();
		JScrollPane sp = new JScrollPane(tf);
		sp.setBorder(new TitledBorder(super.getDisplayLabel()));
		tf.setColumns(20);
		tf.setEditable(false);
		return sp;
	}

}
