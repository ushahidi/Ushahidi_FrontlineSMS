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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DateFormatter;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.ui.i18n.InternationalisationUtils;


/**
 * FComponent for displaying and editing a form date field.
 * @author Kadu
 */
@SuppressWarnings("serial")
public class DateField extends FComponent {
	/** @see FComponent#getDescription() */
	@Override
	public String getDescription() {
		return InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_DATE);
	}

	/** @see FComponent#getIcon() */
	@Override
	public String getIcon() {
		return "datefield.png";
	}

	/** @see FComponent#getDrawingComponent() */
	@Override
	public Container getDrawingComponent() {
		JPanel pn = new JPanel();
		pn.setLayout(new FlowLayout());
		JLabel desc = new JLabel(super.getDisplayLabel() + ":");
		desc.setForeground(new Color(0, 70, 213));
		pn.add(desc);
		int width = desc.getFontMetrics(desc.getFont()).stringWidth(desc.getText());
		width = renderWidth - width;
		DateFormatter df = new DateFormatter(InternationalisationUtils.getDateFormat());
		JFormattedTextField tf = new JFormattedTextField(df);
		tf.setPreferredSize(new Dimension(width, 20));
		tf.setEditable(false);
		pn.add(tf);
		return pn;
	}

}
