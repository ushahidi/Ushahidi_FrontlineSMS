///*
// * FrontlineSMS <http://www.frontlinesms.com>
// * Copyright 2007, 2008 kiwanja
// * 
// * This file is part of FrontlineSMS.
// * 
// * FrontlineSMS is free software: you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or (at
// * your option) any later version.
// * 
// * FrontlineSMS is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// * General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License
// * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
// */
//package net.frontlinesms.debug;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import net.frontlinesms.data.db.DbController;
//
//public class Table {
//	private static final boolean DEBUG = false;
//	
//	private final String name;
//	private final List<Column> columns;
//	
//	private Table(String name) {
//		this.name = name;
//		this.columns = new ArrayList<Column>();
//	}
//	
//	public String getName() {
//		return this.name;
//	}
//	
//	public Collection<Column> getColumns() {
//		return this.columns;
//	}
//	
//	public static Table create(String name) throws SQLException {
//		Table t = new Table(name);
//		
//		Statement s = null;
//		try {
//			s = DbController.getConnection().createStatement();
//			// get column names
//			s.execute("SELECT * FROM " + name + " WHERE 1=1");
//			ResultSet r = s.getResultSet();
//	
//			ResultSetMetaData meta = r.getMetaData();
//			int columnCount = meta.getColumnCount();
//			if(DEBUG) System.out.println("ATTRIBUTE_COLUMNS:");
//			for(int i=1; i<=columnCount; ++i) {
//				if(DEBUG) System.out.println(i + ": " + meta.getColumnName(i));
//				t.addColumn(meta.getColumnName(i));
//			}
//					
//			return t;
//		} finally {
//			if (s != null) {
//				try { 
//					s.close(); 
//				} catch(Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//		}
//	}
//	
//	private void addColumn(Column c) {
//		columns.add(c);
//	}
//	
//	private void addColumn(String columnName) {
//		addColumn(new Column(columnName));
//	}
//}
