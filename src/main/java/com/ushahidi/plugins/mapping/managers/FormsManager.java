package com.ushahidi.plugins.mapping.managers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.data.events.EntitySavedNotification;
import net.frontlinesms.data.repository.ContactDao;
import net.frontlinesms.events.FrontlineEventNotification;
import net.frontlinesms.plugins.forms.FormsPluginController;
import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.domain.FormField;
import net.frontlinesms.plugins.forms.data.domain.FormFieldType;
import net.frontlinesms.plugins.forms.data.domain.FormResponse;
import net.frontlinesms.plugins.forms.data.domain.ResponseValue;
import net.frontlinesms.plugins.forms.data.repository.FormDao;
import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;

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

/**
 * FormsManager
 * @author dalezak
 *
 */
public class FormsManager extends Manager {

	public static MappingLogger LOG = MappingLogger.getLogger(FormsManager.class);	
	
	private final MappingPluginController pluginController;
	private final ContactDao contactDao;
	private final FormDao formDao;
	private final FormResponseDao formResponseDao;
	private final CategoryDao categoryDao;
	private final LocationDao locationDao;
	private final MappingSetupDao mappingSetupDao;	
	private final IncidentDao incidentDao;

	private final String formName;
	
	/**
	 * FormsManager
	 * @param frontlineController FrontlineSMS
	 * @param pluginController FrontlineSMS
	 * @param appContext FrontlineSMS
	 */
	public FormsManager(FrontlineSMS frontlineController, MappingPluginController pluginController) {
		this.pluginController = pluginController;
		frontlineController.getEventBus().registerObserver(this);
		
		this.contactDao = pluginController.getContactDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.locationDao = pluginController.getLocationDao();
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		FormsPluginController formsPluginController = getPluginController(frontlineController, FormsPluginController.class);
		this.formDao = formsPluginController.getFormDao();
		this.formResponseDao = formsPluginController.getFormResponseDao();	
		
		this.formName = MappingMessages.getIncidentReport();		
	}
	
