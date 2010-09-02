package com.ushahidi.plugins.mapping.forms;

import java.util.HashMap;
import java.util.Map;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.events.DatabaseEntityNotification;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.events.FrontlineEventNotification;
import net.frontlinesms.plugins.PluginController;
import net.frontlinesms.plugins.forms.FormsPluginController;
import net.frontlinesms.plugins.forms.data.domain.Form;
import net.frontlinesms.plugins.forms.data.domain.FormField;
import net.frontlinesms.plugins.forms.data.domain.FormFieldType;
import net.frontlinesms.plugins.forms.data.domain.FormResponse;
import net.frontlinesms.plugins.forms.data.domain.ResponseValue;
import net.frontlinesms.plugins.forms.data.repository.FormDao;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.utils.MappingLogger;
import com.ushahidi.plugins.mapping.utils.MappingMessages;

/**
 * FormsManager
 * @author dalezak
 *
 */
public class FormsManager implements EventObserver {

	public static MappingLogger LOG = MappingLogger.getLogger(FormsManager.class);	
	
	private FormDao formDao;
	private final CategoryDao categoryDao;
	private final MappingSetupDao mappingSetupDao;	

	private FormsPluginController formsPluginController;
	private final Map<String, FormField> fieldDictionary = new HashMap<String, FormField>();
	
	/**
	 * FormsManager
	 * @param frontlineController FrontlineSMS
	 * @param pluginController FrontlineSMS
	 * @param appContext FrontlineSMS
	 */
	public FormsManager(FrontlineSMS frontlineController, MappingPluginController pluginController) {
		frontlineController.getEventBus().registerObserver(this);
		
		this.formsPluginController = getPluginController(frontlineController);
		
		this.categoryDao = pluginController.getCategoryDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.formDao = this.formsPluginController.getFormDao();
	}
	
	/**
	 * Create Ushahidi-specific form
	 */
	public void addUshahidiForms() {
		LOG.debug("createUshahidiForms");
		try {
			String formName = MappingMessages.getIncidentReport();
			if (this.formDao == null) {
				this.formDao = this.formsPluginController.getFormDao();
			}
			for(Form form : this.formDao.getAllForms()) {
				if (form.getName().equalsIgnoreCase(formName)) {
					for (FormField formField : form.getFields()) {
						LOG.debug("FormField Loaded [%s, %s]", formField.getType().toString(), formField.getLabel());
						this.fieldDictionary.put(formField.getLabel(), formField);
					}
					LOG.debug("Ushahidi Form alerady exists, exiting.");
					return;
				}
			}
			Form form = new Form(formName);
			//DETAILS
			addFormField(form, FormFieldType.TEXT_FIELD, MappingMessages.getTitle());
			addFormField(form, FormFieldType.TEXT_AREA, MappingMessages.getDescription());
			//DATE
			addFormField(form, FormFieldType.DATE_FIELD, MappingMessages.getDate());
			//TIME
			addFormField(form, FormFieldType.TIME_FIELD, MappingMessages.getTime());
			//LOCATION
			addFormField(form, FormFieldType.TEXT_FIELD, MappingMessages.getLocation());
			//CATEGORIES
			addFormField(form, FormFieldType.TRUNCATED_TEXT, MappingMessages.getCategories());
			for(Category category: this.categoryDao.getAllCategories(this.mappingSetupDao.getDefaultSetup())){
				addFormField(form, FormFieldType.CHECK_BOX, category.getTitle());
			}
			//MEDIA
			addFormField(form, FormFieldType.TEXT_FIELD, MappingMessages.getNews());
			addFormField(form, FormFieldType.TEXT_FIELD, MappingMessages.getVideo());
			//CONTACT
			addFormField(form, FormFieldType.TEXT_FIELD, MappingMessages.getFirstName());
			addFormField(form, FormFieldType.TEXT_FIELD, MappingMessages.getLastName());
			addFormField(form, FormFieldType.EMAIL_FIELD, MappingMessages.getEmail());
			this.formDao.saveForm(form);	
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Handle incoming FrontlineEventNotification
	 */
	@SuppressWarnings("unchecked")
	public void notify(FrontlineEventNotification notification) {
		if (notification instanceof DatabaseEntityNotification) {
			DatabaseEntityNotification databaseEntityNotification = (DatabaseEntityNotification)notification;
			if (databaseEntityNotification.getDatabaseEntity() instanceof FormResponse) {
				FormResponse formResponse = (FormResponse)databaseEntityNotification.getDatabaseEntity();
				LOG.debug("Form Name: %s", formResponse.getParentForm());
				StringBuilder results = new StringBuilder();
				for (ResponseValue value : formResponse.getResults()) {
					if (results.length() > 0) {
						results.append(", ");
					}
					results.append(value.toString());
				}
				LOG.debug("Ushahidi Form Received %s : [%s]", formResponse.getSubmitter(), results.toString());
			}
		}
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
			this.fieldDictionary.put(label, formField);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Get FormsPluginController
	 * @param frontlineController FrontlineSMS
	 * @return FormsPluginController
	 */
	private FormsPluginController getPluginController(FrontlineSMS frontlineController) {
		for (PluginController pluginController : frontlineController.getPluginManager().getPluginControllers()) {
			if (pluginController instanceof FormsPluginController) {
				return (FormsPluginController)pluginController;
			}
		}
		return null;
	}

}