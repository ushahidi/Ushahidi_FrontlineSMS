package com.ushahidi.plugins.mapping.ui;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.maps.TileSaver;
import com.ushahidi.plugins.mapping.maps.TiledMap;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.FrontlineUtils;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.DateSelecter;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class MapPanelHandler extends ExtendedThinlet implements ThinletUiEventHandler, MapListener {

	private static final String UI_PANEL_XML = "/ui/plugins/mapping/mapPanel.xml";
	
	public static Logger LOG = FrontlineUtils.getLogger(MapPanelHandler.class);	
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private Object mainPanel;
	
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final MapBean mapBean;
	private final Object lblCoordinates;
	private final Object sldZoomController;
	
	public MapPanelHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainPanel = this.ui.loadComponentFromFile(UI_PANEL_XML, this);
		this.mapBean = (MapBean)get(this.find(this.mainPanel, "mapBean"), BEAN);
		this.lblCoordinates = this.find(this.mainPanel, "lblCoordinates");
		this.sldZoomController = this.find(this.mainPanel, "sldZoomController");
	}
	
	public Object getMainPanel() {
		return this.mainPanel;
	}
	
	public void init() {
		if(mappingSetupDao.getDefaultSetup() != null) {
			//Get the default mapping setup
			MappingSetup defaultSetup = mappingSetupDao.getDefaultSetup();
			
			// Get the latitude and longitude
			double latitude = defaultSetup.getLatitude();
			double longitude = defaultSetup.getLongitude();
			
			LOG.debug("Default Setup: " + defaultSetup.getSourceURL());
			
			//Check if offline mode for the default setup is enabled
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
			// Get the current zoom level from the slider
			//int zoomLevel = getInteger(getZoomController(), ExtendedThinlet.VALUE);
			
			//mapBean.setZoomLevel(zoomLevel);
			mapBean.setLocation(longitude, latitude);			
			mapBean.setIncidents(incidentDao.getAllIncidents(defaultSetup));
			mapBean.addMapListener(this);
			mapBean.setMapPanelHandler(this);
		} 
		else {
			//The mapping plugin has not been configured; therefore disable the zoom controller
			setEnabled(this.sldZoomController, false);
		}
	}
	
	/**
	 * Fired by {@link MapListener} when a point is selected on the map; the incident creation
	 * dialog is displayed with the coordinates of the selected location
	 */
	public void pointSelected(double lat, double lon) {
		//Show the selected point and the text field for the name to be input
		//setBoolean(ui.find(dialog, COMPONENT_LOCATION_NAME_FIELD), Thinlet.ENABLED , true);
//		setText(ui.find(this.mainPanel, COMPONENT_LBL_SELECTED_LATITUDE), Double.toString(lat));
//		setText(ui.find(this.mainPanel, COMPONENT_LBL_SELECTED_LONGITUDE), Double.toString(lon));
//		setBoolean(ui.find(this.mainPanel, COMPONENT_LOCATIONS_COMBO), Thinlet.ENABLED, false);		

//		setVisible(this.mainPanel, true);
//		setBoolean(this.mainPanel, Thinlet.MODAL, true);
		
		ui.repaint();		
	}
	
	/** @see {@link MapListener#mapZoomed(int)} */
	public void mapZoomed(int zoom){
	    LOG.info("Updating zoom controller to level " + zoom);
	    // Update the zoom slider to reflect the current zoom level
	    setInteger(this.sldZoomController, VALUE, zoom);
	    ui.repaint();
	}
	
	/**
	 * Changes the zoom level of the map
	 * 
	 * @param zoomController The Zoom UI control
	 */
	public void zoomMap(Object zoomController){
		int currentZoom = mapBean.getCurrentZoomLevel();		
		int zoomVal = getInteger(zoomController, ExtendedThinlet.VALUE);
		
		// Adjust the zooming bar so that it moves in steps of 1 only
		if(currentZoom < zoomVal){
			setInteger(zoomController, ExtendedThinlet.VALUE, zoomVal-1);
			ui.repaint();
		}else if (currentZoom > zoomVal){
			setInteger(zoomController, ExtendedThinlet.VALUE, zoomVal + 1);
			ui.repaint();
		}
		
		mapBean.zoomMap(zoomVal);
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
		setText(this.lblCoordinates, lonStr+", "+latStr);
		ui.repaint(this.lblCoordinates);
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
	
	public void startPointSelection(Object dialog) {
		this.mapBean.addMapListener(this);		
		setBoolean(dialog, Thinlet.MODAL, false);
		setVisible(dialog, false);
		
		//Force Thinlet to hide the dialog now
		ui.repaint();
	}
	
	/**
	 * Show the map save dialog
	 */
	public void saveMap() {
		MapSaveDialogHandler mapSaveDialog = new MapSaveDialogHandler(this.pluginController, this.frontlineController, this.ui);
		mapSaveDialog.showDialog(this.mapBean);
	}
	
	public void incidentFilterDateChanged() {		
	}
	
	public void showClusteredData () {
		System.out.println("showClusteredData");
	}
	
	public void showPointData() {
		System.out.println("showPointData");
	}
	
	public void fromDateChanged(Object textField) {
		System.out.println("fromDateChanged: " + this.ui.getText(textField));
	}
	
	public void toDateChanged(Object textField) {
		System.out.println("toDateChanged: " + this.ui.getText(textField));
	}
}