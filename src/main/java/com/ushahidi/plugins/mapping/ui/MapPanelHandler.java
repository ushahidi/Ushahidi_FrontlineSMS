package com.ushahidi.plugins.mapping.ui;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.LocationDetails;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.ui.markers.FormMarker;
import com.ushahidi.plugins.mapping.ui.markers.IncidentMarker;
import com.ushahidi.plugins.mapping.ui.markers.Marker;
import com.ushahidi.plugins.mapping.ui.markers.MessageMarker;
import com.ushahidi.plugins.mapping.ui.markers.TextFormMarker;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;
import com.ushahidi.plugins.mapping.util.MappingProperties;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.data.domain.FrontlineMessage.Status;
import net.frontlinesms.data.domain.FrontlineMessage.Type;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.data.repository.MessageDao;
import net.frontlinesms.plugins.forms.data.domain.FormResponse;
import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;
import net.frontlinesms.plugins.textforms.data.domain.TextFormResponse;
import net.frontlinesms.plugins.textforms.data.repository.TextFormResponseDao;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class MapPanelHandler extends ExtendedThinlet implements ThinletUiEventHandler, MapListener {

	private static final String UI_PANEL_XML = "/ui/plugins/mapping/mapPanel.xml";
	
	private static MappingLogger LOG = new MappingLogger(MapPanelHandler.class);	
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final IncidentDao incidentDao;
	private final CategoryDao categoryDao;
	private final MessageDao messageDao;
	private final ContactDao contactDao;
	private TextFormResponseDao textformResponseDao;
	private FormResponseDao formResponseDao;
	private final MappingSetupDao mappingSetupDao;
	private final MapBean mapBean;
	
	private final Object mainPanel;
	private final UIFields fields;
	private class UIFields extends Fields {
		public UIFields(UiGeneratorController uiController, Object parent) {
			super(uiController, parent);
		}
		public Object lblCoordinates;
		public Object sldZoomLevel;
		public Object cbxCategories;
		public Object cbxShowMessages;
		public Object cbxShowForms;
		public Object cbxShowTextForms;
		public Object cbxShowIncidents;
	}
	
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
		this.fields = new UIFields(ui, mainPanel);
	}
	
	public Object getMainPanel() {
		return this.mainPanel;
	}
	
	public void init() {
		ui.removeAll(fields.cbxCategories);
		mapBean.setMapProvider(MappingProperties.getDefaultMapProvider());
		if(mappingSetupDao.getDefaultSetup() != null) {
			MappingSetup defaultSetup = mappingSetupDao.getDefaultSetup();
			double latitude = defaultSetup.getLatitude();
			double longitude = defaultSetup.getLongitude();
			LOG.debug("Default Setup: " + defaultSetup.getSourceURL());
			mapBean.setLocationAndZoomLevel(longitude, latitude, MappingProperties.getDefaultZoomLevel());			
			mapBean.clearMarkers(false);
			for(Incident incident : incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())) {
				mapBean.addMarker(new IncidentMarker(incident), false);
			}
			mapBean.addMapListener(this);
			ui.setEnabled(fields.sldZoomLevel, true);
			ui.add(fields.cbxCategories, createComboboxChoice(MappingMessages.getAllCategories(), null));
			for(Category category : categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
				LOG.debug("Loading category %s", category.getTitle());
				ui.add(fields.cbxCategories, createComboboxChoice(category.getTitle(), category));
			}
			ui.setSelectedIndex(fields.cbxCategories, 0);
			ui.setEnabled(fields.cbxCategories, true);
			if (getBoolean(fields.cbxShowIncidents, Thinlet.ENABLED) == false) {
				ui.setSelected(fields.cbxShowIncidents, true);
			}
			ui.setEnabled(fields.cbxShowIncidents, true);
		} 
		else {
			double latitude = MappingProperties.getDefaultLatitude();
			double longitude = MappingProperties.getDefaultLongitude();
			mapBean.setLocationAndZoomLevel(longitude, latitude, MappingProperties.getDefaultZoomLevel());			
			mapBean.addMapListener(this);
			ui.setEnabled(fields.sldZoomLevel, true);
			ui.setEnabled(fields.cbxCategories, false);
			ui.setEnabled(fields.cbxShowIncidents, false);
			ui.setSelected(fields.cbxShowIncidents, false);
		}
		ui.setInteger(fields.sldZoomLevel, VALUE, MappingProperties.getDefaultZoomLevel());
	}
	
	/**
	 * Refresh map and markers
	 */
	public void refresh() {
		LOG.debug("MapPanelHandler.refresh");
		if(mappingSetupDao.getDefaultSetup() != null) {
			if (getBoolean(fields.cbxShowIncidents, Thinlet.ENABLED) == false) {
				ui.setSelected(fields.cbxShowIncidents, true);
			}
			ui.setEnabled(fields.cbxShowIncidents, true);
		}
		else {
			ui.setEnabled(fields.cbxShowIncidents, false);
			ui.setSelected(fields.cbxShowIncidents, false);
		}
		if (mapBean != null) {
			mapBean.clearMarkers(false);
			if(getBoolean(fields.cbxShowMessages, Thinlet.SELECTED) && contactDao != null) {
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
			if(getBoolean(fields.cbxShowTextForms, Thinlet.SELECTED) && textformResponseDao != null) {
				LOG.debug("Showing TextForms");	
				for(TextFormResponse textformResponse : textformResponseDao.getAllTextFormResponses()) {
					Contact contact = textformResponse.getContact();
					if (contact != null) {
						LocationDetails locationDetails = contact.getDetails(LocationDetails.class);
						if (locationDetails != null && locationDetails.getLocation() != null) {
							mapBean.addMarker(new TextFormMarker(textformResponse, locationDetails.getLocation()), false);
						}
					}
				}		
			}
			if(getBoolean(fields.cbxShowForms, Thinlet.SELECTED) && formResponseDao != null) {
				LOG.debug("Showing Forms");
				for(FormResponse formResponse : formResponseDao.getAllFormResponses()) {
					Contact contact = contactDao.getFromMsisdn(formResponse.getSubmitter());
					if (contact != null) {
						LocationDetails locationDetails = contact.getDetails(LocationDetails.class);
						if (locationDetails != null && locationDetails.getLocation() != null) {
							mapBean.addMarker(new FormMarker(formResponse, locationDetails.getLocation(), formResponse.getParentForm()), false);
						}
					}
				}
			}
			if(getBoolean(fields.cbxShowIncidents, Thinlet.SELECTED) && incidentDao != null) {
				LOG.debug("Showing Incidents");
				for(Incident incident : incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())) {
					mapBean.addMarker(new IncidentMarker(incident), false);
				}	
			}
			mapBean.repaint();
		}
	}
	
	public void setTextFormResponseDao(TextFormResponseDao textformResponseDao) {
		this.textformResponseDao = textformResponseDao;
	}
	
	public void setFormResponseDao(FormResponseDao formResponseDao) {
		this.formResponseDao = formResponseDao;
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
	    ui.setInteger(fields.sldZoomLevel, VALUE, zoom);
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
	
	public void locationHovered(double latitude, double longitude) {
		String latitudeString = Double.toString(latitude);
		if (latitudeString.length() > 8) {
			latitudeString = latitudeString.substring(0,8);
		}
		String longitudeString = Double.toString(longitude);
		if (longitudeString.length() > 8) {
			longitudeString = longitudeString.substring(0,8);
		}
		ui.setText(fields.lblCoordinates, String.format("%s, %s", latitudeString, longitudeString));
	}

	public void locationSelected(double latitude, double longitude) {}
	
	public void markerSelected(Marker marker) { 
		if (marker instanceof IncidentMarker) {
			IncidentMarker incidentMarker = (IncidentMarker)marker;
			if (incidentMarker.getIncident() != null) {
				LOG.debug("Incident: %s", incidentMarker.getIncident().getTitle());
				ReportDialogHandler reportDialog = new ReportDialogHandler(pluginController, frontlineController, ui);
				reportDialog.showDialog(incidentMarker.getIncident());
			}
		}
		else if (marker instanceof MessageMarker) {
			MessageMarker messageMarker = (MessageMarker)marker;
			if (messageMarker != null && messageMarker.getFrontlineMessage() != null) {
				LOG.debug("Message: %s", messageMarker.getFrontlineMessage().getTextContent());
				ResponseDialogHandler responseDialog = new ResponseDialogHandler(pluginController, frontlineController, ui);
				responseDialog.showDialog(messageMarker.getFrontlineMessage(), messageMarker.getLocation());
			}
		}
		else if (marker instanceof FormMarker) {
			FormMarker formMarker = (FormMarker)marker;
			if (formMarker != null && formMarker.getFormResponse() != null) {
				LOG.debug("Form: %s", formMarker.getFormResponse());
				ResponseDialogHandler responseDialog = new ResponseDialogHandler(pluginController, frontlineController, ui);
				responseDialog.setFormResponseDao(formResponseDao);
				responseDialog.showDialog(formMarker.getFormResponse(), formMarker.getLocation(), formMarker.getForm());
			}
		}
		else if (marker instanceof TextFormMarker) {
			TextFormMarker textformMarker = (TextFormMarker)marker;
			if(textformMarker != null && textformMarker.getTextFormResponse() != null) {
				LOG.debug("TextForm: %s", textformMarker.getTextFormResponse());
				ResponseDialogHandler responseDialog = new ResponseDialogHandler(pluginController, frontlineController, ui);
				responseDialog.showDialog(textformMarker.getTextFormResponse(), textformMarker.getLocation());
			}
		}
	}
}