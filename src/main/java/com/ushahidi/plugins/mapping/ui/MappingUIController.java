package com.ushahidi.plugins.mapping.ui;

import java.awt.Color;
import java.util.List;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.managers.FormsManager;
import com.ushahidi.plugins.mapping.managers.SurveysManager;
import com.ushahidi.plugins.mapping.sync.SynchronizationCallback;
import com.ushahidi.plugins.mapping.sync.SynchronizationManager;
import com.ushahidi.plugins.mapping.util.MappingDebug;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;
import com.ushahidi.plugins.mapping.util.MappingProperties;
import com.ushahidi.plugins.mapping.data.domain.*;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.data.domain.FrontlineMessage.Type;
import net.frontlinesms.data.events.EntitySavedNotification;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.data.repository.MessageDao;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.events.FrontlineEventNotification;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class MappingUIController extends ExtendedThinlet implements ThinletUiEventHandler, SynchronizationCallback, EventObserver  {

	private static MappingLogger LOG = MappingLogger.getLogger(MappingUIController.class);
	
    /** Filename and path of the XML containing the mapping tab */
    private static final String XML_MAIN_TAB = "/ui/plugins/mapping/mainTab.xml";
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
    private final SurveysManager surveysManager;
	private final FormsManager formsManager;
	
	private final Object mainTab;
	private final Object pnlViewIncidents;
	private SyncDialogHandler syncDialog;
	
	private final ContactDao contactDao;
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MessageDao messageDao;
	private final MappingSetupDao mappingSetupDao;
		
	private SynchronizationManager syncManager;
	private MapPanelHandler mapPanelHandler;
	private ReportsPanelHandler reportsPanelHandler;
	
	private final Object cbxIncidentMap;
	private final Object cbxIncidentList;
	private final Object tblMessages;
	private final Object txtSearchMessages;
	private final Object txtSearchContacts;
	private final Object tblContacts;
	private final Object pnlContacts;
	private final Object pnlMessages;
	private final Object cbxContacts;
	
	public MappingUIController(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		this.frontlineController.getEventBus().registerObserver(this);
		
		this.contactDao = frontlineController.getContactDao();
		this.locationDao = pluginController.getLocationDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.messageDao = pluginController.getMessageDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainTab = this.ui.loadComponentFromFile(XML_MAIN_TAB, this);
		this.pnlViewIncidents = this.ui.find(this.mainTab, "pnlViewIncidents");
		this.cbxIncidentMap = this.ui.find(this.mainTab, "cbxIncidentMap");
		this.cbxIncidentList = this.ui.find(this.mainTab, "cbxIncidentList");
		this.tblMessages = this.ui.find(this.mainTab, "tblMessages");
		this.txtSearchMessages = this.ui.find(this.mainTab, "txtSearchMessages");
		this.txtSearchContacts = this.ui.find(this.mainTab, "txtSearchContacts");
		this.tblContacts = this.ui.find(this.mainTab, "tblContacts");
		this.pnlContacts = this.ui.find(this.mainTab, "pnlContacts");
		this.pnlMessages = this.ui.find(this.mainTab, "pnlMessages");
		this.cbxContacts = this.ui.find(this.mainTab, "cbxContacts");
		
		this.surveysManager = new SurveysManager(frontlineController, pluginController);
		this.formsManager = new FormsManager(frontlineController, pluginController);
		
		if (MappingProperties.isDebugMode()) {
			MappingDebug mappingDebug = new MappingDebug(formsManager, surveysManager, messageDao, contactDao);
			mappingDebug.startDebugTerminal();
		}
	}
	
	/**
	 * Initializes the UI
	 * This method must be called by {@link MappingPluginController}
	 */
	public void initUIController(){
		searchMessages(this.txtSearchMessages);
		showIncidentMap();
	}
	
	public Object getTab() {
		return mainTab;
	}
	
	public void setStatus(String status) {
		ui.setStatus(status);
	}
	
	public void showMessages() {
		LOG.debug("showMessages");
		ui.removeAll(tblMessages);
		for (FrontlineMessage message : frontlineController.getMessageDao().getMessages(FrontlineMessage.Type.RECEIVED, FrontlineMessage.Status.RECEIVED)) {
			ui.add(tblMessages, getRow(message));
		}
		ui.setVisible(pnlMessages, true);
		ui.setVisible(pnlContacts, false);
	}
	
	public void showContacts() {
		LOG.debug("showContacts");
		ui.removeAll(tblContacts);
		for(Contact contact : contactDao.getAllContacts()) {
			LOG.debug("Contact:%s", contact.getName());
			ui.add(tblContacts, getRow(contact));
		}
		ui.setVisible(pnlMessages, false);
		ui.setVisible(pnlContacts, true);
	}
	
	public void refreshContacts() {
		LOG.debug("refreshContacts");
		if (this.getBoolean(cbxContacts, Thinlet.SELECTED)) {
			showContacts();
		}
	}
	
	public void searchMessages(Object textField) {
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
	
	public void searchContacts(Object textField) {
		String searchText = ui.getText(textField).trim().toLowerCase();
		ui.removeAll(tblContacts);
		for(Contact contact : contactDao.getAllContacts()) {
			if (contact.getName() == null || contact.getName().toLowerCase().indexOf(searchText) > -1) {
				ui.add(tblContacts, getRow(contact));
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
		FrontlineMessage message = ui.getAttachedObject(item, FrontlineMessage.class);						
		if (message != null) {
			ReportDialogHandler dialog = new ReportDialogHandler(pluginController, frontlineController, ui);
			mapPanelHandler.addMapListener(dialog);
			dialog.showDialog(message);	
			return dialog;
		}
		return null;
	}
	
	public ContactDialogHandler showContactDialog(Object table, Object item) {
		Contact contact = ui.getAttachedObject(item, Contact.class);	
		if (contact != null) {
			ContactDialogHandler dialog = new ContactDialogHandler(pluginController, frontlineController, ui);
			mapPanelHandler.addMapListener(dialog);
			dialog.showDialog(contact);	
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
		SetupDialogHandler dialog = new SetupDialogHandler(pluginController, frontlineController, ui, formsManager, surveysManager);
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
	
	private Object getRow(Contact contact){		
		Object row = createTableRow(contact);
		if (contact != null) {
			createTableCell(row, contact.getDisplayName());
			LocationDetails locationDetails = contact.getDetails(LocationDetails.class);
			if (locationDetails != null) {
				createTableCell(row, locationDetails.getLocationName());
				createTableCell(row, locationDetails.getLocationLatitude());
				createTableCell(row, locationDetails.getLocationLongitude());
			}
			else {
				createTableCell(row, "");
				createTableCell(row, "");
				createTableCell(row, "");	
			}
		}
		else {
			createTableCell(row, "");
			createTableCell(row, "");
			createTableCell(row, "");
			createTableCell(row, "");
		}
		return row;
	}
	
	/**
	 * Gets the display name for the sender of of the text message
	 * @param message
	 * @return
	 */
	private String getSenderDisplayName(FrontlineMessage message) {
		Contact contact = frontlineController.getContactDao().getFromMsisdn(message.getSenderMsisdn());
		if (contact != null) {
			if (contact.getName() != null) {
				return String.format("%s (%s)", contact.getName(), message.getSenderMsisdn()) ;
			}
			return String.format("%s", message.getSenderMsisdn());
		}
		return "";
	}
	
	/**
	 * Performs synchronization with an Ushahidi instance
	 */
	public void beginSynchronization(){
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
	
	public void showIncidentMap() {
		LOG.debug("showIncidentMap");
		if (mapPanelHandler == null) {
			mapPanelHandler = new MapPanelHandler(pluginController, frontlineController, ui);
			mapPanelHandler.setSurveyResponseDao(surveysManager.getSurveyResponseDao());
			mapPanelHandler.setFormResponseDao(formsManager.getFormResponseDao());
		}
		ui.setSelected(cbxIncidentMap, true);
		ui.setSelected(cbxIncidentList, false);
		mapPanelHandler.init();
		ui.removeAll(pnlViewIncidents);
		ui.add(pnlViewIncidents, mapPanelHandler.getMainPanel());
		mapPanelHandler.refresh();
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
		reportsPanelHandler.refresh();
	}

	public void refreshIncidentReports() {
		LOG.debug("refreshIncidentReports");
		if (reportsPanelHandler != null) {
			reportsPanelHandler.refresh();
		}
	}
	
	public void focusGained(Object textfield) {
		String searchText = this.ui.getText(textfield);
		if (textfield == txtSearchMessages && searchText.equalsIgnoreCase(MappingMessages.getSearchMessages())) {
			this.ui.setText(textfield, "");
		}
		else if (textfield == txtSearchContacts && searchText.equalsIgnoreCase(MappingMessages.getSearchContacts())) {
			this.ui.setText(textfield, "");
		}
		this.ui.setForeground(Color.BLACK);
	}
	
	public void focusLost(Object textfield) {
		String searchText = this.ui.getText(textfield);
		if (searchText == null || searchText.length() == 0) {
			if (textfield == txtSearchMessages) {
				this.ui.setText(textfield, MappingMessages.getSearchMessages());
			}
			else if (textfield == txtSearchContacts) {
				this.ui.setText(textfield, MappingMessages.getSearchMessages());
			}
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
		if(categoryDao.findCategory(category.getServerId(), mappingSetupDao.getDefaultSetup()) == null){
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
		if(incidentDao.findIncident(incident.getServerId(), mappingSetupDao.getDefaultSetup()) == null){
			if (incident.getLocation() != null) {
				if (locationDao.findLocation(incident.getLocation().getServerId(), mappingSetupDao.getDefaultSetup()) == null) {
					downloadedLocation(incident.getLocation());
				}
			}
			if (incident.getCategories() != null) {
				for(Category category : incident.getCategories()) {
					if (categoryDao.findCategory(category.getServerId(), mappingSetupDao.getDefaultSetup()) == null) {
						downloadedCategory(category);
					}
				}
			}
			incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
			try {
				incidentDao.saveIncident(incident);
			} catch (DuplicateKeyException e) {
				LOG.debug("Incident already exists", e);
				return;
			}
			LOG.debug("Incident [%s] created!", incident.getTitle());
		}
	}

	public void downloadedLocation(Location location) {
		if (locationDao.findLocation(location.getServerId(), mappingSetupDao.getDefaultSetup()) == null){
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
		pluginController.refreshIncidentMap();
		pluginController.refreshIncidentReports();
		ui.alert(error);
	}

	public void synchronizationFinished() {
		LOG.debug("synchronizationFinished");
		syncDialog.hideDialog();
		pluginController.refreshIncidentMap();
		pluginController.refreshIncidentReports();
	}

	public void synchronizationStarted(int tasks) {
		LOG.debug("synchronizationStarted:%d", tasks);
		if (syncDialog == null) {
			syncDialog = new SyncDialogHandler(pluginController, frontlineController, ui);	
		}
		syncDialog.setProgress(tasks, 0);
		syncDialog.showDialog();
	}

	public void synchronizationUpdated(int tasks, int completed) {
		LOG.debug("synchronizationUpdated: %d/%d", completed, tasks);
		syncDialog.setProgress(tasks, completed);	
	}

	public void uploadedIncident(Incident incident) {
		try{
			incident.setMarked(false);
			incidentDao.updateIncident(incident);
			reportsPanelHandler.refresh();
		}
		catch(DuplicateKeyException dk){
			dk.printStackTrace();
			LOG.debug("Unable to update incident", dk);
		}
	}

	//################# EventObserver #################
	
	public void notify(FrontlineEventNotification notification) {
		if (notification instanceof EntitySavedNotification<?>) {
			EntitySavedNotification<?> entitySavedNotification = (EntitySavedNotification<?>)notification;
			if (entitySavedNotification.getDatabaseEntity() instanceof FrontlineMessage) {
				FrontlineMessage message = (FrontlineMessage)entitySavedNotification.getDatabaseEntity();
				if (message != null && message.getType() == Type.RECEIVED) {
					ui.add(tblMessages, getRow(message));
					LOG.debug("Adding Message: %s", message.getTextContent());
				}
			}
			else if (entitySavedNotification.getDatabaseEntity() instanceof Contact) {
				searchContacts(this.txtSearchContacts);
			}
		}
	}
		
}