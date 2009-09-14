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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;


/**
 * This class defines a model for the properties table.
 * 
 * @author Carlos Eduardo Genz 
 * <li> kadu(at)masabi(dot)com
 */
public class MyTableModel extends DefaultTableModel implements TableModelListener {
	private static final long serialVersionUID = 7207518673243281559L;
	
	public MyTableModel() {
		this.addTableModelListener(this);
	}
	
	public boolean isCellEditable(int row, int col) {
		// Only the second column is editable.
		return col == 1 && row != 0;
	}

	public void tableChanged(TableModelEvent e) {
		if (e.getFirstRow() > 0) {
			String property = (String) getValueAt(e.getFirstRow(), 0);
			String value = (String) getValueAt(e.getFirstRow(), 1);
			FormsUiController.getInstance().propertiesChanged(property, value);
		}
	}
}
