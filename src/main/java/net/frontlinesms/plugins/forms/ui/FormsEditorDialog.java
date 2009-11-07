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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Method;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.plugins.forms.ui.components.FComponent;
import net.frontlinesms.plugins.forms.ui.components.PreviewComponent;
import net.frontlinesms.plugins.forms.ui.components.VisualForm;
import net.frontlinesms.ui.SimpleConstraints;
import net.frontlinesms.ui.SimpleLayout;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import static net.frontlinesms.FrontlineSMSConstants.*;
/**
 * This class represents the main frame of the program.
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 */
public class FormsEditorDialog extends JDialog {
	private static final long serialVersionUID = 2071317022144454655L;
	private PropertiesTable propertiesTable;
	private DrawingPanel pnDrawing;
	
	private VisualForm current;
	private JTextField tfFormName;
	
	public FormsEditorDialog(Frame owner) {
		super(owner, "FrontlineSMS - " + InternationalisationUtils.getI18NString(FormsThinletTabController.I18N_KEY_FORMS_EDITOR), true);
		propertiesTable = new PropertiesTable();
		getContentPane().setLayout(new SimpleLayout());
		pnDrawing = new DrawingPanel();
		getContentPane().add(pnDrawing, new SimpleConstraints(0, 0, 500, 580));
		JScrollPane sp = new JScrollPane(propertiesTable);
		sp.setBorder(new TitledBorder(InternationalisationUtils.getI18NString(COMMON_PROPERTIES)));
		getContentPane().add(sp, new SimpleConstraints(510, 0, 280, 580));
		
		tfFormName = new JTextField();
		tfFormName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		JLabel formName = new JLabel(InternationalisationUtils.getI18NString(FormsThinletTabController.I18N_KEY_FORM_NAME) + ": ");
		formName.setIcon(new ImageIcon(Utils.getImage("/icons/form.png", getClass())));
		FontMetrics m = formName.getFontMetrics(formName.getFont());
		int width = m.stringWidth(formName.getText()) + formName.getIcon().getIconWidth();
		getContentPane().add(formName, new SimpleConstraints(160, 590));
		getContentPane().add(tfFormName, new SimpleConstraints(160 + width + 20, 588, 200, null));
		
		JButton btSave = new JButton(InternationalisationUtils.getI18NString(ACTION_SAVE), new ImageIcon(Utils.getImage("/icons/tick.png", getClass())));
		btSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		JButton btCancel = new JButton(InternationalisationUtils.getI18NString(ACTION_CANCEL), new ImageIcon(Utils.getImage("/icons/cross.png", getClass())));
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		
		getContentPane().add(btSave, new SimpleConstraints(160 + width + 20 + 200 + 20, 585));
		getContentPane().add(btCancel, new SimpleConstraints(160 + width + 20 + 200 + 20 + 90, 585));
		
		this.setResizable(false);
		this.setSize(800, 650);
		try {
			// This method is only available in Java6+.  It might be sensible just to ditch
			// the code altogether, although surely there is a pre-Java6 way to set an icon
			// for a window???
			Method setIconImage = this.getClass().getDeclaredMethod("setIconImage", Image.class);
			setIconImage.setAccessible(true);
			setIconImage.invoke(this, Utils.getImage("/icons/frontline_icon.png", getClass()));
		} catch(Throwable t) {
			// We're running on pre-1.6 :)
		}
		centralise(owner);
		
		addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e) { cancel(); }
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			
		});
	}
	
	private void cancel() {
		current = null;
		dispose();
	}
	
	private void save() {
		current = pnDrawing.getCurrent();
		String name = tfFormName.getText();
		if (name.equals("")) {
			JOptionPane.showMessageDialog(this, InternationalisationUtils.getI18NString(FormsThinletTabController.I18N_KEY_MESSAGE_FORM_NAME_BLANK));
			return;
		}
		current.setName(name);
		dispose();
	}
	
	/**
	 * Centralises this dialog according to the frame owner. 
	 */
	private void centralise(Frame owner) {
		Dimension screen_size = owner.getSize();
		Point p = owner.getLocation();
		this.setLocation(p.x + ((screen_size.width - this.getWidth()) >> 1), 
				p.y + ((screen_size.height - this.getHeight()) >> 1));
	}

	
	/**
	 * Invokes the preview refresh.
	 */
	public void refreshPreview() {
		pnDrawing.refreshPreview();
	}
	
	/**
	 * Retrieves the selected preview component.
	 * @return
	 */
	public PreviewComponent getSelectedComponent() {
		return pnDrawing.getSelectedComponent(); 
	}
	
	/**
	 * Show the properties of the selected component in the properties table.
	 */
	public void showProperties() {
		propertiesTable.clean();
		if (getSelectedComponent() != null) {
			FComponent component = getSelectedComponent().getComponent();
			propertiesTable.addProperty(FComponent.PROPERTY_TYPE, component.getDescription());
			String label = component.getLabel();
			if(label == null) label = "";
			propertiesTable.addProperty(FComponent.PROPERTY_LABEL, label);
		}
	}
	
	public void setForm(VisualForm form) {
		pnDrawing.setForm(form);
		tfFormName.setText(form.getName());
	}

	public VisualForm getVisualForm() {
		return current;
	}
}
