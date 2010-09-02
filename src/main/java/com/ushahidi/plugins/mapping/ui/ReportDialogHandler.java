package com.ushahidi.plugins.mapping.ui;

import java.io.IOException;
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
import net.frontlinesms.ui.DateSelecter;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

@SuppressWarnings("serial")
public class ReportDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler, MapListener {

	public static MappingLogger LOG = MappingLogger.getLogger(ReportDialogHandler.class);
	
	private static final String UI_DIALOG_XML = "/ui/plugins/mapping/reportDialog.xml";

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

	public ReportDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.locationDao = pluginController.getLocationDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainDialog = this.ui.loadComponentFromFile(UI_DIALOG_XML, this);
		
		this.txtReportTitle = this.ui.find(this.mainDialog, "txtReportTitle");
		this.txtReportDescription = this.ui.find(this.mainDialog, "txtReportDescription");
		this.txtReportCoordinates = this.ui.find(this.mainDialog, "txtReportCoordinates");
	
		this.txtReportDate = this.ui.find(this.mainDialog, "txtReportDate");
		this.btnReportDate = this.ui.find(this.mainDialog, "btnReportDate");
		
		this.txtSender = this.ui.find(this.mainDialog, "txtSender");
		this.lblSender = this.ui.find(this.mainDialog, "lblSender");
		
		this.txtReportCategories = this.ui.find(this.mainDialog, "txtReportCategories");
		this.lstReportCategories = this.ui.find(this.mainDialog, "lstReportCategories");
		
		this.txtReportLocation = this.ui.find(this.mainDialog, "txtReportLocation");
		this.pnlReportLocation = this.ui.find(this.mainDialog, "pnlReportLocation");
		this.cboReportLocations = this.ui.find(this.mainDialog, "cboReportLocations");
		
		this.btnSave = this.ui.find(this.mainDialog, "btnSave");
		this.btnCancel = this.ui.find(this.mainDialog, "btnCancel");
		this.btnClose = this.ui.find(this.mainDialog, "btnClose");
	}
	
	public void showDialog(Incident incident) {
		this.ui.setAttachedObject(this.mainDialog, incident);
		
		setVisible(this.txtReportLocation, true);
		setVisible(this.pnlReportLocation, false);
		
		setVisible(this.txtReportCategories, true);
		setVisible(this.lstReportCategories, false);

		setVisible(this.btnSave, false);
		setVisible(this.btnCancel, false);
		setVisible(this.btnClose, true);
		
		setVisible(this.lblSender, false);
		setVisible(this.txtSender, false);
		
		if (incident != null) {
			setText(this.txtReportTitle, incident.getTitle());
			setText(this.txtReportDescription, incident.getDescription());
			setText(this.txtReportCategories, incident.getCategoryNames());
			setText(this.txtReportDate, InternationalisationUtils.getDatetimeFormat().format(incident.getIncidentDate()));
			if (incident.getLocation() != null) {
				setText(this.txtReportLocation, incident.getLocation().getName());
				setText(this.txtReportCoordinates, String.format("%f, %f", incident.getLocation().getLatitude(), incident.getLocation().getLongitude()));
			}
			else {
				setText(this.txtReportLocation, "");
				setText(this.txtReportCoordinates, "");
			}
		}
		else {
			setText(this.txtReportTitle, "");
			setText(this.txtReportDescription, "");
			setText(this.txtReportCategories, "");
			setText(this.txtReportDate, "");
			setText(this.txtReportLocation, "");
			setText(this.txtReportCoordinates, "");
		}
		setEditable(this.txtReportTitle, incident == null);
		setEditable(this.txtReportDescription, incident == null);
		setEditable(this.txtReportLocation, incident == null);
		setVisible(this.btnReportDate, incident == null);
		this.ui.add(this.mainDialog);
	}
	
	public void showDialog(FrontlineMessage message) {
		this.ui.setAttachedObject(this.mainDialog, message);
		
		setVisible(this.txtReportLocation, false);
		setVisible(this.pnlReportLocation, true);
		
		setVisible(this.txtReportCategories, false);
		setVisible(this.lstReportCategories, true);
		
		setVisible(this.btnSave, true);
		setVisible(this.btnCancel, true);
		setVisible(this.btnClose, false);
		
		setVisible(this.lblSender, true);
		setVisible(this.txtSender, true);
		
		setVisible(this.btnReportDate, true);
		
		setEditable(this.txtReportTitle, true);
		setEditable(this.txtReportDescription, true);
		
		removeAll(this.cboReportLocations);
		for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {				
			ui.add(this.cboReportLocations, createComboboxChoice(location.getName(), location));
		}
		
		removeAll(this.lstReportCategories);
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			ui.add(this.lstReportCategories, this.createListItem(category.getTitle(), category));
		}
			
		setText(this.txtReportDate, InternationalisationUtils.getDatetimeFormat().format(message.getDate()));
		setText(this.txtSender, getSenderDisplayValue(message));
		setText(this.txtReportTitle, message.getTextContent());
		
		this.ui.add(this.mainDialog);	
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
		if (getBoolean(this.cboReportLocations, ENABLED)){
			Location location = (Location)getAttachedObject(getSelectedItem(this.cboReportLocations));
			incident.setLocation(location);
		}
		else {
			String coordinatesText = getText(this.txtReportCoordinates);
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
		
		incident.setTitle(getText(this.txtReportTitle));
		incident.setDescription(getText(this.txtReportDescription));
		incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
		
		for(Object selectedItem : ui.getSelectedItems(this.lstReportCategories)) {
			Category category = (Category)getAttachedObject(selectedItem);
			incident.addCategory(category);
		}
		incident.setMarked(true);
		
		String dateString = getText(this.txtReportDate);
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
		ui.remove(this.mainDialog);
	}
	
	/**
	 * Get all the frontend ids of the categories
	 * @return
	 */
	public List<String> getCategoryNames(){
		ArrayList<String> items = new ArrayList<String>();
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup()))
			items.add(category.getTitle().toLowerCase());
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
		setText(textField, coordinates);
		this.ui.repaint(textField);
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	public void selectLocationFromMap(Object dialog) {
		this.setEnabled(this.cboReportLocations, false);
		setBoolean(dialog, Thinlet.MODAL, false);
		setVisible(dialog, false);
		ui.repaint();
	}

	@Override
	public void mapZoomed(int zoom) {}

	@Override
	public void pointSelected(double lat, double lon) {
		setText(this.txtReportCoordinates, String.format("%f, %f", lat, lon));
		setBoolean(this.mainDialog, Thinlet.MODAL, true);
		setVisible(this.mainDialog, true);
		ui.repaint();
	}
	
	public void showDateSelecter(Object textField) {
		this.ui.showDateSelecter(textField);
	}
}