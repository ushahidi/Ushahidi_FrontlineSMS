package com.ushahidi.plugins.mapping.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import thinlet.Thinlet;
import thinlet.ThinletText;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.managers.FormsManager;
import com.ushahidi.plugins.mapping.managers.OperatorManager;
import com.ushahidi.plugins.mapping.sync.SynchronizationCallback;
import com.ushahidi.plugins.mapping.sync.SynchronizationManager;
import com.ushahidi.plugins.mapping.utils.MappingLogger;
import com.ushahidi.plugins.mapping.utils.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.Icon;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class SetupDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler, SynchronizationCallback {

	public static MappingLogger LOG = MappingLogger.getLogger(SetupDialogHandler.class);
	
	private static final String UI_SETUP_DIALOG = "/ui/plugins/mapping/setupDialog.xml";
	
	private static final String CONFIRM_DELETE_KEY = "plugins.ushahidi.setup.confirm.delete";
	private static final String CONFIRM_SYNCHRONIZE_KEY = "plugins.ushahidi.setup.synchronize";
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final Object mainDialog;
	private final Object tblSources;
	private final Object txtSourceName;
	private final Object txtSourceURL;
	private final Object chkSourceDefault;
	private final Object btnSave;
	private final Object btnDelete;
	private final Object btnCancel;
	
	private boolean shouldSynchronize;
	
	public SetupDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.locationDao = pluginController.getLocationDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainDialog = ui.loadComponentFromFile(UI_SETUP_DIALOG, this);
		
		this.tblSources = ui.find(this.mainDialog, "tblSources");
		this.txtSourceName = ui.find(this.mainDialog, "txtSourceName");
		this.txtSourceURL = ui.find(this.mainDialog, "txtSourceURL");
		this.chkSourceDefault = ui.find(this.mainDialog, "chkSourceDefault");
		
		this.btnSave = ui.find(this.mainDialog, "btnSave");
		this.btnDelete = ui.find(this.mainDialog, "btnDelete");
		this.btnCancel = ui.find(this.mainDialog, "btnCancel");
	}
	
	public void showDialog() {
		if (mappingSetupDao.getCount() > 0){
			ui.removeAll(tblSources);
			for(MappingSetup setup: mappingSetupDao.getAllSetupItems()) {
				add(tblSources, getRow(setup));
			}
		}
		shouldSynchronize = false;
		ui.add(this.mainDialog);	
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
		if (shouldSynchronize) {
			ui.showConfirmationDialog("beginSynchronization", this, CONFIRM_SYNCHRONIZE_KEY);
		}
	}
	
	public void beginSynchronization() {
		ui.removeConfirmationDialog();
		pluginController.beginSynchronization();
	}
	
	public void showConfirmationDialog(String methodToBeCalled) {
		ui.showConfirmationDialog(methodToBeCalled, this, CONFIRM_DELETE_KEY);
	}
	
	/**
	 * Gets a table row with {@link MappingSetup} attached
	 * @param setup
	 * @return
	 */
	private Object getRow(MappingSetup setup){
		Object row = createTableRow(setup);
		if (setup.isDefaultSetup()) {
			Object cell = this.createTableCell("");
			ui.setIcon(cell, Icon.TICK);
			ui.setChoice(cell, ThinletText.ALIGNMENT, ThinletText.CENTER);
			ui.add(row, cell);
		}
		else {
			createTableCell(row, "");
		}
		createTableCell(row, setup.getName());
		createTableCell(row, setup.getSourceURL());
		return row;
	}
	
	/**
	 * Loads the details of the mapping setup in the fields for editing
	 * 
	 * @param setupDialog Setup dialog for the map
	 * @param tblLocationSource Table containing the list of the mapping sources
	 */
	public void editMappingSource(Object setupDialog, Object tblLocationSource){
		Object selectedItem = getSelectedItem(tblLocationSource);
		Object attachedObject = getAttachedObject(selectedItem, MappingSetup.class);
		if(attachedObject instanceof MappingSetup){
			MappingSetup setup = (MappingSetup)attachedObject;
			ui.setAttachedObject(setupDialog, setup);
			
			ui.setText(txtSourceName, setup.getName());
			ui.setText(txtSourceURL, setup.getSourceURL());
			ui.setSelected(chkSourceDefault, setup.isDefaultSetup());
			
			ui.setEnabled(btnSave, true);
			ui.setEnabled(btnDelete, true);
			ui.setEnabled(btnCancel, true);
		}
	}
	
	public void deleteMappingSource() {
		Object selectedItem = getSelectedItem(tblSources);
		Object attachedObject = getAttachedObject(selectedItem, MappingSetup.class);
		if(attachedObject instanceof MappingSetup){
			MappingSetup mappingSetup = (MappingSetup)attachedObject;
			if (mappingSetup.isDefaultSetup()) {
				incidentDao.deleteIncidentsWithMapping(mappingSetup);
				locationDao.deleteLocationsWithMapping(mappingSetup);
				categoryDao.deleteCategoriesWithMapping(mappingSetup);
			}
			mappingSetupDao.deleteMappingSetup(mappingSetup);
		}
		ui.remove(selectedItem);
		
		ui.setSelectedItem(tblSources, null);
		ui.setText(txtSourceName, "");
		ui.setText(txtSourceURL,"");
		ui.setSelected(chkSourceDefault, false);
		
		ui.setEnabled(btnSave, false);
		ui.setEnabled(btnDelete, false);
		ui.setEnabled(btnCancel, false);
		
		ui.removeConfirmationDialog();
	}
	
	public void sourceChanged(Object txtSourceName, Object txtSourceURL) {
		String sourceName = getText(txtSourceName);
		String sourceUrl = getText(txtSourceURL);
		if (sourceName != null && sourceName.length() > 0 && 
			sourceUrl != null && sourceUrl.length() > 0 && isValidUrl(sourceUrl, true)) {
			ui.setEnabled(btnSave, true);
		}
		else {
			ui.setEnabled(btnSave, false);
		}
		ui.setEnabled(btnCancel, true);
	}
	
	private boolean isValidUrl(String urlString, boolean regex) {
		if (regex) {
			return urlString.matches("^(ht|f)tp(s?)://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$");
		}
		else {
			try {
			    URL url = new URL(urlString);
			    URLConnection connection = url.openConnection();
			    connection.connect();
			    return true;
			} 
			catch(MalformedURLException e) {} 
			catch(NullPointerException e) {}
			catch(IOException e) {}
			catch(Exception e) {}
			return false;
		}
	}
	
	/**
	 * Adds a new set of mapping configurations to the database
	 * 
	 * @param dialog
	 */
	public void addMappingSource(Object dialog){
		String sourceName = getText(txtSourceName);
		if(sourceName == null || sourceName.length() == 0) {
			ui.alert(MappingMessages.getSourceNameMissing());
			LOG.debug("Invalid or empty source name");
			return;
		}
		String sourceURL = getText(txtSourceURL);
		if(sourceURL == null || sourceURL.length() == 0) {
			ui.alert(MappingMessages.getSourceUrlMissing());
			LOG.debug("Invalid or empty source url");
			return;
		}
		MappingSetup mappingSetup = null;
		Object attachedObject = getAttachedObject(dialog, MappingSetup.class);
		
		if(attachedObject instanceof MappingSetup) {
			mappingSetup = (MappingSetup)attachedObject;
			setAttachedObject(dialog,  null);
		}
		else{
			mappingSetup = new MappingSetup();
		}
		
		//Get the current default setup
		MappingSetup currentDefault = (mappingSetupDao.getDefaultSetup() != null) ? mappingSetupDao.getDefaultSetup() : null;
		mappingSetup.setName(sourceName);
		mappingSetup.setSourceURL(sourceURL);
		
		boolean sourceDefault = getBoolean(chkSourceDefault, Thinlet.SELECTED);
		if (sourceDefault) {
			shouldSynchronize = true;
			SynchronizationManager syncManager = new SynchronizationManager(this, sourceURL);	
			syncManager.downloadGeoMidpoint();
		}
		if(currentDefault == null && sourceDefault == false){
			LOG.debug("Default mapping setup not specified");
			ui.alert(MappingMessages.getSetupDefaultRequired());
			return;
		}
		mappingSetup.setDefaultSetup(sourceDefault);
		try{
			if(currentDefault != null && Long.toString(mappingSetup.getId()) != null) {
				if (mappingSetup.getId() != currentDefault.getId() && mappingSetup.isDefaultSetup()){
					currentDefault.setDefaultSetup(false);
					mappingSetupDao.updateMappingSetup(currentDefault);
					LOG.debug("Changed default mapping setup to " + mappingSetup.getName());					
				}
			}
			if(attachedObject == null){
				mappingSetupDao.saveMappingSetup(mappingSetup);				
			}
			else{
				mappingSetupDao.updateMappingSetup(mappingSetup);
			}
			ui.removeAll(tblSources);
			for(MappingSetup m : mappingSetupDao.getAllSetupItems()) {
				ui.add(tblSources, getRow(m));
			}
		}
		catch(DuplicateKeyException e){
			LOG.debug("Mapping setup parameter already exists", e);
			ui.alert(MappingMessages.getSetupMappingExists());
			return;
		}
		LOG.debug("Mapping setup parameter for [" + mappingSetup.getSourceURL() +"] created!");
				
		clearSourceFields(dialog);
		
		// If the default map setup has changed, re-initialize the keywords, incidents and map bean so as to reflect the new mapping settings
		if(currentDefault != null && currentDefault.getId() != mappingSetup.getId()){
			pluginController.showIncidentMap();
		}
	}
	
	/**
	 * Clears the input fields from the mapping setup dialog 
	 * @param dialog
	 */
	public void clearSourceFields(Object dialog){
		ui.setSelectedItem(tblSources, null);
		ui.setText(txtSourceName, "");
		ui.setText(txtSourceURL,"");
		ui.setSelected(chkSourceDefault, false);
		
		ui.setEnabled(btnSave, false);
		ui.setEnabled(btnDelete, false);
		ui.setEnabled(btnCancel, false);
	}
	
	public void createSurveyQuestions() {
		LOG.debug("createSurveyQuestions");
		OperatorManager operatorManager = new OperatorManager(frontlineController, pluginController);
        if(operatorManager.addUshahidiFields()) {
        	ui.alert(MappingMessages.getSurveyCreated());
        }
        else {
        	ui.alert(MappingMessages.getSurveyFailed());
        }
	}
	
	public void createFormFields() {
		LOG.debug("createFormFields");
        FormsManager formsManager = new FormsManager(frontlineController, pluginController);
        if(formsManager.addUshahidiForms()) {
        	ui.alert(MappingMessages.getFormCreated());
        }
        else {
        	ui.alert(MappingMessages.getFormFailed());
        }
	}

	//################# SynchronizationCallback #################
	
	public void downloadedGeoMidpoint(String domain, String latitude, String longitude) {
		LOG.debug("downloadedGeoMidpoint: %s (%s,%s)", domain, latitude, longitude);
		ui.removeAll(tblSources);
		for(MappingSetup mappingSetup : mappingSetupDao.getAllSetupItems()) {
			if (mappingSetup.isSourceURL(domain)) {
				mappingSetup.setLatitude(Double.parseDouble(latitude));
				mappingSetup.setLongitude(Double.parseDouble(longitude));
				try {
					mappingSetupDao.updateMappingSetup(mappingSetup);
				} 
				catch(DuplicateKeyException ex) {
					ex.printStackTrace();
				}
			}
			ui.add(tblSources, getRow(mappingSetup));
		}
		pluginController.showIncidentMap();
	}

	public void synchronizationFinished() {}

	public void synchronizationStarted(int tasks) {}

	public void synchronizationUpdated(int tasks, int completed) {}
	
	public void synchronizationFailed(String error) {}
	
	public void downloadedCategory(Category category) {}

	public void downloadedIncident(Incident incident) {}

	public void downloadedLocation(Location location) {}

	public void uploadedIncident(Incident incident) {}
}