	/**
	 * Handle incoming FrontlineEventNotification
	 */
	@SuppressWarnings("unchecked")
	public void notify(FrontlineEventNotification notification) {
		if (notification instanceof EntitySavedNotification) {
			EntitySavedNotification entitySavedNotification = (EntitySavedNotification)notification;
			if (entitySavedNotification.getDatabaseEntity() instanceof FormResponse) {
				FormResponse formResponse = (FormResponse)entitySavedNotification.getDatabaseEntity();
				Form form = formResponse.getParentForm();
				if (formResponse.getParentForm().getName().equalsIgnoreCase(formName)) {
					LOG.debug("Form '%s' Received From (%s)", form.getName(), formResponse.getSubmitter());
					final List<Category> categories = categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup());
					final List<Location> locations = locationDao.getAllLocations(mappingSetupDao.getDefaultSetup());
					Incident incident = new Incident();
					incident.setMarked(true);
					incident.setMappingSetup(mappingSetupDao.getDefaultSetup());
					Contact contact = getContact(formResponse.getSubmitter());
					if (contact != null) {
						incident.setFirstName(contact.getName());
						incident.setEmailAddress(contact.getEmailAddress());
					}
					int index = 0;
					for (FormField formField : form.getFields()) {
						if (formField.getType().hasValue()) {
							ResponseValue value = formResponse.getResults().get(index);
							if (formField.getLabel().equalsIgnoreCase(MappingMessages.getTitle())) {
								LOG.debug("Incident Title: %s", value);
								incident.setTitle(value.toString());
							}
							else if (formField.getLabel().equalsIgnoreCase(MappingMessages.getDescription())) {
								LOG.debug("Incident Description: %s", value);
								incident.setDescription(value.toString());
							}
							else if (formField.getLabel().equalsIgnoreCase(MappingMessages.getDate())) {
								LOG.debug("Incident Date: %s", value);
								SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
								try {
									Date date = dateFormat.parse(value.toString());
									if (incident.getIncidentDate() != null) {
										Calendar current = Calendar.getInstance();
										current.setTime(incident.getIncidentDate());
										Calendar updated = Calendar.getInstance();
										updated.setTime(date);
										current.set(Calendar.YEAR, updated.get(Calendar.YEAR));
										current.set(Calendar.MONTH, updated.get(Calendar.MONTH));
										current.set(Calendar.DAY_OF_MONTH, updated.get(Calendar.DAY_OF_MONTH));
										incident.setIncidentDate(current.getTime());
									}
									else {
										incident.setIncidentDate(date);
									}
								} 
								catch (ParseException ex) {
									LOG.error("ParseException: %s", ex);
								}
							}
							else if (formField.getLabel().equalsIgnoreCase(MappingMessages.getTime())) {
								LOG.debug("Incident Time: %s", value);
								SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
								try {
									Date date = dateFormat.parse(value.toString());
									if (incident.getIncidentDate() != null) {
										Calendar current = Calendar.getInstance();
										current.setTime(incident.getIncidentDate());
										Calendar updated = Calendar.getInstance();
										updated.setTime(date);
										current.set(Calendar.HOUR_OF_DAY, updated.get(Calendar.HOUR_OF_DAY));
										current.set(Calendar.MINUTE, updated.get(Calendar.MINUTE));
										incident.setIncidentDate(current.getTime());
									}
									else {
										incident.setIncidentDate(date);
									}
								} 
								catch (ParseException ex) {
									LOG.error("ParseException: %s", ex);
								}
							}
							else if (isCategoryField(categories, formField.getLabel())){
								if (isTrue(value.toString())) {
									for(Category category : categories) {
										if (category.getTitle().equalsIgnoreCase(formField.getLabel())) {
											LOG.debug("Incident Category: %s", category.getTitle());
											incident.addCategory(category);
											break;
										}
									}
								}
							}
							else if (isLocationField(locations, formField.getLabel())){
								if (isTrue(value.toString())) {
									for(Location location : locations) {
										if (location.getName().equalsIgnoreCase(formField.getLabel())) {
											LOG.debug("Incident Location: %s", location.getName());
											incident.setLocation(location);
											break;
										}
									}
								}
							}
							else if (formField.getLabel().equalsIgnoreCase(MappingMessages.getLocationOther())) {
								LOG.debug("Incident Other Location: %s", value);
								//TODO set other location
							}
							else {
								LOG.error("Unknown Field: %s", formField.getLabel());
							}
							index++;
						}
					}
					try {
						incidentDao.saveIncident(incident);
						LOG.debug("Saving New Incident: %s", incident.getTitle());
						pluginController.refreshIncidentMap();
						pluginController.refreshIncidentReports();
					} 
					catch (DuplicateKeyException ex) {
						LOG.error("DuplicateKeyException: %s", ex);
					}
				}
			}
		}
	}
	
	/**
	 * Create Ushahidi-specific form fields
	 */
	public boolean addFormFields() {
		LOG.debug("createUshahidiForms");
		try {
			for(Form form : this.formDao.getAllForms()) {
				if (form.getName().equalsIgnoreCase(formName)) {
					LOG.debug("Ushahidi Form already exists, exiting.");
					return true;
				}
			}
			Form form = new Form(formName);
			//TITLE
			addFormField(form, FormFieldType.TEXT_FIELD, MappingMessages.getTitle());
			//DESCRIPTION
			addFormField(form, FormFieldType.TEXT_AREA, MappingMessages.getDescription());
			//DATE
			addFormField(form, FormFieldType.DATE_FIELD, MappingMessages.getDate());
			//TIME
			addFormField(form, FormFieldType.TIME_FIELD, MappingMessages.getTime());
			//CATEGORIES
			addFormField(form, FormFieldType.TRUNCATED_TEXT, MappingMessages.getCategories());
			for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
				addFormField(form, FormFieldType.CHECK_BOX, category.getTitle());
			}
			//LOCATION
			addFormField(form, FormFieldType.TRUNCATED_TEXT, MappingMessages.getLocation());
			for(Location location: locationDao.getAllLocations(mappingSetupDao.getDefaultSetup())) {
				if (location.getName() != null && location.getName().equalsIgnoreCase("unknown") == false) {
					addFormField(form, FormFieldType.CHECK_BOX, location.getName());
				}
			}
			//OTHER LOCATION
			addFormField(form, FormFieldType.TEXT_FIELD, MappingMessages.getLocationOther());
			this.formDao.saveForm(form);	
			return true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean addFormResponse(String title) {
		try {
			FormResponse formResponse = new FormResponse(getSenderMsisdn(), getForm(), getResponseValues(title));
			LOG.debug("Added Form Response: %s", title);
			this.formResponseDao.saveResponse(formResponse);
			return true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Add Field to Form
	 * @param form Form
	 * @param formFieldType FormFieldType
	 * @param label field label
	 */
	private void addFormField(Form form, FormFieldType formFieldType, String label) {
		try {
			FormField formField = new FormField(formFieldType, label); 
			form.addField(formField);
			LOG.debug("FormField Created [%s, %s]", formFieldType.toString(), label);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String getSenderMsisdn() {
		for (Contact contact : this.contactDao.getAllContacts()) {
			if (contact.getPhoneNumber() != null) {
				return contact.getPhoneNumber();
			}
			else if (contact.getOtherPhoneNumber() != null) {
				return contact.getOtherPhoneNumber();
			}
		}
		return null;
	}
	
	private List<ResponseValue> getResponseValues(String title) {
		List<ResponseValue> responseValues = new ArrayList<ResponseValue>();
		boolean categorySpecified = false;
		boolean locationSpecified = false;
		final List<Category> categories = categoryDao.getAllCategories(this.mappingSetupDao.getDefaultSetup());
		final List<Location> locations = locationDao.getAllLocations(this.mappingSetupDao.getDefaultSetup());
		for(FormField formField :getForm().getFields()) {
			LOG.debug("FormField: %s", formField.getLabel());
			if (formField.getLabel().equalsIgnoreCase(MappingMessages.getTitle())) {
				responseValues.add(new ResponseValue(title != null ? title : "Incident Title"));
			}
			else if (formField.getLabel().equalsIgnoreCase(MappingMessages.getDescription())) {
				responseValues.add(new ResponseValue(title != null ? title : "Incident Description"));
			}
			else if (formField.getLabel().equalsIgnoreCase(MappingMessages.getDate())) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
				responseValues.add(new ResponseValue(dateFormat.format(new Date())));
			}
			else if (formField.getLabel().equalsIgnoreCase(MappingMessages.getTime())) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
				responseValues.add(new ResponseValue(dateFormat.format(new Date())));
			}
			else if (isCategoryField(categories, formField.getLabel())){
				responseValues.add(new ResponseValue(categorySpecified ? "false" : "true"));
				categorySpecified = true;
			}
			else if (isLocationField(locations, formField.getLabel())){
				responseValues.add(new ResponseValue(locationSpecified ? "false" : "true"));
				locationSpecified = true;
			}
			else if (formField.getLabel().equalsIgnoreCase(MappingMessages.getLocationOther())) {
				responseValues.add(new ResponseValue("A Different Location"));
			}
		}
		return responseValues;
	}
	
	private Form getForm() {
		for(Form form : this.formDao.getAllForms()) {
			if (form.getName().equalsIgnoreCase(formName)) {
				return form;
			}
		}
		return null;
	}
	
	private boolean isCategoryField(List<Category> categories, String fieldLabel) {
		for (Category category : categories) {
			if (category.getTitle().toLowerCase().startsWith(fieldLabel.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isLocationField(List<Location> locations, String fieldLabel) {
		for (Location location : locations) {
			if (location.getName().toLowerCase().startsWith(fieldLabel.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isTrue(String value) {
		for (String trueValue : new String[] {"true", "yes"}) {
			if (trueValue.equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}
	
	private Contact getContact(String phoneNumber) {
		for(Contact contact : contactDao.getAllContacts()) {
			if (contact.getPhoneNumber().equalsIgnoreCase(phoneNumber) ||
				contact.getOtherPhoneNumber().equalsIgnoreCase(phoneNumber)) {
				return contact;
			}
		}
		return null;
	}
}