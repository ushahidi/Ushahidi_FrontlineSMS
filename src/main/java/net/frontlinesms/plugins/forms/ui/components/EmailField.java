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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

/**
 * FComponent for displaying and editing a form email field.
 * @author Kadu
 */
@SuppressWarnings("serial")
public class EmailField extends FComponent {
	/** Create a new instance of this class and initialise its render height */
	public EmailField() {
		setRenderHeight(60);
	}

	/** @see FComponent#getDescription() */
	@Override
	public String getDescription() {
		return InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_E_MAIL_ADDRESS);
	}

	/** @see FComponent#getIcon() */
	@Override
	public String getIcon() {
		return "emailfield.png";
	}

	/** @see FComponent#getDrawingComponent() */
	@Override
	public Container getDrawingComponent() {
		JPanel pn = new JPanel();
		pn.setLayout(new FlowLayout());
		pn.setBorder(new TitledBorder(super.getDisplayLabel()));
		JLabel desc = new JLabel("@");
		int width = desc.getFontMetrics(desc.getFont()).stringWidth(desc.getText());
		width = renderWidth - 25 - width;
		JTextField p1 = new JTextField();
		p1.setPreferredSize(new Dimension(width/2, 20));
		p1.setEditable(false);
		JTextField p2 = new JTextField();
		p2.setPreferredSize(new Dimension((width/2) + width%2, 20));
		p2.setEditable(false);
		pn.add(p1);
		pn.add(desc);
		pn.add(p2);
		return pn;
	}
}
