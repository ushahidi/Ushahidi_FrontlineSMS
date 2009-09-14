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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.plugins.forms.ui.components.PreviewComponent;
import net.frontlinesms.plugins.forms.ui.components.VisualForm;
import net.frontlinesms.ui.SimpleConstraints;
import net.frontlinesms.ui.SimpleLayout;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

/**
 * This class represents the Preview.
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 */
public class PreviewPanel extends JPanel implements MouseListener, KeyListener {
	private static final long serialVersionUID = 1L;
	
	private VisualForm form = null;
	private static final int GAP = 5; // GAP between the components
	private DragListener dragListener;
	private DragSource dragSource;
	private int dragY = - 1; // Coordinate to draw a line (helping user to drop component)
	private int WIDTH = 235;
	
	public PreviewPanel(DragListener dragListener, DragSource dragSource) {
		setLayout(new SimpleLayout());
		setBorder(new TitledBorder(InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_PREVIEW)));
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.dragListener = dragListener;
		this.dragSource = dragSource;
	}
	
	/**
	 * Show the supplied form on the preview.
	 * 
	 * @param form
	 */
	public void showForm(VisualForm form) {
		this.form = form;
		deselectAll();
		refresh();
		FormsUiController.getInstance().showProperties();
	}
	
	/**
	 * Adds the supplied component to the preview finding the right position
	 * according to the supplied coordinates.
	 * 
	 * @param comp The component to be added.
	 * @param x
	 * @param y
	 */
	public void addComponent(PreviewComponent comp, int x, int y) {
		int index = getIndex(y);
		if (index == -1 || index >= form.getComponents().size()) {
			// We are adding to the end.
			form.getComponents().add(comp);
		} else {
			// We are adding to a specified position.
			form.getComponents().add(index, comp);
		}
		refresh();
	}

	/**
	 * This method finds the closest component to the supplied coordinate
	 * and returns its index.
	 * 
	 * @param y
	 * @return
	 */
	private int getIndex(int y) {
		int index = 0;
		for (PreviewComponent c : form.getComponents()) {
			Rectangle b = c.getDrawComponent().getBounds();
			if (y > b.y && y < b.y + b.height + GAP) {
				// We find the component
				if (y < b.y + ( (b.height + GAP)/ 2) ) {
					// User dropped the component in the upper part of this component. 
					index = form.getComponents().indexOf(c);
				} else {
					// User dropped the component in the lower part of this component. 
					index = form.getComponents().indexOf(c) + 1;
				}
				break;
			} 
		}
		// If we get index = 0 and the list is not empty, we verify if the user
		// dropped the component in the end of the list, so we'll have to add
		// the component to the end of the panel.
		if (index == 0 && !form.getComponents().isEmpty()) {
			Rectangle b = form.getComponents().get(form.getComponents().size() - 1).getDrawComponent().getBounds();
			index = y > b.y + b.height ? form.getComponents().size() : index; 
		}
		return index;
	}
	
	/**
	 * Returns the selected component.
	 * 
	 * @return
	 */
	public PreviewComponent getSelectedItem() {
		int index = getSelectedIndex();
		return index == -1 ? null : form.getComponents().get(index);
	}
	
	/**
	 * Returns the selected component index.
	 * 
	 * @return
	 */
	private int getSelectedIndex() {
		for (int i = 0; i < form.getComponents().size(); i++) {
			PreviewComponent c = form.getComponents().get(i);
			if (c.isSelected()) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Moves the supplied component finding the right position
	 * according to the supplied coordinates.
	 * 
	 * @param comp
	 * @param x
	 * @param y
	 */
	public void moveComponent(Component comp, int x, int y) {
		int index = getIndex(y); // Index to be moved.
		PreviewComponent toRemove = findComponent(comp);
		int exIndex = form.getComponents().indexOf(toRemove); // Index before being moved.
		if (index != exIndex) {
			form.getComponents().remove(toRemove);
			// If the user are moving down, we need to decrease the future index by one,
			// because we have just removed the component.
			if (index > exIndex) index--;
			if (index != -1 && index <= form.getComponents().size()) {
				form.getComponents().add(index, toRemove);
			}
		}
		refresh();
	}

	/**
	 * Searches and returns the preview component that contains the supplied component.
	 * 
	 * @param comp
	 * @return
	 */
	private PreviewComponent findComponent(Component comp) {
		for (PreviewComponent c : form.getComponents()) {
			if (c.getDrawComponent().equals(comp)) {
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Removes the selected component from preview.
	 * 
	 * @param c
	 */
	public PreviewComponent removeComponent(Component c) {
		PreviewComponent cc = findComponent(c);
		form.getComponents().remove(cc);
		refresh();
		return cc;
	}
	
	/**
	 * Refreshes the preview.
	 */
	public void refresh() {
		this.removeAll();
		if (form != null) {
			int x = 9;
			int y = 20;
			for (PreviewComponent c : form.getComponents()) {
				c.updateDrawComponent();
				int width = this.getBounds().width == 0 ? WIDTH : this.getBounds().width;
				this.add(c.getDrawComponent(), new SimpleConstraints(x, y, width - 18, c.getComponent().getHeight()));
				addListenerRecursevely(c.getDrawComponent());
				y+= c.getComponent().getHeight() + GAP;
			}
		}
		this.validate();
		this.repaint();
		dragY = -1;
	}

	/**
	 * Adds mouse/key/drag listeners to the supplied component and its children.
	 * 
	 * @param c
	 */
	private void addListenerRecursevely(Container c) {
		c.addMouseListener(this);
		c.addKeyListener(this);
		dragSource.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_COPY, dragListener);
		for (Component c1: c.getComponents()) {
			if (c1 instanceof Container) {
				addListenerRecursevely((Container) c1);
			}
		}
	}

	/**
	 * Remove the selection.
	 */
	private void deselectAll() {
		if (form != null) {
			for (PreviewComponent c : form.getComponents()) {
				c.setSelected(false);
				c.getDrawComponent().validate();
				c.getDrawComponent().repaint();
			}
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		for (PreviewComponent c : form.getComponents()) {
			if (c.isSelected()) {
				// Draw selection
				g.setColor(new Color(0x316ac5));
				Rectangle b = c.getDrawComponent().getBounds();
				g.drawRect(this.getBounds().x + 3, b.y - 1, this.getBounds().width - 8, b.height + 1);
				break;
			}
		}
		if (dragY != -1) {
			// Draw line to help users to drop components.
			g.setColor(Color.BLACK);
			g.drawLine(this.getBounds().x + 3, dragY, this.getBounds().width - 3, dragY);
			g.drawLine(this.getBounds().x + 3, dragY - 4, this.getBounds().x + 3, dragY + 4);
			g.drawLine(this.getBounds().width - 3, dragY - 4, this.getBounds().width - 3, dragY + 4);
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			// If it's a left-click, we change the selection.
			deselectAll();
			Object source = e.getSource();
			if (source instanceof PreviewPanel) {
				// User clicked on the panel, so we check coordinates.
				for (PreviewComponent c : form.getComponents()) {
					Rectangle b = c.getDrawComponent().getBounds();
					if (e.getY() > b.y && e.getY() <= b.y + b.height + GAP) {
						c.setSelected(true);
						break;
					}
				}
			} else {
				// User clicked in a component, so we just need to find it.
				Component comp = (Component) source;
				findComponent(getContainerParent(comp)).setSelected(true);
			}
			// We need to repaint to show the selection and show properties for this component.
			this.validate();
			this.repaint();
			this.requestFocus();
			FormsUiController.getInstance().showProperties();
		}
	}


	/**
	 * Searches the preview panel in the component.
	 * 
	 * @param obj
	 * @return
	 */
	private static Component getContainerParent(Component obj) {
		if (obj instanceof JPanel || obj instanceof JScrollPane) 
			return obj;
		return getContainerParent(obj.getParent());
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			moveSelectionUp();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			moveSelecionDown();
		} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			removeSelected();
		}
	}

	/**
	 * Removes the selected component after the user pressing 'delete'.
	 */
	private void removeSelected() {
		form.getComponents().remove(getSelectedItem());
		refresh();
		FormsUiController.getInstance().showProperties();
	}

	/**
	 * Moves the selection down.
	 */
	private void moveSelecionDown() {
		int index = getSelectedIndex();
		if (index != -1 && index != form.getComponents().size() - 1) {
			deselectAll();
			index++;
			form.getComponents().get(index).setSelected(true);
			this.validate();
			this.repaint();
			FormsUiController.getInstance().showProperties();
		}
	}
	
	/**
	 * Moves the selection up.
	 */
	private void moveSelectionUp() {
		int index = getSelectedIndex();
		if (index > 0) {
			deselectAll();
			index--;
			form.getComponents().get(index).setSelected(true);
			this.validate();
			this.repaint();
			FormsUiController.getInstance().showProperties();
		}
	}

	public void keyTyped(KeyEvent e) {}

	public void setDragY(int dragY) {
		this.dragY = dragY;
	}

	public VisualForm getForm() {
		return form;
	}
}
