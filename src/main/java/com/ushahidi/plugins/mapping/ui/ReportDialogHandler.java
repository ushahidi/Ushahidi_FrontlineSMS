package com.ushahidi.plugins.mapping.ui;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.Photo;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.ui.markers.Marker;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

@SuppressWarnings("serial")
public class ReportDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler, MapListener {

	private static MappingLogger LOG = MappingLogger.getLogger(ReportDialogHandler.class);
	
	private static final String UI_DIALOG_XML = "/ui/plugins/mapping/reportDialog.xml";

	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
	
	private static final String UNKNOWN = "unknown";
	private static final String SEPARATOR = ", ";
	
	private final Object mainDialog;
	private final UIFields fields;
	private class UIFields extends Fields {
		public UIFields(UiGeneratorController uiController, Object parent) {
			super(uiController, parent);
		}
		public Object txtReportTitle;
		public Object txtReportDescription;
		public Object txtReportCategories;
		public Object lblReportVerified;
		public Object txtReportVerified;
		public Object lstReportCategories;
		public Object txtReportDate;
		public Object txtReportLocation;
		public Object txtReportCoordinates;
		public Object pnlReportLocation;
		public Object cboReportLocations;
		public Object txtSenderName;
		public Object lblSenderName;
		public Object txtSenderEmail;
		public Object lblSenderEmail;
		public Object btnSave;
		public Object btnCancel;
		public Object btnClose;
		public Object btnReportDate;
		public Object cbxExistingLocation;
		public Object pnlExistingLocation;
		public Object pnlNewLocation;
		public Object txtNewLocation;
		public Object lstPhotos;
		public Object lblPhotos;
		public Object pnlAddPhoto;
		public Object txtAddPhoto;
		public Object lblSyncStatus;
		public Object txtSyncStatus;
	}
	
