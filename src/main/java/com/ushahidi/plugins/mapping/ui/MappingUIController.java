package com.ushahidi.plugins.mapping.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.sync.SynchronizationCallback;
import com.ushahidi.plugins.mapping.sync.SynchronizationManager;
import com.ushahidi.plugins.mapping.utils.MappingLogger;
import com.ushahidi.plugins.mapping.utils.MappingMessages;
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

	private static MappingLogger LOG = MappingLogger.getLogger(MappingUIController.class);
	
    /** Filename and path of the XML containing the mapping tab */
    private static final String XML_MAIN_TAB = "/ui/plugins/mapping/mainTab.xml";
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final Object mainTab;
	private final Object pnlViewIncidents;
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
	private final Object tblMessages;
	private final Object txtSearch;
	
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
		this.tblMessages = this.ui.find(this.mainTab, "tblMessages");
		this.txtSearch = this.ui.find(this.mainTab, "txtSearch");
	}
	
	/**
	 * Initializes the UI
	 * This method must be called by {@link MappingPluginController}
	 */
	public void initUIController(){
		search(this.txtSearch);
		showIncidentMap();
	}
	
	public Object getTab() {
		return mainTab;
	}
	
	public void search(Object textField) {
		String searchText = ui.getText(textField).trim().toLowerCase();
		ui.removeAll(tblMessages);		
		for (FrontlineMessage message : frontlineController.getMessageDao().getMessages(FrontlineMessage.Type.RECEIVED, FrontlineMessage.Status.RECEIVED)) {
			String sender = getSenderDisplayName(message);
			if (searchText.length() == 0 || 
				searchText.equalsIgnoreCase(MappingMessages.getSearchMessages()) ||
				message.getTextContent().toLowerCase().indexOf(searchText) > -1 ||
				sender.toLowerCase().indexOf(searchText) > -1) {
				ui.add(tblMessages, getRow(message));
			}
		}
	}
	
	/**
	 * Displays the incident creation dialog
	 * This dialog allows the user to create an Ushahidi incident report from a text message
	 *  
	 * @param item Selected {@link Message}; the one an incident report shall be created from
	 */
	public ReportDialogHandler showIncidentDialog(Object table, Object item) {
		FrontlineMessage message = (FrontlineMessage) getAttachedObject(item);						
		if (message != null) {
			ReportDialogHandler dialog = new ReportDialogHandler(pluginController, frontlineController, ui);
			mapPanelHandler.addMapListener(dialog);
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
		SetupDialogHandler dialog = new SetupDialogHandler(pluginController, frontlineController, ui);
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
		createTableCell(row, getSenderDisplayName(message));
		createTableCell(row, message.getTextContent());
		return row;
	}
	
	/**
	 * Gets the display name for the sender of of the text message
	 * @param message
	 * @return
	 */
	private String getSenderDisplayName(FrontlineMessage message) {
		Contact sender = frontlineController.getContactDao().getFromMsisdn(message.getSenderMsisdn());
		if (sender != null) {
			if (sender.getDisplayName() != null) {
				return String.format("%s (%s)", sender.getDisplayName(), message.getSenderMsisdn()) ;
			}
			return String.format("%s", message.getSenderMsisdn());
		}
		return "";
	}
	
	/**
	 * Performs synchronization with an Ushahidi instance
	 */
	public void beginSynchronization(){
		//Check if the mapping plugin has been configured to synchronize to an Ushahidi instance
		if (mappingSetupDao.getCount() == 0 || mappingSetupDao.getDefaultSetup() == null){
			LOG.debug("Mapping plugin has not been configured");
			showSetupDialog();
			ui.alert(MappingMessages.getSetupDefaultMissing());
		}
		else if(mappingSetupDao.getCount() > 0 && mappingSetupDao.getDefaultSetup() == null){
			LOG.debug("Default mapping setup not set");
			showSetupDialog();
			ui.alert(MappingMessages.getSetupDefaultRequired());
		}
		else{
			syncManager = new SynchronizationManager(this, mappingSetupDao.getDefaultSetup().getSourceURL());		
			LOG.debug("Starting full synchronization...");
			List<Incident> pendingIncidents = incidentDao.getUnMarkedIncidents(mappingSetupDao.getDefaultSetup());
			syncManager.performFullSynchronization(pendingIncidents);
		}
	}
	
//	/**
//	 * Gets all the frontend ids of the locations
//	 * @return
//	 */
//	public List<String> getLocationIds(){
//		ArrayList<String> locations = new ArrayList<String>();
//		for(Location location : locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {
//			locations.add(Long.toString(location.getFrontendId()));
//		}
//		return locations;
//	}
	
	/**
	 * Adds an incoming message (received via the connected mobile phone) to the list of
	 * messages
	 * 
	 * @param message The received message
	 */
	public void handleIncomingMessage(FrontlineMessage message) {
		ui.add(tblMessages, getRow(message));
	}
	
	public void showIncidentMap() {
		LOG.debug("showIncidentMap");
		if (mapPanelHandler == null) {
			mapPanelHandler = new MapPanelHandler(pluginController, frontlineController, ui);
		}
		ui.setSelected(cbxIncidentMap, true);
		ui.setSelected(cbxIncidentList, false);
		mapPanelHandler.init();
		ui.removeAll(pnlViewIncidents);
		ui.add(pnlViewIncidents, mapPanelHandler.getMainPanel());
	}
	
	public void refreshIncidentMap() {
		if (mapPanelHandler != null) {
			mapPanelHandler.refresh();
		}
	}
	
	public void showIncidentReports() {
		LOG.debug("showIncidentReports");
		if (reportsPanelHandler == null) {
			reportsPanelHandler = new ReportsPanelHandler(pluginController, frontlineController, ui);
		}
		if (mapPanelHandler == null) {
			mapPanelHandler.destroyMap();
		}
		ui.setSelected(cbxIncidentMap, false);
		ui.setSelected(cbxIncidentList, true);
		reportsPanelHandler.init();
		ui.removeAll(pnlViewIncidents);
		ui.add(pnlViewIncidents, reportsPanelHandler.getMainPanel());
	}

	public void refreshIncidentReports() {
		LOG.debug("refreshIncidentReports");
		if (reportsPanelHandler != null) {
			reportsPanelHandler.refresh();
		}
	}
	
	public void focusGained(Object textfield) {
		String searchText = this.ui.getText(textfield);
		if (searchText.equalsIgnoreCase(MappingMessages.getSearchMessages())) {
			this.ui.setText(textfield, "");
		}
		this.ui.setForeground(Color.BLACK);
	}
	
	public void focusLost(Object textfield) {
		String searchText = this.ui.getText(textfield);
		if (searchText == null || searchText.length() == 0) {
			this.ui.setText(textfield, MappingMessages.getSearchMessages());
			this.ui.setForeground(Color.LIGHT_GRAY);
		}
		else {
			this.ui.setForeground(Color.BLACK);
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
		}
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
		}
	}

	public void synchronizationFailed(String error) {
		LOG.debug("synchronizationFailed:%s", error);
		syncDialog.hideDialog();
		ui.alert(error);
	}

	public void synchronizationFinished() {
		LOG.debug("synchronizationFinished");
		syncDialog.hideDialog();
	}

	public void synchronizationStarted(int tasks) {
		LOG.debug("synchronizationStarted:%d", tasks);
		if (syncDialog == null) {
			syncDialog = new SyncDialogHandler(pluginController, frontlineController, ui);	
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