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

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.plugins.forms.data.domain.FormField;
import net.frontlinesms.plugins.forms.data.domain.FormFieldType;
import net.frontlinesms.plugins.forms.ui.components.*;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

/**
 * This class represents the Palette.
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 */
public class PalettePanel extends JPanel {
	private static Logger LOG = Utils.getLogger(PalettePanel.class);
	private static final long serialVersionUID = 1799362551968963234L;
	
	public PalettePanel(DragListener dragListener, DragSource source) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_PALETTE)));
		for (FormFieldType fieldType : FormFieldType.values()) {
			Class<? extends FComponent> clazz = FComponent.getComponentClass(fieldType);
			FComponent c;
			try {
				c = clazz.newInstance();
				PaletteComponent label = getComponent(c);
				source.createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_COPY, dragListener);
				add(label);
			} catch (Exception e) {
				LOG.debug("", e);
			}
		}
		setToolTipText(InternationalisationUtils.getI18NString(FrontlineSMSConstants.TOOLTIP_DRAG_TO_PREVIEW));
	}

	/**
	 * Returns a palette component using the supplied FComponent instance
	 * to find the icon and the name of the component.
	 * 
	 * @param c
	 * @return
	 */
	private PaletteComponent getComponent(FComponent c) {
		PaletteComponent label = new PaletteComponent(c.getDescription());
		label.setIcon(new ImageIcon(Utils.getImage("/icons/components/" + c.getIcon(), getClass())));
		label.setComponent(c);
		return label;
	}
}	
