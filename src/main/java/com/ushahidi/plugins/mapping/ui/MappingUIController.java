package com.ushahidi.plugins.mapping.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.sync.SynchronizationManager;
import com.ushahidi.plugins.mapping.data.domain.*;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.FrontlineUtils;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.DateSelecter;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class MappingUIController extends ExtendedThinlet implements ThinletUiEventHandler {

    /** Filename and path of the XML containing the mapping tab */
    private static final String XML_MAIN_TAB = "/ui/plugins/mapping/mainTab.xml";
	
	private static final String COMPONENT_MESSAGE_TABLE = "messageTable";
	private static final String COMPONENT_LOCATIONS_COMBO = "cboLocations";
	private static final String COMPONENT_ALL_CB = "cbMfAll";
	private static final String COMPONENT_KEYWORDS_CM = "cbMfKeywords";
	private static final String COMPONENT_DATE_CB = "cbMfDate";
	private static final String COMPONENT_LOCATION_LABEL = "lblLoction";
	private static final String COMPONENT_SAVE_BUTTON = "btSave";
	private static final String COMPONENT_KEYWORDS_TABLE = "tblKeywords";
	private static final String COMPONENT_MF_DATE_PANEL = "pblMfDate";
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private Object mainTab;
	private Object pnlViewIncidents;
	
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
		
	private MapBean mapBean;
	private SynchronizationManager syncManager;
	
	public static Logger LOG = FrontlineUtils.getLogger(MappingUIController.class);	
	
	public MappingUIController(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.locationDao = pluginController.getLocationDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainTab = this.ui.loadComponentFromFile(XML_MAIN_TAB, this);
		this.pnlViewIncidents = this.ui.find(this.mainTab, "pnlViewIncidents");
	}
	
	/**
	 * Initializes the UI
	 * This method must be called by {@link MappingPluginController}
	 */
	public void initUIController(){
		updateKeywordList();
		updateMappingTab();
		showIncidentMap();
	}
	
	public Object getTab() {
		return this.mainTab;
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
		if (isSelected(find(getTab(), COMPONENT_ALL_CB))) {
			setVisible(ui.find(getTab(), COMPONENT_MF_DATE_PANEL), false);
			setVisible(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), false);
			ui.setInteger(pnlSearchParams, "rowspan", 1);
			if (incidentDao.getCount() > 0) {
				//TODO update map or reports
				//mapBean.setIncidents(incidentDao.getAllIncidents());
			}
			
		} 
		else if(isSelected(find(getTab(), COMPONENT_KEYWORDS_CM))) {
			setVisible(ui.find(getTab(), COMPONENT_MF_DATE_PANEL), false);
			setVisible(ui.find(getTab(), COMPONENT_KEYWORDS_TABLE), true);
			ui.setInteger(pnlSearchParams, "rowspan", 4);
			
		} 
		else if(isSelected(find(getTab(), COMPONENT_DATE_CB))) {
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
		Object messageTableComponent = ui.find(this.mainTab, COMPONENT_MESSAGE_TABLE);
		removeAll(messageTableComponent);		
		for (FrontlineMessage m : frontlineController.getMessageDao().getMessages(FrontlineMessage.Type.RECEIVED, FrontlineMessage.Status.RECEIVED)) {
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
	public ReportDialogHandler showIncidentDialog(Object item) {
		FrontlineMessage message = (FrontlineMessage) getAttachedObject(item);						
		if (message != null) {
			ReportDialogHandler dialog = new ReportDialogHandler(this.pluginController, this.frontlineController, this.ui);
			dialog.showDialog(message);	
			return dialog;
		}
		return null;
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
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
	 * Displays the mapping setup dialog
	 */
	public SetupDialogHandler showSetupDialog(){
		SetupDialogHandler dialog = new SetupDialogHandler(this.pluginController, this.frontlineController, this.ui);
		dialog.showDialog();
		return dialog;
	}
	
	/**
	 * Gets a row with {@link Message} attached
	 * @param message
	 * @return
	 */
	private Object getRow(FrontlineMessage message){		
		Object row = createTableRow(message);
		createTableCell(row, InternationalisationUtils.getDatetimeFormat().format(message.getDate()));
		createTableCell(row, getSenderDisplayValue(message));
		createTableCell(row, message.getTextContent());
		return row;
	}
	
	/**
	 * Gets the display name for the sender of of the text message
	 * @param message
	 * @return
	 */
	private String getSenderDisplayValue(FrontlineMessage message) {
		Contact sender = frontlineController.getContactDao().getFromMsisdn(message.getSenderMsisdn());
		return sender != null ? sender.getDisplayName() + "(" + message.getSenderMsisdn() + ")" : message.getSenderMsisdn();
	}
	
	/**
	 * Performs synchronization with an Ushahidi instance
	 */
	public void beginSynchronization(){
		//Check if the mapping plugin has been configured to synchronize to an Ushahidi instance
		if (mappingSetupDao.getCount() == 0 || mappingSetupDao.getDefaultSetup() == null || getDefaultSynchronizationURL() == null){
			check_PluginConfiguration();
		}
		else{
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
		removeAll(ui.find(this.mainTab, COMPONENT_KEYWORDS_TABLE));
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
			add(ui.find(this.mainTab, COMPONENT_KEYWORDS_TABLE), row);
		}
		ui.repaint(ui.find(this.mainTab, COMPONENT_KEYWORDS_TABLE));
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
			}
			catch(DuplicateKeyException e){
				LOG.debug("Category already exists", e);
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
		if (locationDao.findLocation(location.getFrontendId(), mappingSetupDao.getDefaultSetup()) == null){
			location.setMappingSetup(mappingSetupDao.getDefaultSetup());
			try{
				locationDao.saveLocation(location);
			}
			catch(DuplicateKeyException e){			
				LOG.debug("Location already exists", e);
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
		return (mappingSetupDao.getDefaultSetup() == null) ? null : 
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
		}
		else if(mappingSetupDao.getCount() > 0 && mappingSetupDao.getDefaultSetup() == null){
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
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {
			items.add(Long.toString(location.getFrontendId()));
		}
		return items;
	}
	
	/**
	 * Updates the UI when the keyword selection changes
	 * @param tblKeywords
	 */
	public void mappingTab_keywordSelectionChanged(Object tblKeywords){
		Object item = getAttachedObject(getSelectedItem(tblKeywords));
		if(item instanceof Category) {
			
		}
		else if(item instanceof Location){
			Location location = (Location)item;
			location.setMappingSetup(mappingSetupDao.getDefaultSetup());
			List<Incident> incidents = incidentDao.getIncidentsByLocation(location);
			mapBean.setIncidents(incidents);
		}
		else {
			throw new RuntimeException();
		}
	}
	
	/**
	 * Adds an incoming message (received via the connected mobile phone) to the list of
	 * messages
	 * 
	 * @param message The received message
	 */
	public void handleIncomingMessage(FrontlineMessage message) {
		Object messageTableComponent = ui.find(this.mainTab, COMPONENT_MESSAGE_TABLE);
		ui.add(messageTableComponent, getRow(message));
		ui.repaint();
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
		}
		catch(DuplicateKeyException dk){
			LOG.debug("Unable to update incident", dk);
		}
	}
	
	/**
	 * Displays the synchronization dialog for the duration of the sync
	 * 
	 * @return
	 */
	public SyncDialogHandler showSyncDialog() {
		SyncDialogHandler syncDialog = new SyncDialogHandler(this.pluginController, this.frontlineController, this.ui);
		syncDialog.showDialog();
		return syncDialog;
	}
	
	/**
	 * Kills all the mapping and synchronization threads that are still running
	 */
	public void shutdownUIController(){
		if (syncManager != null) {
			syncManager.terminateManagerThread();
		}
		if (mapBean != null) {
			mapBean.destroyMap();
		}
	}
	
	public MapPanelHandler showIncidentMap() {
		System.out.println("showIncidentMap");
		MapPanelHandler mapPanel = new MapPanelHandler(this.pluginController, this.frontlineController, this.ui);
		mapPanel.showPanel(this.pnlViewIncidents);
		return mapPanel;
	}
	
	public ReportsPanelHandler showIncidentReports() {
		System.out.println("showIncidentReports");
		ReportsPanelHandler reportsPanel = new ReportsPanelHandler(this.pluginController, this.frontlineController, this.ui);
		reportsPanel.showPanel(this.pnlViewIncidents);
		return reportsPanel;
	}
		
}