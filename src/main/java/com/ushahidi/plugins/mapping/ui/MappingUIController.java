package com.ushahidi.plugins.mapping.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.SynchronizationManager;
import com.ushahidi.plugins.mapping.data.domain.*;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.maps.Map;
import com.ushahidi.plugins.mapping.maps.TileSaver;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.Utils;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.Message;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.DateSelecter;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;
import thinlet.Thinlet;

@SuppressWarnings("serial")
public class MappingUIController extends ExtendedThinlet implements ThinletUiEventHandler, MapListener {

	private static final String COMPONENT_MESSAGE_TABLE = "messageTable";
	private static final String COMPONENT_INCIDENT_DIALOG = "incident_Dialog";
	private static final String COMPONENT_LOCATIONS_COMBO = "cboLocations";
	private static final String COMPONENT_ALL_CB = "cbMfAll";
	private static final String COMPONENT_KEYWORDS_CM = "cbMfKeywords";
	private static final String COMPONENT_DATE_CB = "cbMfDate";
	private static final String COMPONENT_LOCATION_BUTTON = "btnSelectPoint";
	private static final String COMPONENT_LOCATION_LABEL = "lblLoction";
	private static final String COMPONENT_CATEGORIES_COMBO = "cboCategories";
	private static final String COMPONENT_ADDITIONAL_INFO_TEXTFIELD = "txtAdditionalInfo";
	private static final String COMPONENT_SAVE_BUTTON = "btSave";
	private static final String COMPONENT_MAP_BEAN = "mapBean";
	private static final String COMPONENT_KEYWORDS_TABLE = "tblKeywords";
	private static final String COMPONENT_MF_DATE_PANEL = "pblMfDate";
	private static final String UI_FILE_INCIDENT_DIALOG = "/ui/plugins/mapping/incidentDialog.xml";
	private static final String UI_SETUP_DIALOG = "/ui/plugins/mapping/setupDialog.xml";
	private static final String UI_SAVE_DIALOG="/ui/plugins/mapping/mapSaveDialog.xml";
	private static final String UI_REPORTS_DIALOG = "/ui/plugins/mapping/reportsDialog.xml";
	private static final String UI_REPORT_DETAILS_DIALOG = "/ui/plugins/mapping/reportDetailsDialog.xml";
	private static final String UI_SYNCHRONIZATION_DIALOG = "/ui/plugins/mapping/synchronizationDialog.xml";
	private static final String SETUP_DLG_COMPONENT_SOURCE_TABLE = "locationSources_Table";
	private static final String SETUP_DLG_COMPONENT_FLD_SOURCE_NAME = "txtSourceName";
	private static final String SETUP_DLG_COMPONENT_FLD_SOURCE	= "txtLocationSource";
	private static final String SETUP_DLG_COMPONENT_FLD_LONGITUDE = "txtLongitude";
	private static final String SETUP_DLG_COMPONENT_FLD_LATITUDE = "txtLatitude";
	private static final String SETUP_DLG_COMPOONENT_CHK_STATUS = "chkSourceStatus";
	private static final String TAB_MAPPING = ":mapping";
	private static final String COMPONENT_COORDINATE_LABEL = "lblCoordinates";
	private static final String COMPONENT_PANEL_SELECTED_LOCATION = "pnl_SelectedLocation";
	private static final String COMPONENT_LBL_SELECTED_LATITUDE = "lbl_Latitude";
	private static final String COMPONENT_LBL_SELECTED_LONGITUDE = "lbl_Longitude";
	private static final String COMPONENT_PANEL_INCIDENT_INFO = "pnl_IncidentInfo";
	private static final String COMPONENT_TBL_INCIDENT_REPORTS = "tbl_IncidentReports";
	private static final String COMPONENT_REPORT_TITLE_FIELD = "txtReportTitle";
	private static final String COMPONENT_REPORT_DESC_FIELD = "txtReportDescription";
	private static final String COMPONENT_REPORT_DATE_FIELD = "txtReportDate";
	private static final String COMPONENT_REPORT_LOCATION_NAME_FIELD = "txtReportLocation";
	private static final String COMPONENT_REPORT_LOCATION_COORDS_LABEL = "lbl_ReportLocationCoords";
	private static final String COMPONENT_LOCATION_NAME_FIELD = "txtLocationName";
	private static final String COMPONENT_LBL_SYNC_CURRENT_TASK_NO  = "lbl_currentTaskNo";
	private static final String COMPONENT_LBL_SYNC_TOTAL_TASK_COUNT = "lbl_totalTaskCount";
	private static final String COMPONENT_SYNC_PROGRESS_BAR = "pbar_Synchronization";
	private static final String COMPONENT_SLD_ZOOM_CONTROLLER = "sld_ZoomController";
	
	private FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
		
	private boolean syncStarted = false;
	private MapBean mapBean;
	private SynchronizationManager syncManager;
	
