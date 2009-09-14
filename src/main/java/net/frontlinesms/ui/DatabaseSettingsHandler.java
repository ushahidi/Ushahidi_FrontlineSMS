//package net.frontlinesms.ui;
//
//import net.frontlinesms.*;
//import net.frontlinesms.data.domain.*;
//import net.frontlinesms.properties.PropertySet;
//import net.frontlinesms.ui.i18n.InternationalisationUtils;
//
//import org.apache.log4j.Logger;
//
//import thinlet.*;
//
///**
// * Ui Handler for database settings.
// * 
// * @author Alex Anderson, Carlos Eduardo Genz
// */
//@SuppressWarnings("serial")
//public class DatabaseSettingsHandler extends FrontlineUI {
//	private static final Logger LOG = Utils.getLogger(DatabaseSettingsHandler.class);
//
//	private static final String UI_FILE_DATABASE_CONFIG_DIALOG = "/ui/database/configDialog.xml";
//	private static final String UI_FILE_DATABASE_CONFIG_PANEL = "/ui/database/configPanel.xml";
//	
//	private static final String COMPONENT_CLEAR_DATABASE = "cbClearDatabase";
//	private static final String COMPONENT_LABEL_DATABASE_PASS = "lbDatabasePass";
//	private static final String COMPONENT_LABEL_DATABASE_USER = "lbDatabaseUser";
//	private static final String COMPONENT_LABEL_DATABASE_PORT = "lbDatabasePort";
//	private static final String COMPONENT_TEXTFIELD_DATABASE_NAME = "tfDatabaseName";
//	private static final String COMPONENT_TEXTFIELD_DATABASE_PASS = "tfDatabasePass";
//	private static final String COMPONENT_TEXTFIELD_DATABASE_USER = "tfDatabaseUser";
//	private static final String COMPONENT_TEXTFIELD_DATABASE_PORT = "tfDatabasePort";
//	private static final String COMPONENT_TEXTFIELD_DATABASE_SERVER = "tfDatabaseServer";
//	private static final String COMPONENT_LABEL_DATABASE_SERVER = "lbDatabaseServer";
//	private static final String COMPONENT_COMBO_DATABASE_TYPE = "cbDatabaseType";
//
//	// FIXME should get this width and height from the XML
//	private static final int DEFAULT_HEIGHT = 335;
//	// FIXME should get this width and height from the XML
//	private static final int DEFAULT_WIDTH = 535;
//	
//	private FrontlineUI controller;
//	private Object configPanel;
//	
//	public DatabaseSettingsHandler(FrontlineUI instance) {
//		this.controller = instance;
//	}
//	
//	public DatabaseSettingsHandler() {
//		this.controller = this;
//	}
//	
//	/**
//	 * Shows the database configuration database, filling the fields with the current database information.
//	 */
//	public void showDatabaseConfigDialog() {
//		configPanel = controller.loadComponentFromFile(UI_FILE_DATABASE_CONFIG_PANEL, this);
//		
//		updateFieldValues(DbController.getType());
//
//		controller.setInteger(controller.find(configPanel, COMPONENT_COMBO_DATABASE_TYPE), Thinlet.SELECTED, DbController.getType());
//		
//		setText(COMPONENT_TEXTFIELD_DATABASE_NAME, DbController.getDatabaseName());
//		setText(COMPONENT_TEXTFIELD_DATABASE_SERVER, DbController.getDatabaseServer());
//		setText(COMPONENT_TEXTFIELD_DATABASE_PORT, DbController.getDatabasePort());
//		setText(COMPONENT_TEXTFIELD_DATABASE_USER, DbController.getDatabaseUser());
//		setText(COMPONENT_TEXTFIELD_DATABASE_PASS, DbController.getDatabasePass());
//		
//		if (this != controller) {
//			// Show this component as a dialog
//			Object databaseConfig = controller.loadComponentFromFile(UI_FILE_DATABASE_CONFIG_DIALOG, this);
//			controller.add(databaseConfig, configPanel);
//			controller.add(databaseConfig);
//		} else {
//			// Show this component in its own FrameLauncher
//			controller.add(configPanel);
//			frameLauncher = new FrameLauncher(InternationalisationUtils.getI18NString(COMMON_DATABASE_CONFIG), this, DEFAULT_WIDTH, DEFAULT_HEIGHT, getIcon(Icon.DATABASE_SETTINGS));
//		}
//	}
//	
//	/**
//	 * Sets the text of a label component in the {@link #configPanel} to the supplied value.  If the
//	 * value is <code>null</code>, the label's text will be set to empty.  If the component cannot be
//	 * @param componentName
//	 * @param value
//	 * @throws NullPointerException if the component could not be found
//	 */
//	private void setText(String componentName, String value) {
//		if(value == null) {
//			value = "";
//		}
//		controller.setText(controller.find(configPanel, componentName), value);
//	}
//	
//	/**
//	 * Gets the text content of a UI component.
//	 * @param componentName The UI component's name
//	 * @return The value of the text attribute of the component
//	 * @throws NullPointerException if the component could not be found
//	 */
//	private String getText(String componentName) {
//		return controller.getText(controller.find(configPanel, componentName));
//	}
//	
//	/**
//	 * Sets whether a compoenent in the {@link #configPanel} is enabled.
//	 * @param componentName
//	 * @param enabled
//	 * @throws NullPointerException if the component cannot be found.
//	 */
//	private void setEnabled(String componentName, boolean enabled) {
//		controller.setEnabled(controller.find(configPanel, componentName), enabled);
//	}
//	
//	/**
//	 * Tests a database connection using the settings in the config UI.  If the test is successful,
//	 * this method will return without incident.  If the test fails, a {@link Throwable} will be thrown.
//	 */
//	private void _testDatabaseConnection() throws Throwable {
//		LOG.trace("ENTER");
//		
//		int databaseType = getSelectedDatabaseType();
//		LOG.debug("Type [" + databaseType + "]");
//		
//		String databaseName = getText(COMPONENT_TEXTFIELD_DATABASE_NAME);
//		String databaseServer = getText(COMPONENT_TEXTFIELD_DATABASE_SERVER);
//		String databasePort = getText(COMPONENT_TEXTFIELD_DATABASE_PORT);
//		String databaseUser = getText(COMPONENT_TEXTFIELD_DATABASE_USER);
//		String databasePassword = getText(COMPONENT_TEXTFIELD_DATABASE_PASS);
//
//		LOG.debug("Testing connection...");
//		DbController.testConnection(databaseType, databaseName, databaseServer, databasePort, databaseUser, databasePassword);
//		return;
//	}
//	
//	/**
//	 * This method tests the database connection currently detailed in the UI fields of {@link #configPanel}.
//	 * A dialog will be displayed indicating whether the connection test was successful.
//	 */
//	public boolean testDatabaseConnection() {
//		try {
//			_testDatabaseConnection();
//			controller.alert(COMMON_CONNECTION_OK);
//			return true;
//		} catch(Throwable t) {
//			controller.alert(COMMON_CONNECTION_FAILED);
//			return false;
//		}
//	}
//	
//	/**
//	 * This method is called when the save button is pressed. It updates and connects
//	 * to the new database informed by the user.
//	 * <br> Also, this method updates the properties files with the new database details.
//	 * 
//	 * @throws DbConnectionException 
//	 */
//	public void saveDatabaseDetails() throws DbConnectionException {
//		LOG.trace("ENTER");
//		// Test the connection
//		if (testDatabaseConnection()) {
//			LOG.info("Database connection test was successful.  Saving new database settings...");
//			updatePropertiesFromFields();
//			
//			LOG.debug("Properties saved. Re-initiliasing database connection..");
//			DbController.init(controller.isSelected(controller.find(configPanel, COMPONENT_CLEAR_DATABASE)));
//			
//			controller.reloadUI(true);
//		} else {
//			LOG.info("Database connection failed.  Old settings will be retained.");
//		}
//		LOG.trace("EXIT");
//	}
//
//	/**
//	 * Updates the database properties files using the data entered into the UI fields on {@link #configPanel}.
//	 */
//	private void updatePropertiesFromFields() {
//		int selectedDatabaseType = getSelectedDatabaseType();
//		LOG.debug("Type [" + selectedDatabaseType + "]");
//		
//		PropertySet properties = getProperties(selectedDatabaseType);
//		if (selectedDatabaseType == DbController.TYPE_MYSQL) {
//			properties.setProperty(PROPERTIES_SERVER_ADDRESS, getText(COMPONENT_TEXTFIELD_DATABASE_SERVER));
//			properties.setProperty(PROPERTIES_SERVER_PORT, getText(COMPONENT_TEXTFIELD_DATABASE_PORT));
//			properties.setProperty(PROPERTIES_SERVER_USERNAME, getText(COMPONENT_TEXTFIELD_DATABASE_USER));
//			properties.setProperty(PROPERTIES_SERVER_PASSWORD, getText(COMPONENT_TEXTFIELD_DATABASE_PASS));
//		} else if(selectedDatabaseType == DbController.TYPE_DERBY_EMBEDDED) {
//			properties.setProperty(PROPERTIES_DATABASE_USERNAME, getText(COMPONENT_TEXTFIELD_DATABASE_USER));
//			properties.setProperty(PROPERTIES_DATABASE_PASSWORD, getText(COMPONENT_TEXTFIELD_DATABASE_PASS));
//		}
//		properties.setProperty(PROPERTIES_DATABASE_NAME, getText(COMPONENT_TEXTFIELD_DATABASE_NAME));
//		properties.saveToDisk();
//
//		// Update the db.properties file to indicate that the current database type should now be used 
//		PropertySet dbProperties = PropertySet.load(PROPERTIES_DATABASE);
//		dbProperties.setProperty(PROPERTIES_DATABASE_TYPE, DbController.getDatabaseTypeName(selectedDatabaseType));
//		dbProperties.saveToDisk();
//	}
//	
//	/**
//	 * Sets the UI field values for database settings for a particular database type.  These properties
//	 * are loaded from the relevant {@link PropertySet}.
//	 * @param properties
//	 * @param databaseType
//	 */
//	private void setFieldsFromProperties(int databaseType) {
//		PropertySet properties = getProperties(databaseType);
//		LOG.debug("Setting values from properties file [" + properties.getFilePath() + "]");
//		
//		setText(COMPONENT_TEXTFIELD_DATABASE_NAME, properties.getProperty(PROPERTIES_DATABASE_NAME));
//		setText(COMPONENT_TEXTFIELD_DATABASE_SERVER, properties.getProperty(PROPERTIES_SERVER_ADDRESS));
//		setText(COMPONENT_TEXTFIELD_DATABASE_PORT, properties.getProperty(PROPERTIES_SERVER_PORT));
//		if (databaseType == DbController.TYPE_MYSQL) {
//			setText(COMPONENT_TEXTFIELD_DATABASE_USER, properties.getProperty(PROPERTIES_SERVER_USERNAME));
//			setText(COMPONENT_TEXTFIELD_DATABASE_PASS, properties.getProperty(PROPERTIES_SERVER_PASSWORD));
//		} else if (databaseType == DbController.TYPE_DERBY_EMBEDDED) {
//			setText(COMPONENT_TEXTFIELD_DATABASE_USER, properties.getProperty(PROPERTIES_DATABASE_USERNAME));
//			setText(COMPONENT_TEXTFIELD_DATABASE_PASS, properties.getProperty(PROPERTIES_DATABASE_PASSWORD));
//		}
//	}
//	
//	/**
//	 * Updates the UI field values in {@link #configPanel}.  All unused fields
//	 * for a particular database type will be disabled.  All fields will be cleared
//	 * of value. 
//	 * @param databaseType The database type
//	 */
//	private void updateFieldValues(int databaseType) {
//		LOG.trace("ENTER");
//		LOG.debug("DB Type [" + databaseType + "]");
//		
//		boolean dbIsMysql = databaseType == DbController.TYPE_MYSQL;
//		setEnabled(controller.find(configPanel, COMPONENT_LABEL_DATABASE_SERVER), dbIsMysql);
//		setEnabled(controller.find(configPanel, COMPONENT_TEXTFIELD_DATABASE_SERVER), dbIsMysql);
//		setEnabled(controller.find(configPanel, COMPONENT_LABEL_DATABASE_PORT), dbIsMysql);
//		setEnabled(controller.find(configPanel, COMPONENT_TEXTFIELD_DATABASE_PORT), dbIsMysql);
//		
//		boolean dbNotSqlite = databaseType != DbController.TYPE_SQLITE;
//		setEnabled(COMPONENT_LABEL_DATABASE_USER, dbNotSqlite);
//		setEnabled(COMPONENT_TEXTFIELD_DATABASE_USER, dbNotSqlite);
//		setEnabled(COMPONENT_LABEL_DATABASE_PASS, dbNotSqlite);
//		setEnabled(COMPONENT_TEXTFIELD_DATABASE_PASS, dbNotSqlite);
//		
//		setText(COMPONENT_TEXTFIELD_DATABASE_NAME, null);
//		setText(COMPONENT_TEXTFIELD_DATABASE_SERVER, null);
//		setText(COMPONENT_TEXTFIELD_DATABASE_PORT, null);
//		setText(COMPONENT_TEXTFIELD_DATABASE_USER, null);
//		setText(COMPONENT_TEXTFIELD_DATABASE_PASS, null);
//
//		controller.setSelected(controller.find(configPanel, COMPONENT_CLEAR_DATABASE), false);
//		LOG.trace("EXIT");
//	}
//	
//	/**
//	 * Loads the database properties set in the application config.
//	 * @param databaseType
//	 * @return
//	 */
//	private PropertySet getProperties(int databaseType) {
//		if (databaseType == DbController.TYPE_SQLITE) {
//			return PropertySet.load(PROPERTIES_SQLITE);
//		}
//		if (databaseType == DbController.TYPE_MYSQL) {
//			return PropertySet.load(PROPERTIES_MYSQL);
//		}
//		if (databaseType == DbController.TYPE_DERBY_EMBEDDED) {
//			return PropertySet.load(PROPERTIES_DERBY);
//		}
//		throw new RuntimeException("Unrecognized database type: " + databaseType);
//	}
//	
//	/**
//	 * Gets the database type selected in the combobox on {@link #configPanel}.
//	 * @return {@link DbController#TYPE_MYSQL}, {@link DbController#TYPE_SQLITE} or {@link DbController#TYPE_DERBY_EMBEDDED} 
//	 */
//	private int getSelectedDatabaseType() {
//		return controller.getSelectedIndex(controller.find(configPanel, COMPONENT_COMBO_DATABASE_TYPE));
//	}
//
//	/**
//	 * Method called when the database type combobox changes its selection.  This will update
//	 * fields to reflect settings for the selected database type.
//	 */
//	public void databaseTypeChanged() {
//		int databaseType = getSelectedDatabaseType();
//		updateFieldValues(databaseType);
//		setFieldsFromProperties(databaseType);
//	}
//}