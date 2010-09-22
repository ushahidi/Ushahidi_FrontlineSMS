package com.ushahidi.plugins.mapping.ui;

import java.io.File;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.LocationDetails;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.ui.markers.IncidentMarker;
import com.ushahidi.plugins.mapping.ui.markers.MessageMarker;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;
import com.ushahidi.plugins.mapping.util.MappingProperties;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.data.domain.FrontlineMessage.Status;
import net.frontlinesms.data.domain.FrontlineMessage.Type;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.data.repository.MessageDao;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class MapPanelHandler extends ExtendedThinlet implements ThinletUiEventHandler, MapListener {

	private static final String UI_PANEL_XML = "/ui/plugins/mapping/mapPanel.xml";
	
	private static MappingLogger LOG = MappingLogger.getLogger(MapPanelHandler.class);	
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final Object mainPanel;
	
	private final IncidentDao incidentDao;
	private final CategoryDao categoryDao;
	private final MessageDao messageDao;
	private final ContactDao contactDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final MapBean mapBean;
	private final Object lblCoordinates;
	private final Object sldZoomLevel;
	private final Object cbxCategories;
	private final Object cbxShowMessages;
	private final Object cbxShowForms;
	private final Object cbxShowSurveys;
	private final Object cbxShowIncidents;
	
	public MapPanelHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.incidentDao = pluginController.getIncidentDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.messageDao = pluginController.getMessageDao();
		this.contactDao = pluginController.getContactDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainPanel = this.ui.loadComponentFromFile(UI_PANEL_XML, this);
		this.mapBean = (MapBean)get(this.find(this.mainPanel, "mapBean"), BEAN);
		this.lblCoordinates = this.ui.find(this.mainPanel, "lblCoordinates");
		this.sldZoomLevel = this.ui.find(this.mainPanel, "sldZoomLevel");
		this.cbxCategories = this.ui.find(this.mainPanel, "cbxCategories");
		this.cbxShowMessages = this.ui.find(this.mainPanel, "cbxShowMessages");
		this.cbxShowForms = this.ui.find(this.mainPanel, "cbxShowForms");
		this.cbxShowSurveys = this.ui.find(this.mainPanel, "cbxShowSurveys");
		this.cbxShowIncidents = this.ui.find(this.mainPanel, "cbxShowIncidents");
	}
	
	public Object getMainPanel() {
		return this.mainPanel;
	}
	
	public void init() {
		ui.removeAll(cbxCategories);
		mapBean.setMapProvider(MappingProperties.getDefaultMapProvider());
		if(mappingSetupDao.getDefaultSetup() != null) {
			MappingSetup defaultSetup = mappingSetupDao.getDefaultSetup();
			double latitude = defaultSetup.getLatitude();
			double longitude = defaultSetup.getLongitude();
			LOG.debug("Default Setup: " + defaultSetup.getSourceURL());
			if(mappingSetupDao.getDefaultSetup().isOffline()){				
				String fileName = defaultSetup.getOfflineMapFile();
				File file = new File(fileName);
				if (file.exists()) {
					mapBean.setOfflineMapFile(fileName);
				}
				else {
					defaultSetup.setOffline(false);
					defaultSetup.setOfflineMapFile(null);
					try{
						mappingSetupDao.updateMappingSetup(defaultSetup);
					}
					catch(DuplicateKeyException de) {
						LOG.debug(de);
						ui.alert("Unable to update the map setup");
					}					
				}
			}
			mapBean.setLocationAndZoomLevel(longitude, latitude, MappingProperties.getDefaultZoomLevel());			
			mapBean.clearMarkers(false);
			for(Incident incident : incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())) {
				mapBean.addMarker(new IncidentMarker(incident), false);
			}
			mapBean.addMapListener(this);
			ui.setEnabled(sldZoomLevel, true);
			ui.add(cbxCategories, createComboboxChoice(MappingMessages.getAllCategories(), null));
			for(Category category : categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
				LOG.debug("Loading category %s", category.getTitle());
				ui.add(cbxCategories, createComboboxChoice(category.getTitle(), category));
			}
			ui.setSelectedIndex(cbxCategories, 0);
			ui.setEnabled(cbxCategories, true);
			if (getBoolean(cbxShowIncidents, Thinlet.ENABLED) == false) {
				ui.setSelected(cbxShowIncidents, true);
			}
			ui.setEnabled(cbxShowIncidents, true);
		} 
		else {
			double latitude = MappingProperties.getDefaultLatitude();
			double longitude = MappingProperties.getDefaultLongitude();
			mapBean.setLocationAndZoomLevel(longitude, latitude, MappingProperties.getDefaultZoomLevel());			
			mapBean.addMapListener(this);
			ui.setEnabled(sldZoomLevel, true);
			ui.setEnabled(cbxCategories, false);
			ui.setEnabled(cbxShowIncidents, false);
			ui.setSelected(cbxShowIncidents, false);
		}
		ui.setInteger(sldZoomLevel, VALUE, MappingProperties.getDefaultZoomLevel());
	}
	
	public void refresh() {
		LOG.debug("MapPanelHandler.refresh");
		if(mappingSetupDao.getDefaultSetup() != null) {
			if (getBoolean(cbxShowIncidents, Thinlet.ENABLED) == false) {
				ui.setSelected(cbxShowIncidents, true);
			}
			ui.setEnabled(cbxShowIncidents, true);
		}
		else {
			ui.setEnabled(cbxShowIncidents, false);
			ui.setSelected(cbxShowIncidents, false);
		}
		if (mapBean != null) {
			mapBean.clearMarkers(false);
			if(getBoolean(cbxShowMessages, Thinlet.SELECTED)) {
				LOG.debug("Showing Messages");
				for(FrontlineMessage message : messageDao.getMessages(Type.RECEIVED, Status.RECEIVED)) {
					Contact contact = contactDao.getFromMsisdn(message.getSenderMsisdn());
					if (contact != null) {
						LocationDetails locationDetails = contact.getDetails(LocationDetails.class);
						if (locationDetails != null && locationDetails.getLocation() != null) {
							mapBean.addMarker(new MessageMarker(message, locationDetails.getLocation()), false);
						}
					}
				}
			}
			if(getBoolean(cbxShowSurveys, Thinlet.SELECTED)) {
				LOG.debug("Showing Surveys");	
			}
			if(getBoolean(cbxShowForms, Thinlet.SELECTED)) {
				LOG.debug("Showing Forms");
			}
			if(getBoolean(cbxShowIncidents, Thinlet.SELECTED)) {
				LOG.debug("Showing Incidents");
				for(Incident incident : incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())) {
					mapBean.addMarker(new IncidentMarker(incident), false);
				}	
			}
			mapBean.repaint();
		}
	}
	
	public void destroyMap() {
		if (mapBean != null) {
			mapBean.destroyMap();
		}
	}
	
	public void addMapListener(MapListener listener) {
		mapBean.addMapListener(listener);
	}
	
	public void search(Object comboBox) {
		Object selectedItem =  getSelectedItem(comboBox);
		Category category = selectedItem != null ? getAttachedObject(selectedItem, Category.class) : null;
		LOG.debug("category=%s", category);
		mapBean.clearMarkers(false);
		for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
			if (category == null || incident.hasCategory(category)) {
				mapBean.addMarker(new IncidentMarker(incident), false);
			}
		}
		mapBean.repaint();
	}
	
	/** @see {@link MapListener#mapZoomed(int)} */
	public void zoomChanged(int zoom){
	    LOG.info("Updating zoom controller to level " + zoom);
	    ui.setInteger(sldZoomLevel, VALUE, zoom);
	}
	
	/**
	 * Changes the zoom level of the map
	 * 
	 * @param zoomController The Zoom UI control
	 */
	public void zoomChanged(Object zoomController){
		int currentZoom = mapBean.getZoomLevel();		
		int zoomValue = getInteger(zoomController, ExtendedThinlet.VALUE);
		if(currentZoom < zoomValue){
			ui.setInteger(zoomController, ExtendedThinlet.VALUE, zoomValue - 1);
		}
		else if (currentZoom > zoomValue){
			ui.setInteger(zoomController, ExtendedThinlet.VALUE, zoomValue + 1);
		}
		mapBean.setZoomLevel(zoomValue);
	}
	
	/**
	 * Show the map save dialog
	 */
	public void saveMap() {
		MapSaveDialogHandler mapSaveDialog = new MapSaveDialogHandler(pluginController, frontlineController, ui);
		mapSaveDialog.showDialog(mapBean);
	}

	public void locationHovered(double latitude, double longitude) {
		String latString = Double.toString(latitude);
		if (latString.length() > 8) {
			latString = latString.substring(0,8);
		}
		String lonString = Double.toString(longitude);
		if (lonString.length() > 8) {
			lonString = lonString.substring(0,8);
		}
		ui.setText(lblCoordinates, latString + ", " + lonString);
	}

	public void locationSelected(double latitude, double longitude) {
		LOG.debug("Latitude:%f Longitude:%f", latitude, longitude);
	}
	
}