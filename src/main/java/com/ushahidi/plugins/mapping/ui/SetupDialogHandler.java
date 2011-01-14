package com.ushahidi.plugins.mapping.ui;

import java.io.File;
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
import com.ushahidi.plugins.mapping.managers.TextFormsManager;
import com.ushahidi.plugins.mapping.maps.providers.MapProvider;
import com.ushahidi.plugins.mapping.maps.providers.MapProviderFactory;
import com.ushahidi.plugins.mapping.sync.SynchronizationCallback;
import com.ushahidi.plugins.mapping.sync.SynchronizationManager;
import com.ushahidi.plugins.mapping.util.FileUtils;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;
import com.ushahidi.plugins.mapping.util.MappingProperties;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.Icon;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class SetupDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler, SynchronizationCallback {

	public static MappingLogger LOG = new MappingLogger(SetupDialogHandler.class);
	
	private static final String UI_SETUP_DIALOG = "/ui/plugins/mapping/setupDialog.xml";
	
	private static final String CONFIRM_DELETE_KEY = "plugins.mapping.setup.confirm.delete";
	private static final String CONFIRM_SYNCHRONIZE_KEY = "plugins.mapping.setup.synchronize";
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final TextFormsManager textformsManager;
	private final FormsManager formsManager;
	
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final Object mainDialog;
	private final UIFields fields;
	private class UIFields extends Fields {
		public UIFields(UiGeneratorController uiController, Object parent) {
			super(uiController, parent);
		}
		public Object tblSources;
		public Object txtSourceName;
		public Object txtSourceURL;
		public Object chkSourceDefault;
		public Object btnSave;
		public Object btnDelete;
		public Object btnCancel;
		public Object btnCreateForm;
		public Object btnCreateTextForm;
		public Object txtDefaultLatitude;
		public Object txtDefaultLongitude;
		public Object cbxMapProviders;
		public Object sldDefaultZoom;
	}
	
	private SyncDialogHandler syncDialog;
	
	public SetupDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController, FormsManager formsManager, TextFormsManager textformsManager) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.formsManager = formsManager;
		this.textformsManager = textformsManager;
		
		this.locationDao = pluginController.getLocationDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainDialog = ui.loadComponentFromFile(UI_SETUP_DIALOG, this);
		this.fields = new UIFields(ui, mainDialog);
	}
	
	public void showDialog() {
		if (mappingSetupDao.getCount() > 0){
			ui.removeAll(fields.tblSources);
			for(MappingSetup setup: mappingSetupDao.getAllSetupItems()) {
				ui.add(fields.tblSources, getRow(setup));
			}
			ui.setEnabled(fields.btnCreateForm, true);
			ui.setEnabled(fields.btnCreateTextForm, true);
		}
		else {
			ui.setEnabled(fields.btnCreateForm, false);
			ui.setEnabled(fields.btnCreateTextForm, false);
		}
		ui.setText(fields.txtDefaultLatitude, MappingProperties.getDefaultLatitudeString());
		ui.setText(fields.txtDefaultLongitude, MappingProperties.getDefaultLongitudeString());
		
		ui.removeAll(fields.cbxMapProviders);
		int index = 0;
		for(MapProvider mapProvider : MapProviderFactory.getMapProviders()) {
			Object comboChoice = ui.createComboboxChoice(mapProvider.getTitle(), mapProvider);
			if (mapProvider == MappingProperties.getDefaultMapProvider()) {
				ui.setSelectedIndex(fields.cbxMapProviders, index);
			}
			index++;
			ui.add(fields.cbxMapProviders, comboChoice);
		}
		ui.setInteger(fields.sldDefaultZoom, VALUE, MappingProperties.getDefaultZoomLevel());
		ui.add(this.mainDialog);	
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	public void clearMapCache(Object button) {
		File mapsDirectory = new File(ResourceUtils.getConfigDirectoryPath(), "maps");
		if (mapsDirectory.exists()) {
			float directorySize = FileUtils.getDirectorySize(mapsDirectory);
			int filesDeleted = FileUtils.deleteFiles(mapsDirectory);
			ui.alert(String.format("%d %s : %.1f\n mb", filesDeleted, MappingMessages.getMapCacheFilesDeleted(), directorySize));	
		}
		else {
			ui.alert(String.format("0 %s", MappingMessages.getMapCacheFilesDeleted()));	
		}
	}
	
	public void beginSynchronization() {
		ui.removeConfirmationDialog();
		String sourceURL = this.mappingSetupDao.getDefaultSetup().getSourceURL();
		SynchronizationManager syncManager = new SynchronizationManager(this, sourceURL);	
		syncManager.performFullSynchronization(null);
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
			
			ui.setText(fields.txtSourceName, setup.getName());
			ui.setText(fields.txtSourceURL, setup.getSourceURL());
			ui.setSelected(fields.chkSourceDefault, setup.isDefaultSetup());
			
			ui.setEnabled(fields.btnSave, true);
			ui.setEnabled(fields.btnDelete, true);
			ui.setEnabled(fields.btnCancel, true);
		}
	}
	
	public void deleteMappingSource() {
		Object selectedItem = getSelectedItem(fields.tblSources);
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
		
		ui.setSelectedItem(fields.tblSources, null);
		ui.setText(fields.txtSourceName, "");
		ui.setText(fields.txtSourceURL,"");
		ui.setSelected(fields.chkSourceDefault, false);
		
		ui.setEnabled(fields.btnSave, false);
		ui.setEnabled(fields.btnDelete, false);
		ui.setEnabled(fields.btnCancel, false);
		
		ui.removeConfirmationDialog();
	}
	
	public void sourceChanged(Object txtSourceName, Object txtSourceURL) {
		String sourceName = getText(txtSourceName);
		String sourceUrl = getText(txtSourceURL);
		if (sourceName != null && sourceName.length() > 0 && 
			sourceUrl != null && sourceUrl.length() > 0 && isValidUrl(sourceUrl, true)) {
			ui.setEnabled(fields.btnSave, true);
		}
		else {
			ui.setEnabled(fields.btnSave, false);
		}
		ui.setEnabled(fields.btnCancel, true);
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
		String sourceName = getText(fields.txtSourceName);
		if(sourceName == null || sourceName.length() == 0) {
			ui.alert(MappingMessages.getSourceNameMissing());
			LOG.debug("Invalid or empty source name");
			return;
		}
		String sourceURL = getText(fields.txtSourceURL);
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
		
		boolean sourceDefault = getBoolean(fields.chkSourceDefault, Thinlet.SELECTED);
		if (sourceDefault) {
			ui.showConfirmationDialog("beginSynchronization", this, CONFIRM_SYNCHRONIZE_KEY);
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
			ui.removeAll(fields.tblSources);
			for(MappingSetup m : mappingSetupDao.getAllSetupItems()) {
				ui.add(fields.tblSources, getRow(m));
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
		ui.setSelectedItem(fields.tblSources, null);
		ui.setText(fields.txtSourceName, "");
		ui.setText(fields.txtSourceURL,"");
		ui.setSelected(fields.chkSourceDefault, false);
		
		ui.setEnabled(fields.btnSave, false);
		ui.setEnabled(fields.btnDelete, false);
		ui.setEnabled(fields.btnCancel, false);
	}
	
	public void createTextFormQuestions() {
		LOG.debug("createTextFormQuestions");
		if(textformsManager.addTextFormQuestions()) {
			ui.setStatus(MappingMessages.getTextFormCreated());
        	ui.alert(MappingMessages.getTextFormCreated());
        }
        else {
        	ui.alert(MappingMessages.getTextFormFailed());
        }
	}
	
	public void createFormFields() {
		LOG.debug("createFormFields");
		if(formsManager.addFormFields()) {
			ui.setStatus(MappingMessages.getFormCreated());
        	ui.alert(MappingMessages.getFormCreated());
        }
        else {
        	ui.alert(MappingMessages.getFormFailed());
        }
	}

	public void latitudeLongitudeChanged(Object txtLatitude, Object txtLongitude) {
		String latitude = ui.getText(txtLatitude); 
		String longitude = ui.getText(txtLongitude); 
		LOG.debug("latitude:%s longitude:%s", latitude, longitude);
		MappingProperties.setDefaultLatitude(latitude);
		MappingProperties.setDefaultLongitude(longitude);
		pluginController.showIncidentMap();
	}
	
	public void mapProviderChanged(Object comboBox) {
		Object selectedItem = ui.getSelectedItem(comboBox);
		if (selectedItem != null) {
			MapProvider mapProvider = ui.getAttachedObject(selectedItem, MapProvider.class);
			if (mapProvider != null) {
				LOG.debug("MapProvider: %s", mapProvider.getTitle());
				MappingProperties.setDefaultMapProvider(mapProvider);
				pluginController.showIncidentMap();
			}
		}
	}
	
	public void zoomChanged(Object sldZoomLevel) {
		int zoomLevel = getInteger(sldZoomLevel, ExtendedThinlet.VALUE);
		LOG.debug("zoom:%d", zoomLevel);
		MappingProperties.setDefaultZoomLevel(zoomLevel);
		pluginController.showIncidentMap();
	}
	
	//################# SynchronizationCallback #################
	
	public void downloadedGeoMidpoint(String domain, String latitude, String longitude) {
		LOG.debug("downloadedGeoMidpoint: %s (%s,%s)", domain, latitude, longitude);
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
		}
	}

	public void synchronizationFinished() {
		boolean hasMappingSetup = mappingSetupDao.getCount() > 0;
		ui.setEnabled(fields.btnCreateForm, hasMappingSetup);
		ui.setEnabled(fields.btnCreateTextForm, hasMappingSetup);
		syncDialog.hideDialog();
		pluginController.showIncidentMap();
	}

	public void synchronizationStarted(int tasks) {
		if (syncDialog == null) {
			syncDialog = new SyncDialogHandler(pluginController, frontlineController, ui);	
		}
		syncDialog.setProgress(tasks, 1);
		syncDialog.showDialog();
	}

	public void synchronizationUpdated(int tasks, int completed) {
		syncDialog.setProgress(tasks, completed);	
	}
	
	public void synchronizationFailed(String error) {
		boolean hasMappingSetup = mappingSetupDao.getCount() > 0;
		ui.setEnabled(fields.btnCreateForm, hasMappingSetup);
		ui.setEnabled(fields.btnCreateTextForm, hasMappingSetup);
		syncDialog.hideDialog();
		this.ui.alert(error);
	}
	
	public void downloadedCategory(Category category) {
		LOG.debug("downloadedCategory:%s", category);
		if (category != null) {
			category.setMappingSetup(mappingSetupDao.getDefaultSetup());
			try{
				categoryDao.saveCategory(category);			
			}
			catch(DuplicateKeyException e){
				LOG.debug("Category already exists: %s", e);
			}	
		}
	}

	public void downloadedIncident(Incident incident) {
		LOG.debug("downloadedIncident:%s", incident);
		if (incident != null) {
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
				LOG.debug("saveIncident...");
				incidentDao.saveIncident(incident);
			} 
			catch (DuplicateKeyException e) {
				LOG.debug("Incident already exists: %s", e);
			}	
		}
	}

	public void downloadedLocation(Location location) {
		LOG.debug("downloadedLocation:%s", location);
		if (location != null) {
			location.setMappingSetup(mappingSetupDao.getDefaultSetup());
			try{
				locationDao.saveLocation(location);
			}
			catch(DuplicateKeyException e){			
				LOG.debug("Location already exists: %s", e);
			}			
		}
	}

	public void uploadedIncident(Incident incident) {}
	
	public void failedIncident(Incident incident) {}
}