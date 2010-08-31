package com.ushahidi.plugins.mapping.ui;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.FrontlineUtils;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

@SuppressWarnings("serial")
public class ReportDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler {

	private static final String UI_DIALOG_XML = "/ui/plugins/mapping/reportDialog.xml";
	
	public static Logger LOG = FrontlineUtils.getLogger(ReportDialogHandler.class);	
	
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
	private final Object txtReportDate;
	private final Object txtReportLocation;
	private final Object txtReportCoordinates;
	private final Object cbxVerified;
	
	private static final String COMPONENT_INCIDENT_DIALOG = "incident_Dialog";
	private static final String COMPONENT_LOCATIONS_COMBO = "cboLocations";
	private static final String COMPONENT_CATEGORIES_COMBO = "cboCategories";
	private static final String COMPONENT_ADDITIONAL_INFO_TEXTFIELD = "txtAdditionalInfo";
	private static final String COMPONENT_LBL_SELECTED_LATITUDE = "lbl_Latitude";
	private static final String COMPONENT_LBL_SELECTED_LONGITUDE = "lbl_Longitude";
	private static final String COMPONENT_LOCATION_NAME_FIELD = "txtLocationName";

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
		this.txtReportCategories = this.ui.find(this.mainDialog, "txtReportCategories");
		this.txtReportDate = this.ui.find(this.mainDialog, "txtReportDate");
		this.txtReportLocation = this.ui.find(this.mainDialog, "txtReportLocation");
		this.txtReportCoordinates = this.ui.find(this.mainDialog, "txtReportCoordinates");
		this.cbxVerified = this.ui.find(this.mainDialog, "cbxVerified");
	}
	
	public void showDialog(Incident incident) {
		this.ui.setAttachedObject(this.mainDialog, incident);
		if (incident != null) {
			setText(this.txtReportTitle, incident.getTitle());
			setText(this.txtReportDescription, incident.getDescription());
			setText(this.txtReportCategories, incident.getCategoryNames());
			setText(this.txtReportDate, InternationalisationUtils.getDatetimeFormat().format(incident.getIncidentDate()));
			setText(this.txtReportLocation, incident.getLocation().getName());
			setText(this.txtReportCoordinates, Double.toString(incident.getLocation().getLatitude()) + ", " + 
											   Double.toString(incident.getLocation().getLongitude()));
			setSelected(this.cbxVerified, incident.isVerified());
		}
		else {
			setText(this.txtReportTitle, "");
			setText(this.txtReportDescription, "");
			setText(this.txtReportCategories, "");
			setText(this.txtReportDate, "");
			setText(this.txtReportLocation, "");
			setText(this.txtReportCoordinates, "");
			setSelected(this.cbxVerified, false);
		}
		setEditable(this.txtReportTitle, incident == null);
		setEditable(this.txtReportDescription, incident == null);
		setEditable(this.txtReportTitle, incident == null);
		setEditable(this.txtReportLocation, incident == null);
		setEditable(this.txtReportCoordinates, incident == null);
		setEnabled(this.cbxVerified, false);
		this.ui.add(this.mainDialog);
	}
	
	public void showDialog(FrontlineMessage message) {
		this.ui.setAttachedObject(this.mainDialog, message);
		
		//Populate the locations combo
		Object cbLocation = find(this.mainDialog, COMPONENT_LOCATIONS_COMBO);
		for(Location  l: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {				
			Object choice = createComboboxChoice(l.getName(), l);
			ui.add(cbLocation, choice);
		}
		
		Object cbCategory = find(this.mainDialog, COMPONENT_CATEGORIES_COMBO);
		for(Category c: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			Object choice = createComboboxChoice(c.getTitle(), c);
			ui.add(cbCategory, choice);
		}
			
		//Load Message Details			
		setText(this.txtReportDate, InternationalisationUtils.getDatetimeFormat().format(message.getDate()));
		setText(find(this.mainDialog, "txtIncidentSender"), getSenderDisplayValue(message));
		setText(find(this.mainDialog, "txtMessage"), message.getTextContent());
		
		ui.add(this.mainDialog);	
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
	
	/**
	 * Save the details for the report after editing
	 * @param dialog
	 */
	public void saveReport(Object dialog){
		Incident incident = (Incident)getAttachedObject(dialog);
		if(incident != null){
			incident.setTitle(getText(this.txtReportTitle));
			incident.setDescription(getText(this.txtReportDescription));
			//Only locally created incidents should be marked for posting to the frontend
			//TODO: Accomodate editing and posting of incidents fetched from the frontend; API feature request
			if (Long.toString(incident.getFrontendId()).equals(null)) {
				incident.setMarked(true);
			}
			try{
				incidentDao.updateIncident(incident);
			}
			catch(DuplicateKeyException e){
				LOG.debug("Unable to update incident", e);
			}
		}
	}
	
	/**
	 * Saves an {@link Incident} created from a text message
	 * 
	 * @throws DuplicateKeyException
	 */
	public void saveIncident() throws DuplicateKeyException {
		Object dialog = ui.find(COMPONENT_INCIDENT_DIALOG);
		
		FrontlineMessage message = (FrontlineMessage)getAttachedObject(dialog);
		if(message != null){
			Incident incident = new Incident();
			String dateStr = getText(ui.find(dialog, "txtIncidentDate"));
			String title = message.getTextContent();
			String additionalInfo = getText(ui.find(dialog, COMPONENT_ADDITIONAL_INFO_TEXTFIELD));			
			
			Location location = null;
			
			//Get form values
			Object cboLocations = ui.find(dialog, COMPONENT_LOCATIONS_COMBO);
			if(getBoolean(cboLocations, ENABLED)){
				location = (Location)getAttachedObject(getSelectedItem(cboLocations));
				incident.setLocation(location);
			}
			else {
				double lat = Double.parseDouble(getText(ui.find(dialog, COMPONENT_LBL_SELECTED_LATITUDE)));
				double lon = Double.parseDouble(getText(ui.find(dialog, COMPONENT_LBL_SELECTED_LONGITUDE)));
				
				String name = getText(ui.find(dialog, COMPONENT_LOCATION_NAME_FIELD));
				location = new Location(lat, lon);
				location.setName(name);
				location.setMappingSetup(mappingSetupDao.getDefaultSetup());
				try{
					locationDao.saveLocation(location);					
				}
				catch(DuplicateKeyException de){
					LOG.debug(de);
					//de.printStackTrace();
					ui.alert("The location ["+name+"] could not be saved.");
					return;
				}
				//Reload the list of keywords
				//updateKeywordList();
			}			
			
			incident.setTitle(title);
			incident.setDescription(additionalInfo);
			incident.setLocation(location);
			incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
			
			Object cboCategories = ui.find(dialog, COMPONENT_CATEGORIES_COMBO);
//			Category category = (Category)getAttachedObject(getSelectedItem(cboCategories));
//			incident.setCategory(category);
			incident.setMarked(true);
			
			try{
				incident.setIncidentDate(InternationalisationUtils.getDatetimeFormat().parse(dateStr));
			}catch(ParseException pe){
				LOG.debug("Invalid date string", pe);
				ui.alert("The incident date [" + dateStr + "] is invalid");
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
		}
		
		//Re-plot the incidents on the map
		//mapBean.setIncidents(incidentDao.getAllIncidents());
		ui.remove(this);
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
}