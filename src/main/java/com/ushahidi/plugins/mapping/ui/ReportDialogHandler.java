package com.ushahidi.plugins.mapping.ui;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import thinlet.Thinlet;


import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
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
	
	private final Object mainDialog;
	
	private final LocationDao locationDao;
	private final CategoryDao categoryDao;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final Object txtReportTitle;
	private final Object txtReportDescription;
	private final Object txtReportCategories;
	private final Object lstReportCategories;
	private final Object txtReportDate;
	private final Object txtReportLocation;
	private final Object txtReportCoordinates;
	private final Object pnlReportLocation;
	private final Object cboReportLocations;
	private final Object txtSenderName;
	private final Object lblSenderName;
	private final Object txtSenderEmail;
	private final Object lblSenderEmail;
	private final Object btnSave;
	private final Object btnCancel;
	private final Object btnClose;
	private final Object btnReportDate;
	private final Object cbxExistingLocation;
	private final Object pnlExistingLocation;
	private final Object pnlNewLocation;
	private final Object txtNewLocation;
	
	private static final String UNKNOWN = "unknown";
	private static final String SEPARATOR = ", ";
	
	public ReportDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.locationDao = pluginController.getLocationDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainDialog = ui.loadComponentFromFile(UI_DIALOG_XML, this);
		
		this.txtReportTitle = ui.find(this.mainDialog, "txtReportTitle");
		this.txtReportDescription = ui.find(this.mainDialog, "txtReportDescription");
		this.txtReportCoordinates = ui.find(this.mainDialog, "txtReportCoordinates");
	
		this.txtReportDate = ui.find(this.mainDialog, "txtReportDate");
		this.btnReportDate = ui.find(this.mainDialog, "btnReportDate");
		
		this.txtSenderName = ui.find(this.mainDialog, "txtSenderName");
		this.lblSenderName = ui.find(this.mainDialog, "lblSenderName");
		this.txtSenderEmail = ui.find(this.mainDialog, "txtSenderEmail");
		this.lblSenderEmail = ui.find(this.mainDialog, "lblSenderEmail");
		
		this.txtReportCategories = ui.find(this.mainDialog, "txtReportCategories");
		this.lstReportCategories = ui.find(this.mainDialog, "lstReportCategories");
		
		this.txtReportLocation = ui.find(this.mainDialog, "txtReportLocation");
		this.pnlReportLocation = ui.find(this.mainDialog, "pnlReportLocation");
		this.cboReportLocations = ui.find(this.mainDialog, "cboReportLocations");
		
		this.cbxExistingLocation = ui.find(this.mainDialog, "cboReportLocations");
		this.pnlExistingLocation = ui.find(this.mainDialog, "pnlExistingLocation");
		
		this.pnlNewLocation = ui.find(this.mainDialog, "pnlNewLocation");
		this.txtNewLocation =  ui.find(this.mainDialog, "txtNewLocation");
		
		this.btnSave = ui.find(this.mainDialog, "btnSave");
		this.btnCancel = ui.find(this.mainDialog, "btnCancel");
		this.btnClose = ui.find(this.mainDialog, "btnClose");
	}
	
	public void showDialog(Incident incident) {
		ui.setAttachedObject(mainDialog, incident);
		
		removeAll(cboReportLocations);
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {
			if (location.getName() != null && location.getName().equalsIgnoreCase(UNKNOWN) == false) {
				ui.add(cboReportLocations, createComboboxChoice(location.getName(), location));
			}
		}
		
		removeAll(lstReportCategories);
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			ui.add(lstReportCategories, createListItem(category.getDisplayName(), category));
		}
		
		if (incident != null) {
			ui.setText(txtReportTitle, incident.getTitle());
			ui.setText(txtReportDescription, incident.getDescription());
			ui.setText(txtReportCategories, incident.getCategoryNames());
			for(Object item : ui.getItems(lstReportCategories)) {
				Category category = ui.getAttachedObject(item, Category.class);
				ui.setSelected(item, incident.hasCategory(category));
			}
			if (incident.getIncidentDate() != null) {
				ui.setText(txtReportDate, InternationalisationUtils.getDatetimeFormat().format(incident.getIncidentDate()));
			}
			else {
				ui.setText(txtReportDate, "");
			}
			if (incident.getLocation() != null) {
				ui.setText(txtReportLocation, incident.getLocation().getName());
				int index = 0;
				for(Object item : ui.getItems(cboReportLocations)) {
					Location location = ui.getAttachedObject(item, Location.class);
					if (incident.isLocation(location)) {
						ui.setSelectedIndex(cboReportLocations, index);
						break;
					}
					index++;
				}
				ui.setText(txtReportCoordinates, String.format("%f, %f", incident.getLocation().getLatitude(), incident.getLocation().getLongitude()));
			}
			else {
				ui.setText(txtReportLocation, "");
				ui.setSelectedIndex(cboReportLocations, -1);
				ui.setText(txtReportCoordinates, "");
			}
			ui.setText(txtSenderName, incident.getFirstName());
			ui.setText(txtSenderEmail, incident.getEmailAddress());
		}
		else {
			ui.setText(txtReportTitle, "");
			ui.setText(txtReportDescription, "");
			ui.setText(txtReportCategories, "");
			ui.setText(txtReportDate, "");
			ui.setText(txtReportLocation, "");
			ui.setText(txtReportCoordinates, "");	
		}
		boolean editMode = incident == null || incident.isMarked();
		ui.setVisible(txtReportLocation, !editMode);
		ui.setVisible(pnlReportLocation, editMode);
		
		ui.setVisible(txtReportCategories, !editMode);
		ui.setVisible(lstReportCategories, editMode);

		ui.setVisible(btnSave, editMode);
		ui.setVisible(btnCancel, editMode);
		ui.setVisible(btnClose, !editMode);
		
		ui.setVisible(lblSenderName, editMode);
		ui.setVisible(txtSenderName, editMode);
		ui.setVisible(lblSenderEmail, editMode);
		ui.setVisible(txtSenderEmail, editMode);
		
		ui.setEditable(txtReportTitle, editMode);
		ui.setEditable(txtReportDescription, editMode);
		ui.setEditable(txtReportLocation, editMode);
		ui.setVisible(btnReportDate, editMode);
		
		ui.add(mainDialog);
	}
	
	public void showDialog(FrontlineMessage message) {
		ui.setAttachedObject(mainDialog, message);
		
		ui.setVisible(txtReportLocation, false);
		ui.setVisible(pnlReportLocation, true);
		
		ui.setVisible(txtReportCategories, false);
		ui.setVisible(lstReportCategories, true);
		
		ui.setVisible(btnSave, true);
		ui.setVisible(btnCancel, true);
		ui.setVisible(btnClose, false);
		
		ui.setVisible(lblSenderName, true);
		ui.setVisible(txtSenderName, true);
		ui.setVisible(lblSenderEmail, true);
		ui.setVisible(txtSenderEmail, true);
		
		ui.setVisible(btnReportDate, true);
		
		ui.setEditable(txtReportTitle, true);
		ui.setEditable(txtReportDescription, true);
		
		removeAll(cboReportLocations);
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {	
			if (location.getName() != null && location.getName().equalsIgnoreCase(UNKNOWN) == false) {
				ui.add(cboReportLocations, createComboboxChoice(location.getName(), location));
			}
		}
		
		removeAll(lstReportCategories);
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			ui.add(lstReportCategories, createListItem(category.getDisplayName(), category));
		}
			
		ui.setText(txtReportDate, InternationalisationUtils.getDatetimeFormat().format(message.getDate()));
		ui.setText(txtReportTitle, message.getTextContent());
		Contact contact = getContact(message);
		if (contact != null){
			ui.setText(txtSenderName, contact.getName());
			ui.setText(txtSenderEmail, contact.getEmailAddress());
		}
		else {
			ui.setText(txtSenderName, "");
			ui.setText(txtSenderEmail, "");
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
		if (ui.getSelectedItem(cbxExistingLocation) != null){
			Location location = getAttachedObject(getSelectedItem(cboReportLocations), Location.class);
			incident.setLocation(location);
		}
		else {
			String coordinatesText = ui.getText(txtReportCoordinates);
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
		
		incident.setTitle(getText(txtReportTitle));
		incident.setDescription(getText(txtReportDescription));
		incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
		incident.setEmailAddress(ui.getText(txtSenderEmail));
		String[] words = ui.getText(this.txtSenderName).split(" ");
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
		for(Object selectedItem : ui.getSelectedItems(lstReportCategories)) {
			Category category = getAttachedObject(selectedItem, Category.class);
			incident.addCategory(category);
			LOG.debug("category_id:%d server_id:%d", category.getId(), category.getServerId());
		}
		incident.setMarked(true);
		
		String dateString = getText(txtReportDate);
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
		setEnabled(cboReportLocations, false);
		pluginController.showIncidentMap();
		setBoolean(dialog, Thinlet.MODAL, false);
		ui.setVisible(dialog, false);
	}

	public void showDateSelecter(Object textField) {
		ui.showDateSelecter(textField);
	}
	
	public void showExistingLocations() {
		LOG.debug("showExistingLocations");
		ui.setVisible(pnlExistingLocation, true);
		ui.setVisible(pnlNewLocation, false);
	}
	
	public void showNewLocation() {
		LOG.debug("showNewLocation");
		ui.setVisible(pnlExistingLocation, false);
		ui.setVisible(pnlNewLocation, true);
	}
	

	//################# MapListener #################
	
	public void mapZoomed(int zoom) {}

	public void pointSelected(double latitude, double longitude) {
		ui.setText(txtReportCoordinates, String.format("%f, %f", latitude, longitude));
		ui.setText(txtNewLocation, String.format("%f, %f", latitude, longitude));
		pluginController.refreshIncidentMap();
		pluginController.refreshIncidentReports();
		setBoolean(mainDialog, Thinlet.MODAL, true);
		ui.setVisible(mainDialog, true);
		ui.repaint();
	}
}