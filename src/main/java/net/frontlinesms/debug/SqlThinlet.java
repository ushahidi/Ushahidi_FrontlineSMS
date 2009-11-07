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
//import java.io.IOException;
//import java.sql.DatabaseMetaData;
//import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//
//import net.frontlinesms.data.db.DbController;
//import net.frontlinesms.ui.i18n.LanguageBundle;
//import thinlet.Thinlet;
//
//public class SqlThinlet extends Thinlet {
//	private static final long serialVersionUID = 4520273780544078774L;
//
//	public SqlThinlet(LanguageBundle bundle) throws IOException, SQLException {
//		this.setResourceBundle(bundle.getProperties(), bundle.isRightToLeft());
//		add(parse("/ui/dialog/sqlBrowser.xml"));
//		
//		Object tableList = find("dbTables");
//		for(Table t : getTables()) {
//			add(tableList, createListItem(t));
//		}
//	}
//	
//	private static final Table[] getTables() {
//		ArrayList<Table> tables = new ArrayList<Table>();
//		ResultSet r = null;
//		try {
//			DatabaseMetaData dbData = DbController.getConnection().getMetaData();
//			ArrayList<String> tableTypes = new ArrayList<String>();
//			ResultSet tTypes = dbData.getTableTypes();
//			while(tTypes.next()) {
//				tableTypes.add(tTypes.getString(1));
//			}
//			r = dbData.getTables(null, "%", "%", tableTypes.toArray(new String[tableTypes.size()]));
//			while(r.next()) {
//				String tableName = r.getString("TABLE_NAME");
//				if(DbController.getType() != DbController.TYPE_DERBY_EMBEDDED || !tableName.startsWith("SYS")) {
//					tables.add(Table.create(tableName));
//				}
//			}
//			r.close();
//			return tables.toArray(new Table[tables.size()]);
//		} catch(SQLException ex) {
//			ex.printStackTrace();
//			return null;
//		} finally {
//			if (r != null) {
//				try { 
//					r.close(); 
//				} catch(Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//		}
//	}
//
//	private static final String asString(ResultSetMetaData meta, ResultSet r, int columnIndex) throws SQLException {
//		int columnType = meta.getColumnType(columnIndex);
//		switch(columnType) {
//			case java.sql.Types.BIT:
//			case java.sql.Types.TINYINT:
//			case java.sql.Types.SMALLINT:
//			case java.sql.Types.INTEGER:
//			case java.sql.Types.BIGINT:
//			case java.sql.Types.FLOAT:
//			case java.sql.Types.REAL:
//			case java.sql.Types.DOUBLE:
//			case java.sql.Types.NUMERIC:
//			case java.sql.Types.DECIMAL:
//				return Integer.toString(r.getInt(columnIndex));
//			case java.sql.Types.CHAR:
//			case java.sql.Types.VARCHAR:
//			case java.sql.Types.LONGVARCHAR:
//				return r.getString(columnIndex);
//			case java.sql.Types.DATE:
//				return r.getDate(columnIndex).toString();
//			case java.sql.Types.TIME:
//				return r.getTime(columnIndex).toString();
//			case java.sql.Types.TIMESTAMP:
//				return r.getTimestamp(columnIndex).toString();
//			case java.sql.Types.LONGVARBINARY:
//			case java.sql.Types.NULL:
//				return "" + null;
//			case java.sql.Types.BOOLEAN:
//				return Boolean.toString(r.getBoolean(columnIndex));
//			case java.sql.Types.OTHER:
//			case java.sql.Types.JAVA_OBJECT:
//			case java.sql.Types.DISTINCT:
//			case java.sql.Types.STRUCT:
//			case java.sql.Types.ARRAY:
//			case java.sql.Types.BLOB:
//			case java.sql.Types.CLOB:
//			case java.sql.Types.REF:
//			case java.sql.Types.DATALINK:
//			case java.sql.Types.BINARY:
//			case java.sql.Types.VARBINARY:
//			default: throw new IllegalArgumentException("Unrecognized SQL type: " + columnType);		}
//	}
//	
//	public void tableSelectionChanged(Object dbTableList) {
//		Table t = (Table)getAttachedObject(getSelectedItem(dbTableList));
//		Object tableTable = find("dbTable.contents");
//		removeAll(tableTable);
//		Object header = get(tableTable, HEADER);
//		removeAll(header);
//		for(Column c : t.getColumns()) {
//			add(header, createTableColumn(c));
//		}
//		
//		Statement s = null;
//		try {
//			s = DbController.getConnection().createStatement();
//			s.execute("SELECT * FROM " + t.getName() + " WHERE 1=1");
//			ResultSet r = s.getResultSet();
//			ResultSetMetaData meta = r.getMetaData();
//			int columnCount = meta.getColumnCount();
//			while(r.next()) {
//				Object row = createTableRow(null);
//				for(int i=1; i<=columnCount; ++i)
//					createTableCell(row, asString(meta, r, i));
//				add(tableTable, row);
//			}
//		} catch (SQLException ex) {
//			alert(ex);
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
//	/* Allow this thinlet window to close without killing the main frame */
//	public boolean destroy() {
//		return false;
//	}
//	
//	public void sqlExecute(String sql, Object resultTable) {
//		removeAll(resultTable);
//		Object header = get(resultTable, HEADER);
//		removeAll(header);
//		Statement s = null;
//		try {
//			s = DbController.getConnection().createStatement();
//			if(s.execute(sql)) {
//				ResultSet r = s.getResultSet();
//				
//				ResultSetMetaData meta = r.getMetaData();
//				int columnCount = meta.getColumnCount();
//				for(int i=1; i<=columnCount; ++i) add(header, createTableColumn(meta.getColumnName(i)));
//				
//				while(r.next()) {
//					Object row = createTableRow(null);
//					for(int i=1; i<=columnCount; ++i)
//						createTableCell(row, asString(meta, r, i));
//					add(resultTable, row);
//				}
//			} else {
//				alert(s.getUpdateCount() + " row(s) updated.");
//			}
//		} catch(SQLException ex) {
//			alert(ex);
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
//	public void clearTextfield(Object textfield) {
//		setString(textfield, TEXT, "");
//	}
//	
//	private final Object createTableColumn(Column c) {
//		return createTableColumn(c.getName());
//	}
//	
//	private final Object createTableColumn(String s) {
//		Object col = Thinlet.create(COLUMN);
//		setString(col, TEXT, s);
//		return col;
//	}
//	
//	private final Object createListItem(Table t) {
//		return createListItem(t.getName(), t);
//	}
//	
//	private final void alert(String s) {
//		try {
//			Object alertDialog = parse("/ui/dialog/alert.xml");
//			setText(find(alertDialog, "alertMessage"), s);
//			add(alertDialog);
//		} catch(IOException ex) {
//			ex.printStackTrace();
//		}
//	}
//	
//	private final void alert(Throwable t) {
//		t.printStackTrace();
//		alert("Unhandled error: " + t.getClass() + ": " + t.getMessage());
//	}
//
//	public void removeDialog(Object dialog) {
//		remove(dialog);
//	}
//}
