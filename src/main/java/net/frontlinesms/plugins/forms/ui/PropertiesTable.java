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

import java.util.Vector;

import javax.swing.JTable;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

/**
 * This class represents the Properties Table.
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 */
public class PropertiesTable extends JTable {
	private static final long serialVersionUID = -7523734570779253511L;
	
	private MyTableModel model;
	private Vector<String> columns;
	private Vector<String> data = null;
	
	public PropertiesTable() {
		model = new MyTableModel();
		columns = new Vector<String>();
		columns.add(InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_PROPERTY));
		columns.add(InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_VALUE));
		model.setDataVector(data, columns);
		this.setModel(model);
		this.getTableHeader().setReorderingAllowed(false);
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		this.setBorder(null);
	}
	
	/**
	 * Removes all data from table.
	 */
	public void clean() {
		model.setDataVector(data, columns);
	}
	
	/**
	 * Adds a property to the table.
	 * 
	 * @param property
	 * @param value
	 */
	public void addProperty(String property, String value) {
		model.addRow(new Object[]{property, value});
	}
}
