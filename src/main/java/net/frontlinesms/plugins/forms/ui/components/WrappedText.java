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
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

/**
 * FComponent for displaying and editing a wrapped block of text on a form.
 * @author Kadu
 */
@SuppressWarnings("serial")
public class WrappedText extends FComponent {
	/** Max width, in pixels, of a line of text */
	private static final int MAX_TEXT_WIDTH_PER_LINE = 194;

	/** @see FComponent#getDescription() */
	@Override
	public String getDescription() {
		return InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_WRAPPED_TEXT);
	}

	/** @see FComponent#getIcon() */
	@Override
	public String getIcon() {
		return "multilinetext.png";
	}

	/** @see FComponent#getDrawingComponent() */
	@Override
	public Container getDrawingComponent() {
		JPanel pn = new JPanel();
		pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
		pn.setBorder(BorderFactory.createTitledBorder(""));
		String labelText = super.getDisplayLabel();
		String text[] = wrapText(labelText, pn);
		int count = 0;
		for (String s : text) {
			if (!s.equals("")) {
				++count;
				pn.add(new JLabel(s));
			}
		}
		setRenderHeight(30 + (count * 15));
		return pn;
	}
	
	/**
	 * Splits a string of text ready to be displayed in a {@link JPanel}
	 * @param text the text to split
	 * @param pn the panel the text will be displayed in
	 * @return an array containing each line of text to show in the panel
	 */
	private String[] wrapText(String text, JPanel pn) {
		FontMetrics m = pn.getFontMetrics(pn.getFont());
		int width = m.stringWidth(text);
		String ret[] = new String[ (width / MAX_TEXT_WIDTH_PER_LINE) + 1];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = getString(text, m);
			text = text.substring(text.indexOf(ret[i]) + ret[i].length());
		}
		return ret;
	}

	/**
	 * Gets the portion of the string, starting at the beginning, which will fit on one line
	 * of the visible component. 
	 * @param text the text to split
	 * @param m font metrics for the panel which the string will be displayed on
	 * @return the next line of text to display
	 */
	private String getString(String text, FontMetrics m) {
		int end = 0;
		String ret = text.substring(0, end);
		while (m.stringWidth(ret) < MAX_TEXT_WIDTH_PER_LINE && (end + 1) <= text.length()) {
			end++;
			ret = text.substring(0, end);
		}
		return ret;
	}
}
