package com.ushahidi.plugins.mapping.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.utils.MappingLogger;
import com.ushahidi.plugins.mapping.utils.MappingMessages;
import com.ushahidi.plugins.mapping.utils.MappingProperties;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
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
	private final MappingSetupDao mappingSetupDao;
	
	private final MapBean mapBean;
	private final Object lblCoordinates;
	private final Object sldZoomLevel;
	private final Object cbxCategories;
	
	public MapPanelHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.incidentDao = pluginController.getIncidentDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainPanel = this.ui.loadComponentFromFile(UI_PANEL_XML, this);
		this.mapBean = (MapBean)get(this.find(this.mainPanel, "mapBean"), BEAN);
		this.lblCoordinates = this.ui.find(this.mainPanel, "lblCoordinates");
		this.sldZoomLevel = this.ui.find(this.mainPanel, "sldZoomLevel");
		this.cbxCategories = this.ui.find(this.mainPanel, "cbxCategories");
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
			mapBean.setIncidents(incidentDao.getAllIncidents(defaultSetup));
			mapBean.addMapListener(this);
			mapBean.setMapPanelHandler(this);
			ui.setEnabled(sldZoomLevel, true);
			ui.add(cbxCategories, createComboboxChoice(MappingMessages.getAllCategories(), null));
			for(Category category : categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
				LOG.debug("Loading category %s", category.getTitle());
				ui.add(cbxCategories, createComboboxChoice(category.getTitle(), category));
			}
			ui.setSelectedIndex(cbxCategories, 0);
			ui.setEnabled(cbxCategories, true);
		} 
		else {
			double latitude = MappingProperties.getDefaultLatitude();
			double longitude = MappingProperties.getDefaultLongitude();
			mapBean.setLocationAndZoomLevel(longitude, latitude, MappingProperties.getDefaultZoomLevel());			
			mapBean.addMapListener(this);
			mapBean.setMapPanelHandler(this);
			ui.setEnabled(sldZoomLevel, true);
			ui.setEnabled(cbxCategories, false);
		}
		ui.setInteger(sldZoomLevel, VALUE, MappingProperties.getDefaultZoomLevel());
	}
	
	public void refresh() {
		LOG.debug("MapPanelHandler.refresh");
		if (mapBean != null) {
			mapBean.setIncidents(incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup()));	
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
		List<Incident> incidents = new ArrayList<Incident>();
		for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
			if (category == null || incident.hasCategory(category)) {
				incidents.add(incident);
			}
		}
		mapBean.setIncidents(incidents);
	}
	
	/**
	 * Fired by {@link MapListener} when a point is selected on the map; the incident creation
	 * dialog is displayed with the coordinates of the selected location
	 */
	public void pointSelected(double lat, double lon) {
		LOG.debug("%f, %f", lat, lon);		
	}
	
	/** @see {@link MapListener#mapZoomed(int)} */
	public void mapZoomed(int zoom){
	    LOG.info("Updating zoom controller to level " + zoom);
	    ui.setInteger(sldZoomLevel, VALUE, zoom);
	}
	
	/**
	 * Changes the zoom level of the map
	 * 
	 * @param zoomController The Zoom UI control
	 */
	public void zoomMap(Object zoomController){
		int currentZoom = mapBean.getZoomLevel();		
		int zoomValue = getInteger(zoomController, ExtendedThinlet.VALUE);
		// Adjust the zooming bar so that it moves in steps of 1 only
		if(currentZoom < zoomValue){
			ui.setInteger(zoomController, ExtendedThinlet.VALUE, zoomValue - 1);
		}
		else if (currentZoom > zoomValue){
			ui.setInteger(zoomController, ExtendedThinlet.VALUE, zoomValue + 1);
		}
		mapBean.setZoomLevel(zoomValue);
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
		String latString = Double.toString(lat);
		if (latString.length() > 8) {
			latString = latString.substring(0,8);
		}
		String lonString = Double.toString(lon);
		if (lonString.length() > 8) {
			lonString = lonString.substring(0,8);
		}
		ui.setText(lblCoordinates, latString + ", " + lonString);
	}
	
	/**
	 * Show the map save dialog
	 */
	public void saveMap() {
		MapSaveDialogHandler mapSaveDialog = new MapSaveDialogHandler(pluginController, frontlineController, ui);
		mapSaveDialog.showDialog(mapBean);
	}
	
}