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

@SuppressWarnings({"serial", "unused"})
public class ContactDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler, MapListener {
	
	private static MappingLogger LOG = MappingLogger.getLogger(ContactDialogHandler.class);
	
	private static final String UI_DIALOG_XML = "/ui/plugins/mapping/contactDialog.xml";

	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final LocationDao locationDao;
	private final ContactDao contactDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final Object mainDialog;
	private final UIFields fields;
	private class UIFields extends Fields {
		public UIFields(UiGeneratorController uiController, Object parent) {
			super(uiController, parent);
		}
		public Object txtContactName;
		public Object cboLocations;
		public Object pnlExistingLocation;
		public Object pnlNewLocation;
		public Object cbxExistingLocation;
		public Object txtNewLocation;
		public Object txtCoordinates;
	}
	
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
		this.fields = new UIFields(ui, mainDialog);
	}
	
	public void showDialog(Contact contact) {
		ui.setAttachedObject(mainDialog, contact);
		removeAll(fields.cboLocations);
		if (contact != null) {
			ui.setText(fields.txtContactName, contact.getName());
			LocationDetails locationDetails = contact.getDetails(LocationDetails.class);
			if (locationDetails != null) {
				ui.setText(fields.txtCoordinates, locationDetails.getLocationCoordinates());
			}
			else {
				ui.setText(fields.txtCoordinates, "");
			}
			int index = 0;
			ui.setSelectedIndex(fields.cboLocations, -1);
			for(Location location: locationDao.getAllLocations()) {	
				if (location.getName() != null && location.getName().equalsIgnoreCase(UNKNOWN) == false) {
					ui.add(fields.cboLocations, createComboboxChoice(location.getName(), location));
					if (locationDetails != null && location.getId() == locationDetails.getLocationID()) {
						ui.setSelectedIndex(fields.cboLocations, index);
					}
				}
				index++;
			}
		}
		else {
			ui.setText(fields.txtContactName, "");
			ui.setText(fields.txtCoordinates, "");
		}
		ui.add(mainDialog);
	}
	
	public void saveContact(Object dialog) {
		Contact contact = ui.getAttachedObject(mainDialog, Contact.class);
		LocationDetails locationDetails = contact.getDetails(LocationDetails.class);
		if (this.getBoolean(fields.cbxExistingLocation, Thinlet.SELECTED)){
			Location location = getAttachedObject(getSelectedItem(fields.cboLocations), Location.class);
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
			String coordinatesText = ui.getText(fields.txtCoordinates);
			String[] coordinates = coordinatesText.split(SEPARATOR);
			if (coordinates.length == 2) {
				double latitude = Double.parseDouble(coordinates[0]);
				double longitude = Double.parseDouble(coordinates[1]);
				String locationText = ui.getText(fields.txtNewLocation);
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
					pluginController.refreshIncidentMap();
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
		ui.setVisible(fields.pnlExistingLocation, true);
		ui.setVisible(fields.pnlNewLocation, false);
	}
	
	public void showNewLocation() {
		LOG.debug("showNewLocation");
		ui.setVisible(fields.pnlExistingLocation, false);
		ui.setVisible(fields.pnlNewLocation, true);
		ui.setText(fields.txtCoordinates, "");
	}
	
	public void locationChanged(Object comboBox, Object textField) {
		Object selectedItem = getSelectedItem(comboBox);
		Location location = getAttachedObject(selectedItem, Location.class);
		String coordinates = Double.toString(location.getLatitude()) + SEPARATOR + Double.toString(location.getLongitude());
		ui.setText(textField, coordinates);
	}
	
	public void selectLocationFromMap(Object dialog) {
		ui.setEnabled(fields.cboLocations, false);
		pluginController.refreshIncidentMap();
		setBoolean(dialog, Thinlet.MODAL, false);
		ui.setVisible(dialog, false);
	}

	//################# MapListener #################
	
	public void locationSelected(double latitude, double longitude) {
		ui.setText(fields.txtCoordinates, String.format("%f, %f", latitude, longitude));
		ui.setText(fields.txtNewLocation, String.format("%f, %f", latitude, longitude));
		setBoolean(mainDialog, Thinlet.MODAL, true);
		ui.setVisible(mainDialog, true);
		ui.repaint();
	}
	
	public void zoomChanged(int zoom) {}
	
	public void locationHovered(double latitude, double longitude) {}
	
	public void markerSelected(Marker marker) {}
}