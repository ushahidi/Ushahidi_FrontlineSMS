package com.ushahidi.plugins.mapping.ui;

import thinlet.Thinlet;
import thinlet.ThinletText;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
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
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final MappingSetupDao mappingSetupDao;
	
	private final Object mainDialog;
	private final Object tblSources;
	private final Object txtSourceName;
	private final Object txtSourceURL;
	private final Object chkSourceDefault;
	private final Object btnSave;
	private final Object btnDelete;
	private final Object btnCancel;
	
	private SyncDialogHandler syncDialog;
	
	public SetupDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		this.mainDialog = this.ui.loadComponentFromFile(UI_SETUP_DIALOG, this);
		
		this.tblSources = this.find(this.mainDialog, "tblSources");
		this.txtSourceName = this.find(this.mainDialog, "txtSourceName");
		this.txtSourceURL = this.find(this.mainDialog, "txtSourceURL");
		this.chkSourceDefault = this.find(this.mainDialog, "chkSourceDefault");
		
		this.btnSave = this.find(this.mainDialog, "btnSave");
		this.btnDelete = this.find(this.mainDialog, "btnDelete");
		this.btnCancel = this.find(this.mainDialog, "btnCancel");
	}
	
	public void showDialog() {
		if (mappingSetupDao.getCount() > 0){
			this.removeAll(tblSources);
			for(MappingSetup setup: mappingSetupDao.getAllSetupItems()) {
				add(tblSources, getRow(setup));
			}
		}
		ui.add(this.mainDialog);	
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
			this.setIcon(cell, Icon.TICK);
			this.setChoice(cell, ThinletText.ALIGNMENT, ThinletText.CENTER);
			this.add(row, cell);
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
		Object item = getAttachedObject(getSelectedItem(tblLocationSource));
		if(item instanceof MappingSetup){
			MappingSetup setup = (MappingSetup)item;
			
			//Attach the setup item to the dialog
			setAttachedObject(setupDialog, setup);
			
			setText(this.txtSourceName, setup.getName());
			setText(this.txtSourceURL, setup.getSourceURL());
			setSelected(this.chkSourceDefault, setup.isDefaultSetup());
			
			setEnabled(this.btnSave, true);
			setEnabled(this.btnDelete, true);
			setEnabled(this.btnCancel, true);
			
			ui.repaint();
		}
	}
	
	public void deleteMappingSource() {
		Object item = getAttachedObject(getSelectedItem(this.tblSources));
		if(item instanceof MappingSetup){
			
		}
	}
	
	public void saveMappingSource() {
		
	}
	
	private void obtainSourceMidPoint(String url) {
		LOG.debug("obtainSourceMidPoint:%s", url);
		SynchronizationManager syncManager = new SynchronizationManager(this, url);	
		syncManager.downloadGeoMidpoint();
	}
	
	/**
	 * Adds a new set of mapping configurations to the database
	 * 
	 * @param dialog
	 */
	public void addMappingSource(Object dialog){
		String sourceName = getText(this.txtSourceName);
		String sourceURL = getText(this.txtSourceURL);
		
		if(sourceName == null || sourceName.length() == 0) {
			ui.alert(MappingMessages.getSourceNameMissing());
			LOG.debug("Invalid or empty source name");
			return;
		}
		
		if(sourceURL == null || sourceURL.length() == 0) {
			ui.alert(MappingMessages.getSourceUrlMissing());
			LOG.debug("Invalid or empty source url");
			return;
		}
		
		MappingSetup setup = null;
		Object attachedObject = getAttachedObject(dialog);
		
		if(attachedObject instanceof MappingSetup) {
			setup = (MappingSetup)attachedObject;
			setAttachedObject(dialog,  null);
		}
		else{
			setup = new MappingSetup();
			obtainSourceMidPoint(sourceURL);
		}
		
		//Get the current default setup
		MappingSetup currentDefault = (mappingSetupDao.getDefaultSetup() != null) ? mappingSetupDao.getDefaultSetup() : null;
		
		//Set the properties for the mapping setup
		setup.setName(sourceName);
		setup.setSourceURL(sourceURL);
		
		boolean sourceDefault = getBoolean(this.chkSourceDefault, Thinlet.SELECTED);
		if(currentDefault != null && mappingSetupDao.getCount() == 1 && !sourceDefault){
			LOG.debug("There must be a default configurartion for Mapping");
			ui.alert("There is only one configuration for Mapping ["+setup.getSourceURL()+"] " + "and it must be set as the default");	
			return;
		}
		//Check for attempts to save without specifying a default mapping configuration
		if( (currentDefault != null && mappingSetupDao.getCount() > 1 && 
				setup.getId() == currentDefault.getId() && !sourceDefault) || (currentDefault == null && !sourceDefault)){
			LOG.debug("Default mapping setup not specified");
			ui.alert("There must be a default configuration for Mapping to work");
			return;
		}
		//Set the active flag for the mapping setup 
		setup.setDefaultSetup(sourceDefault);
		try{
			if(currentDefault != null && Long.toString(setup.getId()) != null) {
				if (setup.getId() != currentDefault.getId() && setup.isDefaultSetup()){
					currentDefault.setDefaultSetup(false);
					mappingSetupDao.updateMappingSetup(currentDefault);
					LOG.debug("Changed default mapping setup to " + setup.getName());					
				}
			}
			if(attachedObject == null){
				mappingSetupDao.saveMappingSetup(setup);				
				add(this.tblSources, getRow(setup));
			}
			else{
				mappingSetupDao.updateMappingSetup(setup);
			}
			removeAll(this.tblSources);
			for(MappingSetup s: mappingSetupDao.getAllSetupItems()) {
				add(this.tblSources, getRow(s));
			}
		}
		catch(DuplicateKeyException e){
			LOG.debug("Mapping setup parameter already exists", e);
			ui.alert("Mapping setup parameter already exists");
			LOG.trace("EXIT");
			return;
		}
		LOG.debug("Mapping setup parameter for [" + setup.getSourceURL() +"] created!");
				
		ui.repaint();
		clearSourceFields(dialog);
		
		// If the default map setup has changed, re-initialize the keywords, incidents and map bean 
		// so as to reflect the new mapping settings
		if(currentDefault != null && currentDefault.getId() != setup.getId()){
			this.pluginController.showIncidentMap();
		}
	}
	
	/**
	 * Clears the input fields from the mapping setup dialog 
	 * @param dialog
	 */
	public void clearSourceFields(Object dialog){
		setSelectedItem(this.tblSources, null);
		setText(this.txtSourceName, "");
		setText(this.txtSourceURL,"");
		setBoolean(this.chkSourceDefault, Thinlet.SELECTED, false);
		
		setEnabled(this.btnSave, false);
		setEnabled(this.btnDelete, false);
		setEnabled(this.btnCancel, false);
		
		ui.repaint();
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	public void createSurveyQuestions() {
		LOG.debug("createSurveyQuestions");
		OperatorManager operatorManager = new OperatorManager(this.frontlineController, this.pluginController);
        if(operatorManager.addUshahidiFields()) {
        	this.ui.alert(MappingMessages.getSurveyCreated());
        }
        else {
        	this.ui.alert(MappingMessages.getSurveyFailed());
        }
	}
	
	public void createFormFields() {
		LOG.debug("createFormFields");
        FormsManager formsManager = new FormsManager(this.frontlineController, this.pluginController);
        if(formsManager.addUshahidiForms()) {
        	this.ui.alert(MappingMessages.getFormCreated());
        }
        else {
        	this.ui.alert(MappingMessages.getFormFailed());
        }
	}

	//################# SynchronizationCallback #################
	
	public void downloadedGeoMidpoint(String domain, String latitude, String longitude) {
		LOG.debug("downloadedGeoMidpoint: %s (%s,%s)", domain, latitude, longitude);
		syncDialog.hideDialog();
		removeAll(this.tblSources);
		for(MappingSetup setup : mappingSetupDao.getAllSetupItems()) {
			if (setup.getSourceURL().equalsIgnoreCase(domain)) {
				setup.setLatitude(Double.parseDouble(latitude));
				setup.setLongitude(Double.parseDouble(longitude));
				try {
					mappingSetupDao.updateMappingSetup(setup);
				} 
				catch (DuplicateKeyException e) {
					e.printStackTrace();
				}
			}
			add(this.tblSources, getRow(setup));
		}
	}

	public void synchronizationFinished() { 
		LOG.debug("synchronizationFinished");
		syncDialog.hideDialog();
	}

	public void synchronizationStarted(int tasks) {
		LOG.debug("synchronizationStarted");
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
	
	public void synchronizationFailed(String error) { 
		LOG.debug("synchronizationFailed:%s", error);
		syncDialog.hideDialog();
		this.ui.alert(error);
	}
	
	public void downloadedCategory(Category category) {}

	public void downloadedIncident(Incident incident) {}

	public void downloadedLocation(Location location) {}

	public void uploadedIncident(Incident incident) {}
}