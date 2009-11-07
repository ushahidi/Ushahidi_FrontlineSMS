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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.plugins.forms.ui.components.PaletteComponent;
import net.frontlinesms.plugins.forms.ui.components.PreviewComponent;
import net.frontlinesms.plugins.forms.ui.components.VisualForm;
import net.frontlinesms.ui.Icon;
import net.frontlinesms.ui.SimpleConstraints;
import net.frontlinesms.ui.SimpleLayout;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import static net.frontlinesms.FrontlineSMSConstants.*;
/**
 * This class represents the UI for palette and preview components.
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 */
public class DrawingPanel extends JPanel {
	private static final int HEIGHT = 580;

	private static final long serialVersionUID = -623321310155700309L;
	
	private PalettePanel pnPalette;
	private PreviewPanel pnPreview;
	
	private DragListener dragListener;

	private JScrollPane scrollPreview;

	private DragSource source;

	public DrawingPanel() {
		dragListener = new DragListener(this);
		source = new DragSource();
		source.addDragSourceMotionListener(dragListener);
		
		setLayout(new SimpleLayout());
		
		JLabel bin = new JLabel(new ImageIcon(Utils.getImage(Icon.BIN, getClass())));
		bin.setToolTipText(InternationalisationUtils.getI18NString(TOOLTIP_DRAG_TO_REMOVE));
		add(bin, new SimpleConstraints(470, HEIGHT - 40));
		
		add(new JLabel(InternationalisationUtils.getI18NString(SENTENCE_DELETE_KEY) + "."),
				new SimpleConstraints(255, HEIGHT - 47));
		add(new JLabel(InternationalisationUtils.getI18NString(SENTENCE_UP_KEY) + "."),
				new SimpleConstraints(255, HEIGHT - 32));
		add(new JLabel(InternationalisationUtils.getI18NString(SENTENCE_DOWN_KEY) + "."),
				new SimpleConstraints(255, HEIGHT - 17));
		
		pnPalette = new PalettePanel(dragListener, source);
		JScrollPane sp1 = new JScrollPane(pnPalette);
		sp1.setBorder(null);
		sp1.setPreferredSize(new Dimension(250, HEIGHT));
		add(sp1, new SimpleConstraints(0, 0, 250, HEIGHT));
		
		pnPreview = new PreviewPanel(dragListener, source);
		scrollPreview = new JScrollPane(pnPreview);
		scrollPreview.setBorder(null);
		scrollPreview.setPreferredSize(new Dimension(250, HEIGHT - 50));
		scrollPreview.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPreview, new SimpleConstraints(250, 0, 250, HEIGHT - 50));
		
		new DropTarget(bin, DnDConstants.ACTION_COPY, dragListener);
		new DropTarget(pnPreview, DnDConstants.ACTION_COPY, dragListener);
	}

	/**
	 * Adds the supplied component to the preview in the supplied position.
	 * 
	 * @param component Component to be added.
	 * @param x
	 * @param y
	 */
	public void addToPreview(PaletteComponent component, int x, int y) {
		PreviewComponent newLabel = new PreviewComponent();
		newLabel.setComponent(component.getComponent().clone());		
		pnPreview.addComponent(newLabel, x, y);
		this.validate();
		this.repaint();
	}
	
	/**
	 * Retrieves the selection from the preview.
	 * @return
	 */
	public PreviewComponent getSelectedComponent() {
		return pnPreview.getSelectedItem();
	}
	
	/**
	 * Moves the supplied component in the preview to the supplied position.
	 * 
	 * @param component Component to be moved.
	 * @param x
	 * @param y
	 */
	public void moveComponentInPreview(Component component, int x, int y) {
		pnPreview.moveComponent(component, x, y);
	}
	
	/**
	 * Refreshes the preview.
	 */
	public void refreshPreview() {
		pnPreview.refresh();
	}
	
	/**
	 * Method invoked when the user moves the mouse during a drag and drop action.
	 * <br>We use the supplied position to draw a line in the preview to help the user
	 * to choose where to drop the component.
	 * 
	 * @param x
	 * @param y
	 */
	public void dragMoved(int x, int y) {
		Point p = pnPreview.getLocationOnScreen();
		Rectangle r = pnPreview.getBounds();
		if ( (x > p.x && x <= p.x + r.width) 
				&& (y > p.y && y <= p.y + r.height)) {
			pnPreview.setDragY(y - p.y);
		} else {
			pnPreview.setDragY(-1);
		}
		pnPreview.validate();
		pnPreview.repaint();
	}
	
	/**
	 * Removes the selected component from the preview.
	 * 
	 * @param c
	 */
	public void deleteFromPreview(Component c) {
		pnPreview.removeComponent(c);
		this.validate();
		this.repaint();
		FormsUiController.getInstance().showProperties();
	}
	
	public VisualForm getCurrent() {
		return pnPreview.getForm();
	}
	
	/**
	 * Prepare the preview for the supplied form.
	 * 
	 * @param form
	 * @param edit 
	 * @param edit 
	 */
	public void setForm(VisualForm form) {
		pnPreview.showForm(form);
	}
}
