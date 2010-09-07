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
import com.ushahidi.plugins.mapping.utils.MappingLogger;

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

	public static MappingLogger LOG = MappingLogger.getLogger(ReportDialogHandler.class);
	
	private static final String UI_DIALOG_XML = "/ui/plugins/mapping/reportDialog.xml";

	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private Object mainDialog;
	
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
	private final Object txtSender;
	private final Object lblSender;
	private final Object btnSave;
	private final Object btnCancel;
	private final Object btnClose;
	private final Object btnReportDate;
	private final Object cbxExistingLocation;
	private final Object cbxNewLocation;
	private final Object pnlExistingLocation;
	private final Object pnlNewLocation;
	private final Object txtNewLocation;
	
	public ReportDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		ui = uiController;
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
		
		this.txtSender = ui.find(this.mainDialog, "txtSender");
		this.lblSender = ui.find(this.mainDialog, "lblSender");
		
		this.txtReportCategories = ui.find(this.mainDialog, "txtReportCategories");
		this.lstReportCategories = ui.find(this.mainDialog, "lstReportCategories");
		
		this.txtReportLocation = ui.find(this.mainDialog, "txtReportLocation");
		this.pnlReportLocation = ui.find(this.mainDialog, "pnlReportLocation");
		this.cboReportLocations = ui.find(this.mainDialog, "cboReportLocations");
		
		this.cbxExistingLocation = ui.find(this.mainDialog, "cboReportLocations");
		this.pnlExistingLocation = ui.find(this.mainDialog, "pnlExistingLocation");
		
		this.cbxNewLocation = ui.find(this.mainDialog, "cbxNewLocation");
		this.pnlNewLocation = ui.find(this.mainDialog, "pnlNewLocation");
		this.txtNewLocation =  ui.find(this.mainDialog, "txtNewLocation");
		
		this.btnSave = ui.find(this.mainDialog, "btnSave");
		this.btnCancel = ui.find(this.mainDialog, "btnCancel");
		this.btnClose = ui.find(this.mainDialog, "btnClose");
	}
	
	public void showDialog(Incident incident) {
		ui.setAttachedObject(mainDialog, incident);
		
		ui.setVisible(txtReportLocation, true);
		ui.setVisible(pnlReportLocation, false);
		
		ui.setVisible(txtReportCategories, true);
		ui.setVisible(lstReportCategories, false);

		ui.setVisible(btnSave, false);
		ui.setVisible(btnCancel, false);
		ui.setVisible(btnClose, true);
		
		ui.setVisible(lblSender, false);
		ui.setVisible(txtSender, false);
		
		if (incident != null) {
			ui.setText(txtReportTitle, incident.getTitle());
			ui.setText(txtReportDescription, incident.getDescription());
			ui.setText(txtReportCategories, incident.getCategoryNames());
			ui.setText(txtReportDate, InternationalisationUtils.getDatetimeFormat().format(incident.getIncidentDate()));
			if (incident.getLocation() != null) {
				ui.setText(txtReportLocation, incident.getLocation().getName());
				ui.setText(txtReportCoordinates, String.format("%f, %f", incident.getLocation().getLatitude(), incident.getLocation().getLongitude()));
			}
			else {
				ui.setText(txtReportLocation, "");
				ui.setText(txtReportCoordinates, "");
			}
		}
		else {
			ui.setText(txtReportTitle, "");
			ui.setText(txtReportDescription, "");
			ui.setText(txtReportCategories, "");
			ui.setText(txtReportDate, "");
			ui.setText(txtReportLocation, "");
			ui.setText(txtReportCoordinates, "");
		}
		ui.setEditable(txtReportTitle, incident == null);
		ui.setEditable(txtReportDescription, incident == null);
		ui.setEditable(txtReportLocation, incident == null);
		ui.setVisible(btnReportDate, incident == null);
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
		
		ui.setVisible(lblSender, true);
		ui.setVisible(txtSender, true);
		
		ui.setVisible(btnReportDate, true);
		
		ui.setEditable(txtReportTitle, true);
		ui.setEditable(txtReportDescription, true);
		
		removeAll(cboReportLocations);
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {				
			ui.add(cboReportLocations, createComboboxChoice(location.getName(), location));
		}
		
		removeAll(lstReportCategories);
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			ui.add(lstReportCategories, createListItem(category.getTitle(), category));
		}
			
		ui.setText(txtReportDate, InternationalisationUtils.getDatetimeFormat().format(message.getDate()));
		ui.setText(txtSender, getSenderDisplayValue(message));
		ui.setText(txtReportTitle, message.getTextContent());
		
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
		if (ui.getBoolean(cboReportLocations, ENABLED)){
			Location location = (Location)getAttachedObject(getSelectedItem(cboReportLocations));
			incident.setLocation(location);
		}
		else {
			String coordinatesText = ui.getText(txtReportCoordinates);
			String[] coordinates = coordinatesText.split(", ");
			
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
				ui.alert("The location [" + coordinatesText + "] could not be saved.");
				return;
			}
			incident.setLocation(location);
		}			
		
		incident.setTitle(getText(txtReportTitle));
		incident.setDescription(getText(txtReportDescription));
		incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
		
		for(Object selectedItem : ui.getSelectedItems(lstReportCategories)) {
			Category category = (Category)getAttachedObject(selectedItem);
			incident.addCategory(category);
		}
		incident.setMarked(true);
		
		String dateString = getText(txtReportDate);
		try{
			incident.setIncidentDate(InternationalisationUtils.getDatetimeFormat().parse(dateString));
		}
		catch(ParseException pe){
			LOG.debug("Invalid date string", pe);
			ui.alert("The incident date [" + dateString + "] is invalid");
			return;
		}			
		try{
			incidentDao.saveIncident(incident);
		}
		catch(DuplicateKeyException de) {
			LOG.debug(de);
			ui.alert("ERROR: Unable to create an incident from the text message");
			return;
		}
		ui.remove(mainDialog);
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
	
	/**
	 * Gets the display name for the sender of of the text message
	 * @param message
	 * @return
	 */
	private String getSenderDisplayValue(FrontlineMessage message) {
		Contact sender = frontlineController.getContactDao().getFromMsisdn(message.getSenderMsisdn());
		return sender != null ? sender.getDisplayName() + "(" + message.getSenderMsisdn() + ")" : message.getSenderMsisdn();
	}
	
	public void locationChanged(Object comboBox, Object textField) {
		Object selectedItem = getSelectedItem(comboBox);
		Location location = (Location)getAttachedObject(selectedItem);
		String coordinates = Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude());
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

	//@Override
	public void mapZoomed(int zoom) {}

	//@Override
	public void pointSelected(double lat, double lon) {
		ui.setText(txtReportCoordinates, String.format("%f, %f", lat, lon));
		ui.setText(txtNewLocation, String.format("%f, %f", lat, lon));
		setBoolean(mainDialog, Thinlet.MODAL, true);
		ui.setVisible(mainDialog, true);
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
}