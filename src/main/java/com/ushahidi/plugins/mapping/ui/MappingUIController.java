package com.ushahidi.plugins.mapping.ui;

import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.sync.SynchronizationCallback;
import com.ushahidi.plugins.mapping.sync.SynchronizationManager;
import com.ushahidi.plugins.mapping.utils.MappingLogger;
import com.ushahidi.plugins.mapping.data.domain.*;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class MappingUIController extends ExtendedThinlet implements ThinletUiEventHandler, SynchronizationCallback {

	public static MappingLogger LOG = MappingLogger.getLogger(MappingUIController.class);
	
    /** Filename and path of the XML containing the mapping tab */
    private static final String XML_MAIN_TAB = "/ui/plugins/mapping/mainTab.xml";
	
	private static final String COMPONENT_MESSAGE_TABLE = "messageTable";
	private static final String COMPONENT_ALL_CB = "cbMfAll";
	private static final String COMPONENT_KEYWORDS_CM = "cbMfKeywords";
	private static final String COMPONENT_DATE_CB = "cbMfDate";
	private static final String COMPONENT_KEYWORDS_TABLE = "tblKeywords";
	private static final String COMPONENT_MF_DATE_PANEL = "pblMfDate";
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private Object mainTab;
	private Object pnlViewIncidents;
	private SyncDialogHandler syncDialog;
	
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
		
	private SynchronizationManager syncManager;
	private MapPanelHandler mapPanelHandler;
	private ReportsPanelHandler reportsPanelHandler;
	
	private final Object cbxIncidentMap;
	private final Object cbxIncidentList;
	
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
		this.cbxIncidentMap = this.ui.find(this.mainTab, "cbxIncidentMap");
		this.cbxIncidentList = this.ui.find(this.mainTab, "cbxIncidentList");
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
				//mapBean.setIncidents(incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup()));
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
		for (FrontlineMessage message : frontlineController.getMessageDao().getMessages(FrontlineMessage.Type.RECEIVED, FrontlineMessage.Status.RECEIVED)) {
			ui.add(messageTableComponent, getRow(message));
		}	
	}
	
	/**
	 * Displays the date selector
	 * 
	 * @param textField
	 */
	public void showDateSelector(Object textField) {
		ui.showDateSelecter(textField);
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
			this.mapPanelHandler.addMapListener(dialog);
			dialog.showDialog(message);	
			return dialog;
		}
		return null;
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
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
		if (mappingSetupDao.getCount() == 0 || mappingSetupDao.getDefaultSetup() == null){
			LOG.debug("Mapping plugin has not been configured");
			showSetupDialog();
			ui.alert("The mapping plugin has not been configured!");
		}
		else if(mappingSetupDao.getCount() > 0 && mappingSetupDao.getDefaultSetup() == null){
			LOG.debug("Default mapping setup not set");
			showSetupDialog();
			ui.alert("Please select the default mapping setup");
		}
		else{
			syncManager = new SynchronizationManager(this, this.mappingSetupDao.getDefaultSetup().getSourceURL());		
			LOG.debug("Starting full synchronization...");
			List<Incident> pendingIncidents = incidentDao.getUnMarkedIncidents(mappingSetupDao.getDefaultSetup());
			syncManager.performFullSynchronization(pendingIncidents);
		}
	}
	
	/**
	 * Updates the keyword list with the new items 
	 */
	private void updateKeywordList(){
		removeAll(ui.find(this.mainTab, COMPONENT_KEYWORDS_TABLE));
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())){
			Object row = createTableRow(location);
			createTableCell(row, "");
			createTableCell(row, location.getName());
			add(ui.find(this.mainTab, COMPONENT_KEYWORDS_TABLE), row);
		}
		ui.repaint(ui.find(this.mainTab, COMPONENT_KEYWORDS_TABLE));
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
			//TODO update
//			mapBean.setIncidents(incidents);
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
	
	public void showIncidentMap() {
		System.out.println("showIncidentMap");
		if (this.mapPanelHandler == null) {
			this.mapPanelHandler = new MapPanelHandler(this.pluginController, this.frontlineController, this.ui);
		}
		this.setSelected(this.cbxIncidentMap, true);
		this.setSelected(this.cbxIncidentList, false);
		this.mapPanelHandler.init();
		this.removeAll(this.pnlViewIncidents);
		this.repaint(this.pnlViewIncidents);
		this.add(this.pnlViewIncidents, this.mapPanelHandler.getMainPanel());
		this.repaint(this.pnlViewIncidents);
	}
	
	public void showIncidentReports() {
		System.out.println("showIncidentReports");
		if (this.reportsPanelHandler == null) {
			this.reportsPanelHandler = new ReportsPanelHandler(this.pluginController, this.frontlineController, this.ui);
		}
		if (this.mapPanelHandler == null) {
			this.mapPanelHandler.destroyMap();
		}
		this.setSelected(this.cbxIncidentMap, false);
		this.setSelected(this.cbxIncidentList, true);
		this.reportsPanelHandler.init();
		this.removeAll(this.pnlViewIncidents);
		this.repaint(this.pnlViewIncidents);
		this.add(this.pnlViewIncidents, this.reportsPanelHandler.getMainPanel());
		this.repaint(this.pnlViewIncidents);
	}

	//SYNCHRONIZATION
	
	public void downloadedCategory(Category category) {
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

	//################# SynchronizationCallback #################
	
	/**
	 * Kills all the mapping and synchronization threads that are still running
	 */
	public void shutdownUIController(){
		if (syncManager != null) {
			syncManager.terminateManagerThread();
		}
	}
	
	public void downloadedGeoMidpoint(String domain, String latitude, String longitude) {
		LOG.debug("downloadedGeoMidpoint: %s (%s,%s)", domain, latitude, longitude);
	}

	public void downloadedIncident(Incident incident) {
		if(incidentDao.findIncident(incident.getFrontendId(), mappingSetupDao.getDefaultSetup()) == null){
			long frontendId = incident.getLocation().getFrontendId();
			Location location = locationDao.findLocation(frontendId, mappingSetupDao.getDefaultSetup());
			
			if(location == null){
				downloadedLocation(incident.getLocation());
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

	public void downloadedLocation(Location location) {
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

	public void synchronizationFailed(String error) {
		LOG.debug("synchronizationFailed:%s", error);
		syncDialog.hideDialog();
		this.ui.alert(error);
	}

	public void synchronizationFinished() {
		LOG.debug("synchronizationFinished");
		syncDialog.hideDialog();
	}

	public void synchronizationStarted(int tasks) {
		LOG.debug("synchronizationStarted:%d", tasks);
		if (syncDialog == null) {
			syncDialog = new SyncDialogHandler(this.pluginController, this.frontlineController, this.ui);	
		}
		syncDialog.setProgress(tasks, 1);
		syncDialog.showDialog();
	}

	public void synchronizationUpdated(int tasks, int completed) {
		LOG.debug("synchronizationUpdated: %d/%d", completed, tasks);
		syncDialog.setProgress(tasks, completed);	
	}

	public void uploadedIncident(Incident incident) {
		incident.setMarked(false);
		try{
			incidentDao.updateIncident(incident);
		}
		catch(DuplicateKeyException dk){
			LOG.debug("Unable to update incident", dk);
		}
	}
		
}