	/** Thinlet component containing the mapping tab */
	private Object tabComponent;
	
	public static Logger LOG = Utils.getLogger(MappingUIController.class);	
	
	public MappingUIController(MappingPluginController pluginController, FrontlineSMS frontlineController, 
			UiGeneratorController uiController) {
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		locationDao = pluginController.getLocationDao();
		categoryDao = pluginController.getCategoryDao();
		incidentDao = pluginController.getIncidentDao();
		mappingSetupDao = pluginController.getMappingSetupDao();
		
		//checkPluginConfiguration();
	}
	
	/**
	 * Initializes the UI
	 * This method must be called by {@link MappingPluginController}
	 */
	public void initUIController(){
		
		//update the keyword list
		updateKeywordList();
		
		updateMappingTab();
		
		//initialize the map bean
		initializeMapBean();
	}
	
	private Object getTab() {
		return ui.find(TAB_MAPPING);
	}
	
	/** Initializes the location bounds for the map bean */
	private void initializeMapBean(){
		if(mappingSetupDao.getDefaultSetup() != null){
			double latitude = mappingSetupDao.getDefaultSetup().getLatitude();
			double longitude = mappingSetupDao.getDefaultSetup().getLongitude();
			mapBean = (MapBean)get(ui.find(this.tabComponent, COMPONENT_MAP_BEAN), BEAN);
			
			//Check if offline mode for the default setup is enabled
			if(mappingSetupDao.getDefaultSetup().isOffline()){
				MappingSetup defaultSetup = mappingSetupDao.getDefaultSetup();
				String fileName = defaultSetup.getOfflineMapFile();
				File f = new File(fileName);
				if(f.exists())
					mapBean.setOfflineMapFile(fileName);
				else{
					defaultSetup.setOffline(false);
					defaultSetup.setOfflineMapFile(null);
					try{
						mappingSetupDao.updateMappingSetup(defaultSetup);
					}catch(DuplicateKeyException de){
						LOG.debug(de);
						ui.alert("Unable to update the map setup");
					}					
				}
			}
			
			mapBean.setLocation(longitude, latitude);			
			mapBean.setIncidents(incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup()));
			mapBean.setMappingUIController(this);
		}else{
			//The mapping plugin has not been configured; therefore disable the zoom controller
			setEnabled(ui.find(this.tabComponent, COMPONENT_SLD_ZOOM_CONTROLLER), false);
		}
	}
	
	/**
	 * Toggle the event filter
	 * 
	 * @param pnlSearchParams
	 */
	public void messageFilterChanged(Object pnlSearchParams) {
		/* The rowspan property of the parent object @pnlSearchParams
		 * needs to be adjusted as the search options are toggled. 
		 */
		if(isSelected(find(getTab(), COMPONENT_ALL_CB))) {
			setVisible(ui.find(getTab(), COMPONENT_MF_DATE_PANEL), false);
			setVisible(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), false);
			ui.setInteger(pnlSearchParams, "rowspan", 1);
			if(incidentDao.getCount() > 0)
				mapBean.setIncidents(incidentDao.getAllIncidents());
			
		} else if(isSelected(find(getTab(), COMPONENT_KEYWORDS_CM))) {
			setVisible(ui.find(getTab(), COMPONENT_MF_DATE_PANEL), false);
			setVisible(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), true);
			ui.setInteger(pnlSearchParams, "rowspan", 4);
			
		} else if(isSelected(find(getTab(), COMPONENT_DATE_CB))) {
			setVisible(ui.find(getTab(), COMPONENT_MF_DATE_PANEL), true);
			setVisible(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), false);
			ui.setInteger(pnlSearchParams, "rowspan", 2);
		}		
		
	}	
	
	public void messageFilterDateChanged() {		
	}	
	
	
	/**
	 * Load all received messages
	 */
	public void updateMappingTab() {
		Object messageTableComponent = ui.find(this.tabComponent, COMPONENT_MESSAGE_TABLE);
		removeAll(messageTableComponent);
		Integer[] status = {Message.STATUS_RECEIVED};
		for (Message m : frontlineController.getMessageDao().getMessages(Message.TYPE_RECEIVED, status)) {
			ui.add(messageTableComponent, getRow(m));
		}	
	}
	
	/**
	 * Displays the date selector
	 * 
	 * @param textField
	 */
	public void showDateSelector(Object textField) {
		LOG.trace("ENTER");
		try {
			new DateSelecter(ui, textField).showSelecter();
		} catch (IOException e) {
			LOG.error("Error parsing file for dateSelecter", e);
			LOG.trace("EXIT");
			throw new RuntimeException(e);
		}
		LOG.trace("EXIT");
	}	
	
	/**
	 * Displays the incident creation dialog
	 * This dialog allows the user to create an Ushahidi incident report from a text message
	 *  
	 * @param item Selected {@link Message}; the one an incident report shall be created from
	 */
	public void showIncidentDialog(Object item) {
		
		Message message = (Message) getAttachedObject(item);						
		if(message != null) {
			Object dialog = ui.loadComponentFromFile(UI_FILE_INCIDENT_DIALOG, this);
			ui.setAttachedObject(dialog, message);
			
			//Populate the locations combo
			Object cbLocation = find(dialog, COMPONENT_LOCATIONS_COMBO);
			for(Location  l: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {				
				Object choice = createComboboxChoice(l.getName(), l);
				ui.add(cbLocation, choice);
			}
			
			Object cbCategory = find(dialog, COMPONENT_CATEGORIES_COMBO);
			for(Category c: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
				Object choice = createComboboxChoice(c.getTitle(), c);
				ui.add(cbCategory, choice);
			}
				
			//Load Message Details			
			setText(find(dialog, "txtIncidentDate"), InternationalisationUtils.getDatetimeFormat().format(message.getDate()));
			setText(find(dialog, "txtIncidentSender"), getSenderDisplayValue(message));
			setText(find(dialog, "txtMessage"), message.getTextContent());
			
			ui.add(dialog);			
		}		
	}
	
	public void startPointSelection(Object dialog) {
		MapBean mapBean = (MapBean) get(ui.find(getTab(), COMPONENT_MAP_BEAN), BEAN);
		mapBean.addMapListener(this);		
		setBoolean(dialog, Thinlet.MODAL, false);
		setVisible(dialog, false);
		
		//Force Thinlet to hide the dialog now
		ui.repaint();
	}
	
	
	public void incidentFilterDateChanged() {		
	}
	
	/**
	 * Gets the display name for the sender of of the text message
	 * @param message
	 * @return
	 */
	private String getSenderDisplayValue(Message message) {
		Contact sender = frontlineController.getContactDao().getFromMsisdn(message.getSenderMsisdn());
		String senderDisplayName = sender != null ? sender.getDisplayName() + "(" + message.getSenderMsisdn() + ")" : message.getSenderMsisdn();
		return senderDisplayName;
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
		MapBean mapBean = (MapBean) get(ui.find(COMPONENT_MAP_BEAN), BEAN);
		//mapBean.removeMapListener();
	}

	/**
	 * Fired by {@link MapListener} when a point is selected on the map; the incident creation
	 * dialog is displayed with the coordinates of the selected location
	 */
	public void pointSelected(double lat, double lon) {
		LOG.debug(lat);
		Object dialog = ui.find(COMPONENT_INCIDENT_DIALOG);		
		
		//Show the selected point and the text field for the name to be input
		setBoolean(ui.find(dialog, COMPONENT_LOCATION_NAME_FIELD), Thinlet.ENABLED , true);
		setText(ui.find(dialog, COMPONENT_LBL_SELECTED_LATITUDE), Double.toString(lat));
		setText(ui.find(dialog, COMPONENT_LBL_SELECTED_LONGITUDE), Double.toString(lon));

		setBoolean(ui.find(dialog, COMPONENT_LOCATIONS_COMBO), Thinlet.ENABLED, false);		

		setVisible(dialog, true);
		setBoolean(dialog, Thinlet.MODAL, true);
		
		ui.repaint();		
	}
	
	public void incidentDialogEdited(Object dialog) {
		//Category && Map point must be selected for save button to be enabled
		Object button = ui.find(dialog, COMPONENT_SAVE_BUTTON);
		if(ui.getText(ui.find(dialog,COMPONENT_LOCATION_LABEL)) != null
				&& ui.getSelectedItem(ui.find(dialog,COMPONENT_LOCATIONS_COMBO)) != null
				&& !ui.getBoolean(button, ENABLED)) {			
			setEnabled(button, true);
			ui.repaint();
		}
	}
	
	/**
	 * Saves an {@link Incident} created from a text message
	 * 
	 * @throws DuplicateKeyException
	 */
	public void saveIncident() throws DuplicateKeyException {
		Object dialog = ui.find(COMPONENT_INCIDENT_DIALOG);
		
		Message message = (Message)getAttachedObject(dialog);
		if(message != null){
			Incident incident = new Incident();
			String dateStr = getText(ui.find(dialog, "txtIncidentDate"));
			String title = message.getTextContent();
			String additionalInfo = getText(ui.find(dialog, COMPONENT_ADDITIONAL_INFO_TEXTFIELD));			
			
			Location location = null;
			
			//Get form values
			Object cboLocations = ui.find(dialog, COMPONENT_LOCATIONS_COMBO);
			if(getBoolean(cboLocations, ENABLED)){
				location = (Location)getAttachedObject(getSelectedItem(cboLocations));
				incident.setLocation(location);
			}else{
				double lat = Double.parseDouble(getText(ui.find(dialog, COMPONENT_LBL_SELECTED_LATITUDE)));
				double lon = Double.parseDouble(getText(ui.find(dialog, COMPONENT_LBL_SELECTED_LONGITUDE)));
				
				String name = getText(ui.find(dialog, COMPONENT_LOCATION_NAME_FIELD));
				location = new Location(lat, lon);
				location.setName(name);
				location.setMappingSetup(mappingSetupDao.getDefaultSetup());
				
				try{
					locationDao.saveLocation(location);					
				}catch(DuplicateKeyException de){
					LOG.debug(de);
					//de.printStackTrace();
					ui.alert("The location ["+name+"] could not be saved.");
					return;
				}
				
				//Reload the list of keywords
				updateKeywordList();
				
			}			
			
			incident.setTitle(title);
			incident.setDescription(additionalInfo);
			incident.setLocation(location);
			incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
			
			Object cboCategories = ui.find(dialog, COMPONENT_CATEGORIES_COMBO);
			Category category = (Category)getAttachedObject(getSelectedItem(cboCategories));
			incident.setCategory(category);
			incident.setMarked(true);
			
			try{
				incident.setIncidentDate(InternationalisationUtils.getDatetimeFormat().parse(dateStr));
			}catch(ParseException pe){
				LOG.debug("Invalid date string", pe);
				ui.alert("The incident date [" + dateStr + "] is invalid");
				return;
			}
						
			try{
				incidentDao.saveIncident(incident);
			}catch(DuplicateKeyException de){
				LOG.debug(de);
				ui.alert("ERROR: Unable to create an incident from the text message");
				return;
			}
		}
		
		//Re-plot the incidents on the map
		mapBean.setIncidents(incidentDao.getAllIncidents());
		
		removeDialog(dialog);
		
	}
	
	/**
	 * Displays the mapping setup dialog
	 */
	public void showSetupDialog(){
		Object setupDialog = ui.loadComponentFromFile(UI_SETUP_DIALOG, this);
		ui.add(setupDialog);
		//Load the setup items
		if(mappingSetupDao.getCount() > 0){
			Object table = ui.find(setupDialog, SETUP_DLG_COMPONENT_SOURCE_TABLE);
			for(MappingSetup setup: mappingSetupDao.getAllSetupItems())
				add(table, getRow(setup));
		}
	}
	
	/**
	 * Adds a new set of mapping configurations to the database
	 * 
	 * @param dialog
	 */
	public void addSource(Object dialog){
		String sourceName = getText(find(dialog, SETUP_DLG_COMPONENT_FLD_SOURCE_NAME));
		String sourceURL = getText(find(dialog, SETUP_DLG_COMPONENT_FLD_SOURCE));
		String lat = getText(find(dialog, SETUP_DLG_COMPONENT_FLD_LATITUDE));
		String lng = getText(find(dialog, SETUP_DLG_COMPONENT_FLD_LONGITUDE));
		
		boolean active = getBoolean(find(dialog, SETUP_DLG_COMPOONENT_CHK_STATUS), Thinlet.SELECTED);
		
		/** Validation checks */
		//source url check
		if(sourceURL == null || sourceURL.length() == 0){
			ui.alert("Please specifiy a valid source url");
			LOG.debug("Invalid or empty source url");
			return;
		}
		
		//latitude check
		if(lat == null || lat.length() ==0){
			ui.alert("Please specifiy a valid latitude");
			LOG.debug("Invalid or empty latitude value");
			return;
		}
		
		//longitude check
		if(lng == null || lng.length() == 0){
			ui.alert("Please specify a valid longitude");
			LOG.debug("Invalid or empty longitude value");
			return;
		}
		
		
		MappingSetup setup = null;
		Object item = getAttachedObject(dialog);
		
		if(item instanceof MappingSetup){
			setup = (MappingSetup)item;
			setAttachedObject(dialog,  null);
		}else{
			setup = new MappingSetup();
		}
		
		//Get the current default setup
		MappingSetup currentDefault = (mappingSetupDao.getDefaultSetup() != null)? mappingSetupDao.getDefaultSetup():null;
		
		//Set the properties for the mapping setup
		setup.setName(sourceName);
		setup.setSourceURL(sourceURL);
		setup.setLatitude(Double.parseDouble(lat));
		setup.setLongitude(Double.parseDouble(lng));		
		
		//Check if the current item is the only one and if its being unset as the default
		if(currentDefault != null && mappingSetupDao.getCount() == 1 && !active){
			LOG.debug("There must be a default configurartion for Mapping");
			ui.alert("There is only one configuration for Mapping ["+setup.getSourceURL()+"] " +
					"and it must be set as the default");
			
			return;
		}
		
		//Check for attempts to save without specifying a default mapping configuration
		if( (currentDefault != null && mappingSetupDao.getCount() > 1 && 
				setup.getId() == currentDefault.getId() && !active) || (currentDefault == null && !active)){
			LOG.debug("Default mapping setup not specified");
			ui.alert("There must be a default configuration for Mapping to work");
			
			return;
		}
		
		//Set the active flag for the mapping setup 
		setup.setDefaultSetup(active);
		
		try{
			Object table = ui.find(dialog, SETUP_DLG_COMPONENT_SOURCE_TABLE);

			//Update the current default setup if it is different from the new one
			if(currentDefault != null && Long.toString(setup.getId()) != null)
				if (setup.getId() != currentDefault.getId() && setup.isDefaultSetup()){
					currentDefault.setDefaultSetup(false);
					mappingSetupDao.updateMappingSetup(currentDefault);
					LOG.debug("Changed default mapping setup to " + setup.getName());
				}
			
			if(item == null){
				mappingSetupDao.saveMappingSetup(setup);				
				add(table, getRow(setup));
			}else{
				mappingSetupDao.updateMappingSetup(setup);
			}
			
			removeAll(table);
			for(MappingSetup s: mappingSetupDao.getAllSetupItems())
				add(table, getRow(s));
			
		}catch(DuplicateKeyException e){
			LOG.debug("Mapping setup parameter already exists", e);
			ui.alert("Mapping setup parameter already exists");
			LOG.trace("EXIT");
			return;
		}
		
		LOG.debug("Mapping setup parameter for [" + setup.getSourceURL() +"] created!");
				
		ui.repaint();
		clearSourceFields(dialog);
	}
	
	/**
	 * Gets a table row with {@link MappingSetup} attached
	 * @param setup
	 * @return
	 */
	private Object getRow(MappingSetup setup){
		Object row = createTableRow(setup);
		createTableCell(row, setup.getName());
		createTableCell(row, setup.getSourceURL());
		createTableCell(row, Double.toString(setup.getLatitude()));
		createTableCell(row, Double.toString(setup.getLongitude()));
		String activeStr = (setup.isDefaultSetup())?"Y":"N";
		createTableCell(row, activeStr);
		
		return row;
	}
	
	/**
	 * Gets a row with {@link Message} attached
	 * @param message
	 * @return
	 */
	private Object getRow(Message message){		
		Object row = createTableRow(message);
		createTableCell(row, InternationalisationUtils.getDatetimeFormat().format(message.getDate()));
		createTableCell(row, getSenderDisplayValue(message));
		createTableCell(row, message.getTextContent());
		
		return row;
	}
	
	/**
	 * Clears the input fields from the mapping setup dialog 
	 * @param dialog
	 */
	public void clearSourceFields(Object dialog){
		setText(find(dialog, SETUP_DLG_COMPONENT_FLD_SOURCE_NAME), "");
		setText(find(dialog, SETUP_DLG_COMPONENT_FLD_SOURCE),"");
		setText(find(dialog, SETUP_DLG_COMPONENT_FLD_LATITUDE),"");
		setText(find(dialog, SETUP_DLG_COMPONENT_FLD_LONGITUDE),"");
		setBoolean(find(dialog, SETUP_DLG_COMPOONENT_CHK_STATUS), Thinlet.SELECTED, false);
		ui.repaint();
	}
	
	/**
	 * Performs synchronization with an Ushahidi instance
	 */
	public void beginSynchronization(){
		//Check if the mapping plugin has been configured to synchronize to an Ushahidi instance
		if(mappingSetupDao.getCount() == 0 || mappingSetupDao.getDefaultSetup() == null){
			check_PluginConfiguration();
		}else{
			syncManager = new SynchronizationManager(this);		
			//Run a full sync
			LOG.debug("Starting full synchronization...");
			syncManager.performFullSynchronization();
		}
		
	}
	
	/**
	 * Updates the keyword list with the new items 
	 */
	private void updateKeywordList(){
		//Clear the contents of the keyword list
		removeAll(ui.find(this.tabComponent, COMPONENT_KEYWORDS_TABLE));
		
		/*
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			Object row = createTableRow(category);
			createTableCell(row,"");
			createTableCell(row, category.getTitle());			
			add(ui.find(COMPONENT_KEYWORDS_TABLE), row);
		}
		*/
		
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())){
			Object row = createTableRow(location);
			createTableCell(row, "");
			createTableCell(row, location.getName());
			add(ui.find(this.tabComponent, COMPONENT_KEYWORDS_TABLE), row);
		}
		
		ui.repaint(ui.find(this.tabComponent, COMPONENT_KEYWORDS_TABLE));
	}
	
	/**
	 * Adds a new category. Any exceptions are trapped and logged
	 * 
	 * @param category Category to be added
	 */
	public synchronized void addCategory(Category category){
		if(categoryDao.findCategory(category.getFrontendId(), mappingSetupDao.getDefaultSetup()) == null){
			category.setMappingSetup(mappingSetupDao.getDefaultSetup());
			try{
				categoryDao.saveCategory(category);			
			}catch(DuplicateKeyException e){
				LOG.debug("Category already exists", e);
				LOG.trace("EXIT");
				syncManager.terminateManagerThread();
				return;
			}
					
			//Add category to the keyword listing
			Object row = ui.createTableRow(category);
			createTableCell(row, "");
			createTableCell(row, category.getTitle());
			ui.add(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), row);
			ui.repaint();
			
			LOG.debug("Category [" + category.getTitle() + "] added!");
		}
	}
	
	/**
	 * Adds a new location. The location is added both to the in memory database
	 * and to the DB used by hibernate
	 * 
	 * @param location Location to be added
	 */
	public synchronized void addLocation(Location location){
		if(locationDao.findLocation(location.getFrontendId(), mappingSetupDao.getDefaultSetup()) == null){
			location.setMappingSetup(mappingSetupDao.getDefaultSetup());
			try{
				locationDao.saveLocation(location);
			}catch(DuplicateKeyException e){			
				LOG.debug("Location already exists", e);
				LOG.trace("EXIT");
				syncManager.terminateManagerThread();
				return;
			}
			
			//Add location to the keyword listing
			Object row = ui.createTableRow(location);
			createTableCell(row, "");
			createTableCell(row, location.getName());
			ui.add(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), row);
			ui.repaint();
	
			LOG.debug("Location [" + location.getName() + "] created!");
		}
	}
	
	/**
	 * Adds a new incident
	 * @param incident Incident to be added
	 */
	public synchronized void addIncident(Incident incident){
		//Find the location
		if(incidentDao.findIncident(incident.getFrontendId(), mappingSetupDao.getDefaultSetup()) == null){
			long frontendId = incident.getLocation().getFrontendId();
			Location location = locationDao.findLocation(frontendId, mappingSetupDao.getDefaultSetup());
			
			if(location == null){
				addLocation(incident.getLocation());
				location = locationDao.findLocation(frontendId, mappingSetupDao.getDefaultSetup());
			}
			
			incident.setLocation(location);
			
			incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
			try {
				incidentDao.saveIncident(incident);
			} catch (DuplicateKeyException e) {
				LOG.debug("Incident already exists", e);
				LOG.trace("EXIT");
				syncManager.terminateManagerThread();
				return;
			}
			LOG.debug("Incident [" + incident.getTitle() + "] created!");
		}
	}
	
	/**
	 * Gets the default URL to be used for the synchronisation. The default URL is specified in the 
	 * default mapping setup
	 * 
	 * @return
	 */
	public String getDefaultSynchronizationURL(){
		return (mappingSetupDao.getDefaultSetup() == null)? null : 
			mappingSetupDao.getDefaultSetup().getSourceURL();
	}
	
	/**
	 * Checks whether the mapping plugin has been configured for purposes of synchronization 
	 * with an Ushahidi instance
	 */
	public void check_PluginConfiguration(){
		if(mappingSetupDao.getCount() == 0){
			LOG.debug("Mapping plugin has not been configured");
			showSetupDialog();
			ui.alert("The mapping plugin has not been configured!");
			LOG.trace("EXIT");
			return;
		}else if(mappingSetupDao.getCount() > 0 && mappingSetupDao.getDefaultSetup() == null){
			LOG.debug("Default mapping setup not set");
			showSetupDialog();
			ui.alert("Please select the default mapping setup");
			LOG.trace("EXIT");
			return;
		}
	}
	
	/**
	 * Gets the list of incidents to be posted/pushed to the Ushahidi instance
	 * @return
	 */
	public List<Incident> getPendingIncidents(){
		return incidentDao.getUnMarkedIncidents(mappingSetupDao.getDefaultSetup());
	}
	
	/**
	 * Gets all the frontend ids of the locations
	 * @return
	 */
	public List<String> getLocationIds(){
		ArrayList<String> items = new ArrayList<String>();
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup()))
			items.add(Long.toString(location.getFrontendId()));
		return items;
	}

	/**
	 * Get all the frontend ids of the categories
	 * @return
	 */
	public List<String> getCategoryNames(){
		ArrayList<String> items = new ArrayList<String>();
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup()))
			items.add(category.getTitle().toLowerCase());
		return items;
	}
	
	/**
	 * Show the map save dialog
	 */
	public void saveMap() {
		Object saveDialog = ui.loadComponentFromFile(UI_SAVE_DIALOG, this);
		ui.add(saveDialog);
	}
	
	/**
	 * Saves the map tiles to a file on the disk
	 * 
	 * @param dialog Map Save dialog
	 * @param mapName Name of the file to save the map to
	 */
	public void doMapSave(Object dialog, String mapName) {
		MapBean mapBean = (MapBean) get(ui.find(COMPONENT_MAP_BEAN), BEAN);
		Map map = mapBean.getMap();
		//Create maps dir in config dir if it doesn't exist
		File f = new File(ResourceUtils.getConfigDirectoryPath() + "/maps");
		if(!f.exists()) {
			if(!f.mkdir()) {
				ui.alert("Unable to create maps dir!");
				return;
			}
		}
		String filename = ResourceUtils.getConfigDirectoryPath() + "maps/" + mapName + ".zip";
		TileSaver ts = new TileSaver(map, map.topLeftCoord(), map.btmRightCoord(), filename, true);
		ts.startSave();
		
		MappingSetup mappingSetup = mappingSetupDao.getDefaultSetup();
		mappingSetup.setOfflineMapFile(filename);
		mappingSetup.setOffline(true);
		
		try{
			mappingSetupDao.updateMappingSetup(mappingSetup);
		}catch(DuplicateKeyException de){
			LOG.debug("Could not update map setup", de);
			ui.alert("Update of map setup failed");
		}
	}
	
	/**
	 * Updates the screen with the current geographical coordinates based on the current
	 * position of the mouse. A mouse motion listener is used to track mouse movement on
	 * the map
	 * 
	 * @param lat Latitude location
	 * @param lon Longitude location
	 */
	public void updateCoordinateLabel(double lat, double lon){
		String latStr = Double.toString(lat).substring(0,8);
		String lonStr = Double.toString(lon).substring(0,8);
		Object label = ui.find(getTab(), COMPONENT_COORDINATE_LABEL);
		setText(label, lonStr+", "+latStr);
		ui.repaint(label);
	}
	
	/**
	 * Updates the UI when the keyword selection changes
	 * @param tblKeywords
	 */
	public void mappingTab_keywordSelectionChanged(Object tblKeywords){
		Object item = getAttachedObject(getSelectedItem(tblKeywords));
		if(item instanceof Category){
			
		}else if(item instanceof Location){
			Location location = (Location)item;
			location.setMappingSetup(mappingSetupDao.getDefaultSetup());
			List<Incident> incidents = incidentDao.getIncidentsByLocation(location);
			mapBean.setIncidents(incidents);
		}else 
			throw new RuntimeException();
	}
	
	/**
	 * Loads the details of the mapping setup in the fields for editing
	 * 
	 * @param setupDialog Setup dialog for the map
	 * @param tblLocationSource Table containing the list of the mapping sources
	 */
	public void edit_MappingSource(Object setupDialog, Object tblLocationSource){
		Object item = getAttachedObject(getSelectedItem(tblLocationSource));
		if(item instanceof MappingSetup){
			MappingSetup setup = (MappingSetup)item;
			
			//Attach the setup item to the dialog
			setAttachedObject(setupDialog, setup);
			
			setText(ui.find(setupDialog, SETUP_DLG_COMPONENT_FLD_SOURCE_NAME), setup.getName());
			setText(ui.find(setupDialog, SETUP_DLG_COMPONENT_FLD_SOURCE), setup.getSourceURL());
			setText(ui.find(setupDialog, SETUP_DLG_COMPONENT_FLD_LATITUDE), Double.toString(setup.getLatitude()));
			setText(ui.find(setupDialog, SETUP_DLG_COMPONENT_FLD_LONGITUDE), Double.toString(setup.getLongitude()));
			setSelected(ui.find(setupDialog, SETUP_DLG_COMPOONENT_CHK_STATUS), setup.isDefaultSetup());
			
			ui.repaint();
		}
	}
	
	/**
	 * Shows the list of reported incidents
	 */
	public void showReports(){
		Object dialog = ui.loadComponentFromFile(UI_REPORTS_DIALOG, this);
		ui.add(dialog);
		
		if(incidentDao.getCount() > 0){
			Object table = ui.find(dialog, COMPONENT_TBL_INCIDENT_REPORTS);
			for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
				add(table, getRow(incident));
			}
		}
	}
	
	/**
	 * Returns a row with an incident attached to it
	 * 
	 * @param incident see {@link #Incident}
	 * 
	 * @return
	 */
	public Object getRow(Incident incident){
		Object row = createTableRow(incident);
		createTableCell(row, incident.getTitle());
		createTableCell(row, incident.getLocation().getName());
		createTableCell(row, InternationalisationUtils.getDateFormat().format(incident.getIncidentDate()));
		return row;
	}
	
	/**
	 * Show the details of the selected report item
	 * @param item
	 */
	public void showReportDetails(Object item){
		Incident incident = (Incident)getAttachedObject(item);
		
		if(incident != null){
			Object dialog = ui.loadComponentFromFile(UI_REPORT_DETAILS_DIALOG, this);
			setAttachedObject(dialog, incident);
			ui.add(dialog);
			
			//Set the details for the incident
			setText(ui.find(dialog, COMPONENT_REPORT_TITLE_FIELD), incident.getTitle());
			setText(ui.find(dialog, COMPONENT_REPORT_DESC_FIELD), incident.getDescription());
			setText(ui.find(dialog, COMPONENT_REPORT_DATE_FIELD), 
					InternationalisationUtils.getDatetimeFormat().format(incident.getIncidentDate()));
			setText(ui.find(dialog, COMPONENT_REPORT_LOCATION_NAME_FIELD), incident.getLocation().getName());
			setText(ui.find(dialog, COMPONENT_REPORT_LOCATION_COORDS_LABEL), 
					Double.toString(incident.getLocation().getLongitude()) +"," +
					Double.toString(incident.getLocation().getLatitude()));
			
			ui.repaint();
		}
	}
	
	/**
	 * Save the details for the report after editing
	 * @param dialog
	 */
	public void saveReport(Object dialog){
		Incident incident = (Incident)getAttachedObject(dialog);
		if(incident != null){
			incident.setTitle(getText(ui.find(dialog, COMPONENT_REPORT_TITLE_FIELD)));
			incident.setDescription(getText(ui.find(dialog, COMPONENT_REPORT_DESC_FIELD)));
			//Only locally created incidents should be marked for posting to the frontend
			//TODO: Accomodate editing and posting of incidents fetched from the frontend; API feature request
			if(Long.toString(incident.getFrontendId()).equals(null))
				incident.setMarked(true);
			
			//
			try{
				incidentDao.updateIncident(incident);
			}catch(DuplicateKeyException e){
				LOG.debug("Unable to update incident", e);
			}
		}
	}
	
	/**
	 * Changes the zoom level of the map
	 * 
	 * @param zoomController The Zoom UI control
	 */
	public void zoomMap(Object zoomController){
		int zoomVal = getInteger(zoomController, ExtendedThinlet.VALUE);
		mapBean.setZoomValue(zoomVal);
	}

	/**
	 * Adds an incoming message (received via the connected mobile phone) to the list of
	 * messages
	 * 
	 * @param message The received message
	 */
	public void handleIncomingMessage(Message message) {
		Object messageTableComponent = ui.find(this.tabComponent, COMPONENT_MESSAGE_TABLE);
		ui.add(messageTableComponent, getRow(message));
		ui.repaint();
	}
	
	public void setTabComponent(Object tabComponent){
		this.tabComponent = tabComponent;
	}
	
	/**
	 * Update the posted incident by turning off the "marked" flag
	 * 
	 * @param incident
	 */
	public synchronized void updatePostedIncident(Incident incident){
		incident.setMarked(false);
		try{
			incidentDao.updateIncident(incident);
		}catch(DuplicateKeyException dk){
			LOG.debug("Unable to update incident", dk);
		}
	}
	
	/**
	 * Displays the synchronization dialog for the duration of the sync
	 * 
	 * @return
	 */
	public Object showSynchronizationDialog(){
		Object dialog = ui.loadComponentFromFile(UI_SYNCHRONIZATION_DIALOG, this);
		setText(ui.find(dialog, COMPONENT_LBL_SYNC_CURRENT_TASK_NO), "1");
		ui.add(dialog);
		return dialog;
	}
	
	/**
	 * Updates the label in synchronization dialog with the total number of tasks to be performed
	 * 
	 * @param dialog
	 * @param count
	 */
	public synchronized void setSynchronizationTaskCount(Object dialog, int count){
		setText(ui.find(dialog, COMPONENT_LBL_SYNC_TOTAL_TASK_COUNT), Integer.toString(count));
		int currentVal = 100 - (count * (int)100/count);
		setInteger(ui.find(dialog, COMPONENT_SYNC_PROGRESS_BAR), Thinlet.VALUE, currentVal);
		ui.repaint();
	}
	
	/**
	 * Updates the current value of the synchronization progress bar
	 * 
	 * @param dialog
	 * @param taskNo
	 */
	public synchronized void updateProgressBar(Object dialog, int taskNo){
		Object progressBar = ui.find(dialog, COMPONENT_SYNC_PROGRESS_BAR);
		int taskCount = Integer.parseInt(getText(ui.find(dialog, COMPONENT_LBL_SYNC_TOTAL_TASK_COUNT)));
		int currentVal = getInteger(progressBar, Thinlet.VALUE);
		int maxValue = getInteger(progressBar, Thinlet.MAXIMUM);
		
		if(taskNo <= taskCount)
			setText(ui.find(dialog, COMPONENT_LBL_SYNC_CURRENT_TASK_NO), Integer.toString(taskNo));		
		
		//Calculate the unit increment and the current value
		currentVal += (taskNo <= taskCount)? (maxValue - currentVal)/taskCount : (maxValue - currentVal);
		
		//Update the progress bar with the current value
		setInteger(progressBar, Thinlet.VALUE, currentVal);
		
		ui.repaint();
	}
		
}