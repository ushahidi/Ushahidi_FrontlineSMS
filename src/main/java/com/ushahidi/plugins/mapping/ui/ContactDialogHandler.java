package com.ushahidi.plugins.mapping.ui;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.domain.LocationDetails;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.ui.markers.Marker;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

public class ContactDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler, MapListener {
	
	private static final long serialVersionUID = 1L;

	private static MappingLogger LOG = MappingLogger.getLogger(ContactDialogHandler.class);
	
	private static final String UI_DIALOG_XML = "/ui/plugins/mapping/contactDialog.xml";

	private final MappingPluginController pluginController;
	@SuppressWarnings("unused")
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final Object mainDialog;
	
	private final LocationDao locationDao;
	private final ContactDao contactDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final Object txtContactName;
	private final Object cboLocations;
	private final Object pnlExistingLocation;
	private final Object pnlNewLocation;
	private final Object cbxExistingLocation;
	private final Object txtNewLocation;
	private final Object txtCoordinates;
	
	private static final String UNKNOWN = "unknown";
	private static final String SEPARATOR = ", ";
	
	public ContactDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.locationDao = pluginController.getLocationDao();
		this.contactDao = pluginController.getContactDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainDialog = ui.loadComponentFromFile(UI_DIALOG_XML, this);
		
		this.txtContactName = ui.find(this.mainDialog, "txtContactName");
		this.cboLocations = ui.find(this.mainDialog, "cboLocations");
		this.pnlExistingLocation = ui.find(this.mainDialog, "pnlExistingLocation");
		this.pnlNewLocation = ui.find(this.mainDialog, "pnlNewLocation");
		this.cbxExistingLocation = ui.find(this.mainDialog, "cbxExistingLocation");
		this.txtNewLocation = ui.find(this.mainDialog, "txtNewLocation");
		this.txtCoordinates = ui.find(this.mainDialog, "txtCoordinates");
	}
	
	public void showDialog(Contact contact) {
		ui.setAttachedObject(mainDialog, contact);
		removeAll(cboLocations);
		if (contact != null) {
			ui.setText(txtContactName, contact.getName());
			LocationDetails locationDetails = contact.getDetails(LocationDetails.class);
			if (locationDetails != null) {
				ui.setText(txtCoordinates, locationDetails.getLocationCoordinates());
			}
			else {
				ui.setText(txtCoordinates, "");
			}
			int index = 0;
			ui.setSelectedIndex(cboLocations, -1);
			for(Location location: locationDao.getAllLocations()) {	
				if (location.getName() != null && location.getName().equalsIgnoreCase(UNKNOWN) == false) {
					ui.add(cboLocations, createComboboxChoice(location.getName(), location));
					if (locationDetails != null && location.getId() == locationDetails.getLocationID()) {
						ui.setSelectedIndex(cboLocations, index);
					}
				}
				index++;
			}
		}
		else {
			ui.setText(txtContactName, "");
			ui.setText(txtCoordinates, "");
		}
		ui.add(mainDialog);
	}
	
	public void saveContact(Object dialog) {
		Contact contact = ui.getAttachedObject(mainDialog, Contact.class);
		LocationDetails locationDetails = contact.getDetails(LocationDetails.class);
		if (this.getBoolean(cbxExistingLocation, Thinlet.SELECTED)){
			Location location = getAttachedObject(getSelectedItem(cboLocations), Location.class);
			if (locationDetails != null) {
				locationDetails.setLocation(location);
			}
			else {
				contact.addDetails(new LocationDetails(location));
			}
			try {
				contactDao.updateContact(contact);
				ui.remove(dialog);
				pluginController.refreshContacts();
			} 
			catch (DuplicateKeyException e) {
				e.printStackTrace();
			}
		}
		else {
			String coordinatesText = ui.getText(txtCoordinates);
			String[] coordinates = coordinatesText.split(SEPARATOR);
			if (coordinates.length == 2) {
				double latitude = Double.parseDouble(coordinates[0]);
				double longitude = Double.parseDouble(coordinates[1]);
				String locationText = ui.getText(txtNewLocation);
				Location location = new Location(latitude, longitude);
				location.setName(locationText);
				location.setMappingSetup(mappingSetupDao.getDefaultSetup());
				try{
					locationDao.saveLocation(location);					
				}
				catch(DuplicateKeyException de){
					LOG.debug(de);
					ui.alert(MappingMessages.getErrorInvalidLocation());
					return;
				}
				if (locationDetails != null) {
					locationDetails.setLocation(location);
				}
				else {
					contact.addDetails(new LocationDetails(location));
				}
				try {
					contactDao.updateContact(contact);
					ui.remove(dialog);
					pluginController.refreshContacts();
				} 
				catch (DuplicateKeyException e) {
					e.printStackTrace();
				}
			}
			else {
				ui.alert(MappingMessages.getErrorInvalidLocation());
			}
		}
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
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
		ui.setText(txtCoordinates, "");
	}
	
	public void locationChanged(Object comboBox, Object textField) {
		Object selectedItem = getSelectedItem(comboBox);
		Location location = getAttachedObject(selectedItem, Location.class);
		String coordinates = Double.toString(location.getLatitude()) + SEPARATOR + Double.toString(location.getLongitude());
		ui.setText(textField, coordinates);
	}
	
	public void selectLocationFromMap(Object dialog) {
		ui.setEnabled(cboLocations, false);
		pluginController.refreshIncidentMap();
		setBoolean(dialog, Thinlet.MODAL, false);
		ui.setVisible(dialog, false);
	}

	//################# MapListener #################
	
	public void zoomChanged(int zoom) {}

	public void locationSelected(double latitude, double longitude) {
		ui.setText(txtCoordinates, String.format("%f, %f", latitude, longitude));
		ui.setText(txtNewLocation, String.format("%f, %f", latitude, longitude));
		setBoolean(mainDialog, Thinlet.MODAL, true);
		ui.setVisible(mainDialog, true);
		ui.repaint();
	}
	
	public void locationHovered(double latitude, double longitude) {}
	
	public void markerSelected(Marker marker) {}
}