	public ReportDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.locationDao = pluginController.getLocationDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainDialog = ui.loadComponentFromFile(UI_DIALOG_XML, this);
		this.fields = new UIFields(ui, mainDialog);
	}
	
	public void showDialog(Incident incident) {
		ui.setAttachedObject(mainDialog, incident);
		
		removeAll(fields.cboReportLocations);
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {
			if (location.getName() != null && location.getName().equalsIgnoreCase(UNKNOWN) == false) {
				ui.add(fields.cboReportLocations, createComboboxChoice(location.getName(), location));
			}
		}
		
		removeAll(fields.lstReportCategories);
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			ui.add(fields.lstReportCategories, createListItem(category.getDisplayName(), category));
		}
		
		if (incident != null) {
			ui.setText(fields.txtReportTitle, incident.getTitle());
			ui.setText(fields.txtReportDescription, incident.getDescription());
			ui.setText(fields.txtReportCategories, incident.getCategoryNames());
			for(Object item : ui.getItems(fields.lstReportCategories)) {
				Category category = ui.getAttachedObject(item, Category.class);
				ui.setSelected(item, incident.hasCategory(category));
			}
			if (incident.getIncidentDate() != null) {
				ui.setText(fields.txtReportDate, InternationalisationUtils.getDatetimeFormat().format(incident.getIncidentDate()));
			}
			else {
				ui.setText(fields.txtReportDate, "");
			}
			if (incident.getLocation() != null) {
				ui.setText(fields.txtReportLocation, incident.getLocation().getName());
				int index = 0;
				for(Object item : ui.getItems(fields.cboReportLocations)) {
					Location location = ui.getAttachedObject(item, Location.class);
					if (incident.isLocation(location)) {
						ui.setSelectedIndex(fields.cboReportLocations, index);
						break;
					}
					index++;
				}
				ui.setText(fields.txtReportCoordinates, String.format("%f, %f", incident.getLocation().getLatitude(), incident.getLocation().getLongitude()));
			}
			else {
				ui.setText(fields.txtReportLocation, "");
				ui.setSelectedIndex(fields.cboReportLocations, -1);
				ui.setText(fields.txtReportCoordinates, "");
			}
			ui.setText(fields.txtSenderName, incident.getFirstName());
			ui.setText(fields.txtSenderEmail, incident.getEmailAddress());
			
			ui.removeAll(fields.lstPhotos);
			ui.setVisible(fields.pnlAddPhoto, false);
			ui.setVisible(fields.lstPhotos, true);
			if (incident.getPhotos().size() > 0) {
				for(Photo photo : incident.getPhotos()) {
					Object item = ui.createListItem("", photo);
					ui.setIcon(item, photo.getImage());
					ui.add(fields.lstPhotos, item);
				}
				ui.setHeight(fields.lstPhotos, 500);
			}
			else {
				ui.setHeight(fields.lstPhotos, 12);
			}
			if (incident.hasSyncStatus()) {
				ui.setVisible(fields.lblSyncStatus, true);
				ui.setVisible(fields.txtSyncStatus, true);
				ui.setText(fields.txtSyncStatus, incident.getSyncStatus());
			}
			else {
				ui.setVisible(fields.lblSyncStatus, false);
				ui.setVisible(fields.txtSyncStatus, false);
			}
			if (incident.isVerified()) {
				ui.setText(fields.txtReportVerified, MappingMessages.getYes());
			}
			else {
				ui.setText(fields.txtReportVerified, MappingMessages.getNo());
			}
			ui.setVisible(fields.lblPhotos, true);
		}
		else {
			ui.setText(fields.txtReportTitle, "");
			ui.setText(fields.txtReportDescription, "");
			ui.setText(fields.txtReportCategories, "");
			ui.setText(fields.txtReportDate, "");
			ui.setText(fields.txtReportLocation, "");
			ui.setText(fields.txtReportCoordinates, "");
			ui.setVisible(fields.lblPhotos, false);
			ui.setVisible(fields.lstPhotos, false);
			ui.setVisible(fields.pnlAddPhoto, false);
			ui.setVisible(fields.lblSyncStatus, false);
			ui.setVisible(fields.txtSyncStatus, false);
			ui.setText(fields.txtReportVerified, "");
		}
		boolean editMode = incident == null || incident.isMarked();
		ui.setVisible(fields.txtReportLocation, !editMode);
		ui.setVisible(fields.pnlReportLocation, editMode);
		
		ui.setVisible(fields.txtReportCategories, !editMode);
		ui.setVisible(fields.lstReportCategories, editMode);

		ui.setVisible(fields.btnSave, editMode);
		ui.setVisible(fields.btnCancel, editMode);
		ui.setVisible(fields.btnClose, !editMode);
		
		ui.setVisible(fields.lblSenderName, editMode);
		ui.setVisible(fields.txtSenderName, editMode);
		ui.setVisible(fields.lblSenderEmail, editMode);
		ui.setVisible(fields.txtSenderEmail, editMode);
		
		ui.setEditable(fields.txtReportTitle, editMode);
		ui.setEditable(fields.txtReportDescription, editMode);
		ui.setEditable(fields.txtReportLocation, editMode);
		ui.setVisible(fields.btnReportDate, editMode);
		
		ui.setVisible(fields.lblReportVerified, true);
		ui.setVisible(fields.txtReportVerified, true);
		
		ui.add(mainDialog);
	}
	
	public void showDialog(FrontlineMessage message) {
		ui.setAttachedObject(mainDialog, message);
		
		ui.setVisible(fields.txtReportLocation, false);
		ui.setVisible(fields.pnlReportLocation, true);
		
		ui.setVisible(fields.txtReportCategories, false);
		ui.setVisible(fields.lstReportCategories, true);
		
		ui.setVisible(fields.btnSave, true);
		ui.setVisible(fields.btnCancel, true);
		ui.setVisible(fields.btnClose, false);
		
		ui.setVisible(fields.lblSenderName, true);
		ui.setVisible(fields.txtSenderName, true);
		ui.setVisible(fields.lblSenderEmail, true);
		ui.setVisible(fields.txtSenderEmail, true);
		
		ui.setVisible(fields.btnReportDate, true);
		
		ui.setVisible(fields.lstPhotos, false);
		ui.setVisible(fields.pnlAddPhoto, true);
		
		ui.setEditable(fields.txtReportTitle, true);
		ui.setEditable(fields.txtReportDescription, true);
		
		ui.setVisible(fields.lblSyncStatus, false);
		ui.setVisible(fields.txtSyncStatus, false);
		
		ui.setVisible(fields.lblReportVerified, false);
		ui.setVisible(fields.txtReportVerified, false);
		
		removeAll(fields.cboReportLocations);
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {	
			if (location.getName() != null && location.getName().equalsIgnoreCase(UNKNOWN) == false) {
				ui.add(fields.cboReportLocations, createComboboxChoice(location.getName(), location));
			}
		}
		
		removeAll(fields.lstReportCategories);
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			ui.add(fields.lstReportCategories, createListItem(category.getDisplayName(), category));
		}
			
		ui.setText(fields.txtReportDate, InternationalisationUtils.getDatetimeFormat().format(message.getDate()));
		ui.setText(fields.txtReportTitle, message.getTextContent());
		Contact contact = getContact(message);
		if (contact != null){
			ui.setText(fields.txtSenderName, contact.getName());
			ui.setText(fields.txtSenderEmail, contact.getEmailAddress());
		}
		else {
			ui.setText(fields.txtSenderName, "");
			ui.setText(fields.txtSenderEmail, "");
		}
		ui.add(mainDialog);	
	}
	
	/**
	 * Saves an {@link Incident} created from a text message
	 * 
	 * @throws DuplicateKeyException
	 */
	public void saveReport(Object dialog) throws DuplicateKeyException {
		Object attachedObject = getAttachedObject(dialog);
		Incident incident = null;
		if (attachedObject instanceof FrontlineMessage) {
			incident = new Incident();
		}
		else if (attachedObject instanceof Incident) {
			incident = (Incident)attachedObject;
		}
		if (this.getBoolean(fields.cbxExistingLocation, Thinlet.SELECTED)){
			Object selectedItem = getSelectedItem(fields.cboReportLocations);
			if (selectedItem != null) {
				Location location = getAttachedObject(selectedItem, Location.class);
				incident.setLocation(location);	
			}
		}
		else {
			String coordinatesText = ui.getText(fields.txtReportCoordinates);
			if (coordinatesText != null && coordinatesText.length() > 0) {
				String[] coordinates = coordinatesText.split(SEPARATOR);
				
				double latitude = Double.parseDouble(coordinates[0]);
				double longitude = Double.parseDouble(coordinates[1]);
				
				Location location = new Location(latitude, longitude);
				location.setName(coordinatesText);
				location.setMappingSetup(mappingSetupDao.getDefaultSetup());
				try{
					locationDao.saveLocation(location);					
				}
				catch(DuplicateKeyException de){
					LOG.debug(de);
					ui.alert(MappingMessages.getErrorInvalidLocation());
					return;
				}
				incident.setLocation(location);
			}
		}			
		
		incident.setTitle(getText(fields.txtReportTitle));
		incident.setDescription(getText(fields.txtReportDescription));
		incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
		incident.setEmailAddress(ui.getText(fields.txtSenderEmail));
		String[] words = ui.getText(fields.txtSenderName).split(" ");
		if (words != null && words.length > 0) {
			incident.setFirstName(words[0]);
			if (words.length > 1) {
				incident.setLastName(words[1]);
			}
			else {
				incident.setLastName(null);
			}
		}
		else {
			incident.setFirstName(null);
			incident.setLastName(null);
		}
		
		incident.removeCategories();
		for(Object selectedItem : ui.getSelectedItems(fields.lstReportCategories)) {
			Category category = getAttachedObject(selectedItem, Category.class);
			incident.addCategory(category);
			LOG.debug("category_id:%d server_id:%d", category.getId(), category.getServerId());
		}
		incident.setMarked(true);
		
		String srcPath = ui.getText(fields.txtAddPhoto);
		if (srcPath != null) {
			File destDirectory = new File(ResourceUtils.getConfigDirectoryPath(), "photos");
			Photo photo = Photo.importPhoto(srcPath, destDirectory);
			if (photo != null) {
				LOG.debug("Adding Photo: %s", photo.getLocalPath());
				incident.addMedia(photo);
			}
		}
		
		String dateString = getText(fields.txtReportDate);
		try{
			incident.setIncidentDate(InternationalisationUtils.getDatetimeFormat().parse(dateString));
		}
		catch(ParseException pe){
			LOG.debug("Invalid date string", pe);
			ui.alert(MappingMessages.getDateInvalid());
			return;
		}			
		try{
			LOG.debug("incident_id:%d", incident.getId());
			if (incident.getId() > 0) {
				incidentDao.updateIncident(incident);
			}
			else {
				incidentDao.saveIncident(incident);
			}
			ui.remove(mainDialog);
		}
		catch(DuplicateKeyException de) {
			de.printStackTrace();
			if (incident.getId() > 0) {
				ui.alert(MappingMessages.getErrorUpdatingIncident());
			}
			else {
				ui.alert(MappingMessages.getErrorCreatingIncident());
			}
		}
		pluginController.refreshIncidentReports();
		pluginController.refreshIncidentMap();
	}
	
	/**
	 * Get all the frontend ids of the categories
	 * @return
	 */
	public List<String> getCategoryNames(){
		ArrayList<String> items = new ArrayList<String>();
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())) {
			items.add(category.getTitle().toLowerCase());
		}
		return items;
	}
	
	private Contact getContact(FrontlineMessage message) {
		return frontlineController.getContactDao().getFromMsisdn(message.getSenderMsisdn());
	}
	
	public void locationChanged(Object comboBox, Object textField) {
		Object selectedItem = getSelectedItem(comboBox);
		Location location = (Location)getAttachedObject(selectedItem);
		String coordinates = Double.toString(location.getLatitude()) + SEPARATOR + Double.toString(location.getLongitude());
		ui.setText(textField, coordinates);
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	public void selectLocationFromMap(Object dialog) {
		setEnabled(fields.cboReportLocations, false);
		pluginController.showIncidentMap();
		setBoolean(dialog, Thinlet.MODAL, false);
		ui.setVisible(dialog, false);
	}

	public void showDateSelecter(Object textField) {
		ui.showDateSelecter(textField);
	}
	
	public void showExistingLocations() {
		LOG.debug("showExistingLocations");
		ui.setVisible(fields.pnlExistingLocation, true);
		ui.setVisible(fields.pnlNewLocation, false);
	}
	
	public void showNewLocation() {
		LOG.debug("showNewLocation");
		ui.setVisible(fields.pnlExistingLocation, false);
		ui.setVisible(fields.pnlNewLocation, true);
	}
	
	public void showFileChooser(Object textField) {
		LOG.debug("showFileChooser");
		ImageChooser imageChooser = new ImageChooser(this.ui);
		imageChooser.setButtonText(MappingMessages.getSelectFile());
		imageChooser.setToolTipText(MappingMessages.getIncidentAddPhoto());
		ui.setText(textField, imageChooser.showDialog());
	}
	
	//################# MapListener #################
	
	public void locationSelected(double latitude, double longitude) {
		ui.setText(fields.txtReportCoordinates, String.format("%f, %f", latitude, longitude));
		ui.setText(fields.txtNewLocation, String.format("%f, %f", latitude, longitude));
		pluginController.refreshIncidentMap();
		pluginController.refreshIncidentReports();
		setBoolean(mainDialog, Thinlet.MODAL, true);
		ui.setVisible(mainDialog, true);
		ui.repaint();
	}
	
	public void zoomChanged(int zoom) {}

	public void locationHovered(double latitude, double longitude) {}
	
	public void markerSelected(Marker marker) {}
}