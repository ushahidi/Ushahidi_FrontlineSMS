package com.ushahidi.plugins.mapping.ui;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

//import com.ushahidi.plugins.mapping.SynchronizationAPI;
import com.ushahidi.plugins.mapping.SynchronizationManager;
import com.ushahidi.plugins.mapping.data.domain.*;
import com.ushahidi.plugins.mapping.data.repository.memory.*;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.maps.Map;
import com.ushahidi.plugins.mapping.maps.TileSaver;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.Utils;
import net.frontlinesms.FrontlineSMSConstants.Dependants;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.Message;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.DateSelecter;
import net.frontlinesms.ui.UiGeneratorController;
import thinlet.Thinlet;

@SuppressWarnings("serial")
public class MappingUIController extends Thinlet implements MapListener {

	private static final String COMPONENT_MESSAGE_TABLE = "messageTable";
	private static final String COMPONENT_INCIDENT_DIALOG = "incidentDialog";
	private static final String COMPONENT_CATEGORIES_COMBO = "cbCategory";
	private static final String COMPONENT_ALL_CB = "cbMfAll";
	private static final String COMPONENT_KEYWORDS_CM = "cbMfKeywords";
	private static final String COMPONENT_DATE_CB = "cbMfDate";
	private static final String COMPONENT_LOCATION_BUTTON = "btSelectPoint";
	private static final String COMPONENT_LOCATION_LABEL = "lblLoction";
	private static final String COMPONENT_LOCATION_COMBO = "cbLocation";
	private static final String COMPONENT_ADDITIONAL_INFO_TEXTFIELD = "tfAdditionalInfo";
	private static final String COMPONENT_SAVE_BUTTON = "btSave";
	private static final String COMPONENT_MAP_BEAN = "mapBean";
	private static final String COMPONENT_KEYWORDS_TABLE = "tblKeywords";
	private static final String COMPONENT_MF_DATE_PANEL = "pblMfDate";
	private static final String UI_FILE_INCIDENT_DIALOG = "/ui/dialog/incidentDialog.xml";
	private static final String UI_SETUP_DIALOG = "/ui/plugins/mapping/setupDialog.xml";
	private static final String UI_SAVE_DIALOG="/ui/plugins/mapping/mapSaveDialog.xml";
	private static final String SETUP_DLG_COMPONENT_SOURCE_TABLE = "locationSources_Table";
	private static final String SETUP_DLG_COMPONENT_FLD_SOURCE_NAME = "txtSourceName";
	private static final String SETUP_DLG_COMPONENT_FLD_SOURCE	= "txtLocationSource";
	private static final String SETUP_DLG_COMPONENT_FLD_LONGITUDE = "txtLongitude";
	private static final String SETUP_DLG_COMPONENT_FLD_LATITUDE = "txtLatitude";
	private static final String SETUP_DLG_COMPOONENT_CHK_STATUS = "chkSourceStatus";
	private static final String TAB_MAPPING = ":mapping";
	private static final String COMPONENT_COORDINATE_LABEL = "lblCoordinates";
	
	private FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
	
	/** InMemory data objects to keep track of newly added items while the application is running */
	private transient InMemoryLocationDao memoryLocationDao;
	private transient InMemoryCategoryDao memoryCategoryDao;
	private transient InMemoryIncidentDao memoryIncidentDao;
	
	private boolean keywordsLoaded = false;
	private boolean syncStarted = false;
	private MapBean mapBean;
	
	public static Logger LOG = Utils.getLogger(MappingUIController.class);	
	
	public MappingUIController(FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.frontlineController = frontlineController;		
		this.ui = uiController;
		
		this.locationDao = frontlineController.getLocationDao();
		this.categoryDao = frontlineController.getCategoryDao();
		this.incidentDao = frontlineController.getIncidentDao();
		this.mappingSetupDao  = frontlineController.getMappingSetupDao();
		
		//Check if the mapping plugin has been configured for use with an online instance
		checkPluginConfiguration();
		
		//Initialise the InMemory data sources
		memoryLocationDao = new InMemoryLocationDao();
		memoryCategoryDao = new InMemoryCategoryDao();
		memoryIncidentDao = new InMemoryIncidentDao();
		
		if(locationDao.getCount() > 0)
			memoryLocationDao.saveLocations(locationDao.getAllLocations());
		if(categoryDao.getCount() > 0)
			memoryCategoryDao.saveCategory(categoryDao.getAllCategories());
		if(incidentDao.getCount() > 0)
			memoryIncidentDao.saveIncident(incidentDao.getAllIncidents());
		
	}
	
	public void init(){
		loadKeywordList();
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
			mapBean = (MapBean)get(ui.find(getTab(), COMPONENT_MAP_BEAN), BEAN);
			
			//Check if offline mode for the default setup is enabled
			if(mappingSetupDao.getDefaultSetup().isOffline())
				mapBean.setOfflineMapFile(mappingSetupDao.getDefaultSetup().getOfflineMapFile());
			
			mapBean.setLocation(longitude, latitude);			
			mapBean.setIncidents(incidentDao.getAllIncidents());
			mapBean.setMappingUIController(this);
		}		
	}
	
	private void loadKeywordList(){
		updateKeywordList();
		
		if(memoryCategoryDao.getCount() > 0 || memoryLocationDao.getCount() >0 ){
			memoryCategoryDao.flush();
			memoryLocationDao.flush();
			memoryIncidentDao.flush();
			keywordsLoaded = true;
		}
	}
	
	public void messageFilterChanged(Object pnlSearchParams) {
		/* The rowspan property of the parent object @pnlSearchParams
		 * needs to be adjusted as the search options are toggled. 
		 */
		if(isSelected(find(getTab(), COMPONENT_ALL_CB))) {
			setVisible(ui.find(getTab(), COMPONENT_MF_DATE_PANEL), false);
			setVisible(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), false);
			ui.setInteger(pnlSearchParams, "rowspan", 1);
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
	
	
	public void updateMappingTab() {
		Object messageTableComponent = ui.find(getTab(), COMPONENT_MESSAGE_TABLE);
		removeAll(messageTableComponent);
		Integer[] status = {Message.STATUS_RECEIVED};
		for (Message m : frontlineController.getMessageDao().getMessages(Message.TYPE_RECEIVED, status)) {
			Object row = createTableRow(m);
			ui.add(row, createTableCell(Dependants.DEFAULT_MESSAGE_DATE_FORMAT.format(m.getDate())));
			ui.add(row, createTableCell(getSenderDisplayValue(m)));
			ui.add(row, createTableCell(m.getTextContent()));
			ui.add(messageTableComponent, row);
		}	
	}
	
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
	
	public void showIncidentDialog(Object item) {
		
		Message message = (Message) getAttachedObject(item);						
		if(message != null) {
			Object dialog = ui.loadComponentFromFile(UI_FILE_INCIDENT_DIALOG, this);
			ui.setAttachedObject(dialog, message);
			
			//Populate categories combo
			Object cbCategory = find(dialog, COMPONENT_CATEGORIES_COMBO);
			for(Category c : categoryDao.getAllCategories(0, 40)) {
				Object choice = ui.createChoice(c.getTitle(), c);
				ui.add(cbCategory, choice);
			}
			
			//Load Message Details			
			setText(find(dialog, "tfDate"), Dependants.DEFAULT_MESSAGE_DATE_FORMAT.format(message.getDate()));
			setText(find(dialog, "tfSender"), getSenderDisplayValue(message));
			setText(find(dialog, "tfMessage"), message.getTextContent());
			
			//Load incident details
			/*
			Incident incident = incidentDao.getIncidentByMessage(message);
			if(incident != null) {
				setText(find(dialog, "tfAdditionalInfo"), incident.getAdditionalInfo());
				showLocation(dialog, incident.getLatitude(), incident.getLongitude());
			}
			*/
			ui.add(dialog);			
		}		
	}
	
	public void startPointSelection() {
		MapBean mapBean = (MapBean) get(ui.find(getTab(), COMPONENT_MAP_BEAN), BEAN);
		mapBean.addMapListener(this);
		setVisible(ui.find(COMPONENT_INCIDENT_DIALOG), false);
		//Force Thinlet to hide the dialog now
		ui.repaint();
	}
	
	
	public void incidentFilterDateChanged() {		
	}
	
	/**
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
		mapBean.removeMapListener(this);
	}


	public void pointSelected(double lat, double lon) {
		Object dialog = ui.find(COMPONENT_INCIDENT_DIALOG);
		showLocation(dialog, lat, lon);
		incidentDialogEdited(dialog);
		setVisible(dialog, true);
		//Force Thinlet to show the dialog now
		ui.repaint();		
	}
	
	public void incidentDialogEdited(Object dialog) {
		//Category && Map point must be selected for save button to be enabled
		Object button = ui.find(dialog, COMPONENT_SAVE_BUTTON);
		if(ui.getText(ui.find(dialog,COMPONENT_LOCATION_LABEL)) != null
				&& ui.getSelectedItem(ui.find(dialog,COMPONENT_CATEGORIES_COMBO)) != null
				&& !ui.getBoolean(button, ENABLED)) {			
			setEnabled(button, true);
			ui.repaint();
		}
	}
	
	public void saveIncident() throws DuplicateKeyException {
		Object dialog = ui.find(COMPONENT_INCIDENT_DIALOG);
		
		//Get form values
		Object cbCategory = ui.find(dialog, COMPONENT_CATEGORIES_COMBO);
		Category category = (Category) ui.getAttachedObject(ui.getSelectedItem(cbCategory));
		Message message = (Message) ui.getAttachedObject(dialog);
		Object tfAdditionalInfo = ui.find(dialog,COMPONENT_ADDITIONAL_INFO_TEXTFIELD);
		String additionalInfo = ui.getText(tfAdditionalInfo);
		Object lblLocation = ui.find(dialog,COMPONENT_LOCATION_LABEL);
		String location = ui.getText(lblLocation);
		Double lat = Double.parseDouble(location.split(",")[0]);
		Double lon = Double.parseDouble(location.split(",")[1]);
		
		//Save Incident
		/*
		Incident incident = incidentDao.getIncidentByMessage(message);
		if(incident == null) {
			//New incident
			incident = new Incident(message, category, additionalInfo, lat, lon);
			incidentDao.saveIncident(incident);
		} else {
			incident.setAdditionalInfo(additionalInfo);
			incident.setLatitude(lat);
			incident.setLongitude(lon);			
		}
		*/
		removeDialog(dialog);
	}
	
	private void showLocation(Object dialog, double lat, double lon) {
		setVisible(ui.find(dialog, COMPONENT_LOCATION_BUTTON), false);
		Object lbl = ui.find(dialog, COMPONENT_LOCATION_LABEL);
		ui.setText(lbl, lat + "," + lon);
		setVisible(lbl, true);
		setEnabled(ui.find(dialog, COMPONENT_LOCATION_COMBO), false);		
	}
	
	/**
	 * Displays the mapping setup dialog
	 */
	public void showSetupDialog(){
		Object setupDialog = ui.loadComponentFromFile(UI_SETUP_DIALOG, this);
		ui.add(setupDialog);
		//Load the setup items
		if(mappingSetupDao.getCount() > 0){
			for(MappingSetup setup: mappingSetupDao.getAllSetupItems())
				addMappingSetupRow(setupDialog, setup);
		}
	}
	
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
		
		//Check for multiple default setups
		if(mappingSetupDao.getDefaultSetup() != null && active){
			ui.alert(mappingSetupDao.getDefaultSetup().getName() + 
					" ["+ mappingSetupDao.getDefaultSetup().getSourceURL() + 
					"] has already been set as the default map configuration");			
			return;
		}
		
		MappingSetup setup = new MappingSetup();
		
		setup.setName(sourceName);
		setup.setSourceURL(sourceURL);
		setup.setLatitude(Double.parseDouble(lat));
		setup.setLongitude(Double.parseDouble(lng));
		setup.setDefaultSetup(active);
		
		try{
			mappingSetupDao.saveMappingSetup(setup);
		}catch(DuplicateKeyException e){
			LOG.debug("Mapping setup parameter already exists", e);
			ui.alert("Mapping setup parameter already exists");
			LOG.trace("EXIT");
			return;
		}
		
		initializeMapBean();
		
		LOG.debug("Mapping setup parameter for [" + setup.getSourceURL() +"] created!");
		addMappingSetupRow(dialog, setup);
		ui.repaint();
		clearSourceFields(dialog);
	}
	
	private void addMappingSetupRow(Object dialog, MappingSetup setup){
		Object row = createTableRow(setup);
		createTableCell(row, setup.getName());
		createTableCell(row, setup.getSourceURL());
		createTableCell(row, Double.toString(setup.getLatitude()));
		createTableCell(row, Double.toString(setup.getLongitude()));
		String activeStr = (setup.isDefaultSetup())?"Y":"N";
		createTableCell(row, activeStr);
		add(ui.find(dialog, SETUP_DLG_COMPONENT_SOURCE_TABLE), row);		
	}
	
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
		
		if(syncStarted){
			ui.alert("Synchronization has already started!");
			return;
		}
		checkPluginConfiguration();
		SynchronizationManager syncManager = new SynchronizationManager(this);
		
		//Flush the InMemory database before the synchronization
		memoryCategoryDao.flush();
		memoryLocationDao.flush();
		memoryIncidentDao.flush();
		
		//Run a full sync
		LOG.debug("Starting full synchronization...");
		syncStarted = true;
		syncManager.performFullSynchronization();
		
		syncStarted = false;
	}
	
	/**
	 * Updates the keyword list with the new items 
	 */
	private void updateKeywordList(){
		for(Category category: memoryCategoryDao.getAllCategories()){
			Object row = createTableRow(category);
			createTableCell(row,"");
			createTableCell(row, category.getTitle());			
			add(ui.find(COMPONENT_KEYWORDS_TABLE), row);
		}
		
		for(Location location: memoryLocationDao.getAllLocations()){
			Object row = createTableRow(location);
			createTableCell(row, "");
			createTableCell(row, location.getName());
			add(ui.find(COMPONENT_KEYWORDS_TABLE), row);
		}
		
		ui.repaint(ui.find(COMPONENT_KEYWORDS_TABLE));
	}
	
	/**
	 * Adds a new category. Any exceptions are trapped and logged
	 * 
	 * @param category Category to be added
	 */
	public synchronized void addCategory(Category category){
		category.setMappingSetup(mappingSetupDao.getDefaultSetup());
		boolean exists = false;
		try{
			categoryDao.saveCategory(category);
			memoryCategoryDao.saveCategory(category);
		}catch(DuplicateKeyException e){
			exists = true;
			LOG.debug("Category already exists", e);
			LOG.trace("EXIT");
			return;
		}
		
		//Add category to the keyword listing
		if(!exists){
			Object row = ui.createTableRow(category);
			createTableCell(row, "");
			createTableCell(row, category.getTitle());
			ui.add(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), row);
			ui.repaint();
		}
		
		LOG.debug("Category [" + category.getTitle() + "] added!");
	}
	
	/**
	 * Adds a new location. The location is added both to the in memory database
	 * and to the DB used by hibernate
	 * 
	 * @param location Location to be added
	 */
	public synchronized void addLocation(Location location){
		location.setMappingSetup(mappingSetupDao.getDefaultSetup());
		boolean exists = false;
		try{
			locationDao.saveLocation(location);
			memoryLocationDao.saveLocation(location);
		}catch(DuplicateKeyException e){
			exists = true;
			LOG.debug("Location already exists", e);
			LOG.trace("EXIT");
			return;
		}
		
		//Add location to the keyword listing
		if(!exists){
			Object row = ui.createTableRow(location);
			createTableCell(row, "");
			createTableCell(row, location.getName());
			ui.add(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), row);
			ui.repaint();
		}

		LOG.debug("Location [" + location.getName() + "] created!");
	}
	
	/**
	 * Adds a new incident
	 * @param incident Incident to be added
	 */
	public synchronized void addIncident(Incident incident){
		//Find the location
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
			memoryIncidentDao.saveIncident(incident);
		} catch (DuplicateKeyException e) {
			LOG.debug("Incident already exists", e);
			LOG.trace("EXIT");
			return;
		}
		LOG.debug("Incident [" + incident.getTitle() + "] created!");		
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
	 * Checks whether the mapping plugin has been configured
	 */
	private void checkPluginConfiguration(){
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
	
	public List<Incident> getPendingIncidents(){
		return incidentDao.getUnMarkedIncidents();
	}
	
	/**
	 * Gets all the frontend ids of the locations
	 * @return
	 */
	public List<String> getLocationIds(){
		ArrayList<String> items = new ArrayList<String>();
		for(Location location: locationDao.getAllLocations())
			items.add(Long.toString(location.getFrontendId()));
		return items;
	}

	/**
	 * Get all the frontend ids of the categories
	 * @return
	 */
	public List<String> getCategoryNames(){
		ArrayList<String> items = new ArrayList<String>();
		for(Category category: categoryDao.getAllCategories())
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
	
}