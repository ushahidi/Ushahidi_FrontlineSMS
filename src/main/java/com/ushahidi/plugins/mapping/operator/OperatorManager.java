package com.ushahidi.plugins.mapping.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Location;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.utils.MappingLogger;
import com.ushahidi.plugins.mapping.utils.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.events.DatabaseEntityNotification;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.events.FrontlineEventNotification;

import net.frontlinesms.plugins.PluginController;
import net.frontlinesms.plugins.resourcemapper.ResourceMapperPluginController;
import net.frontlinesms.plugins.resourcemapper.data.domain.mapping.Field;
import net.frontlinesms.plugins.resourcemapper.data.domain.mapping.FieldType;
import net.frontlinesms.plugins.resourcemapper.data.domain.response.FieldResponse;
import net.frontlinesms.plugins.resourcemapper.data.repository.FieldMappingDao;
import net.frontlinesms.plugins.resourcemapper.data.repository.FieldMappingFactory;

/**
 * OperatorManager
 * @author dalezak
 *
 */
public class OperatorManager implements EventObserver {

	public static MappingLogger LOG = MappingLogger.getLogger(OperatorManager.class);	
	
	/**
	 * FieldMappingDao
	 */
	private final FieldMappingDao fieldMappingDao;
	
	private final CategoryDao categoryDao;
	private final LocationDao locationDao;
	private final MappingSetupDao mappingSetupDao;
	
	private ResourceMapperPluginController resourceMapperPluginController;
	
	private final Map<String, Field> fieldDictionary = new HashMap<String, Field>();
	
	/**
	 * OperatorManager
	 * @param frontlineController FrontlineSMS
	 * @param pluginController FrontlineSMS
	 * @param appContext ApplicationContext
	 */
	public OperatorManager(FrontlineSMS frontlineController, MappingPluginController pluginController) {
		frontlineController.getEventBus().registerObserver(this);
		
		this.resourceMapperPluginController = getPluginController(frontlineController);
		
		this.categoryDao = pluginController.getCategoryDao();
		this.locationDao = pluginController.getLocationDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();

		this.fieldMappingDao = resourceMapperPluginController.getFieldMappingDao();
	}
	
	/**
	 * Create Ushahidi-friendly Operator fields
	 */
	public void addUshahidiFields() {
		LOG.debug("createUshahidiFields");
		//DETAILS
		addOperatorField(MappingMessages.getTitle(), MappingMessages.getTitleKeyword(), MappingMessages.getTitleInfo(), FieldType.PLAINTEXT);
		addOperatorField(MappingMessages.getDescription(), MappingMessages.getDescriptionKeyword(), MappingMessages.getDescriptionInfo(), FieldType.PLAINTEXT);
		//DATE
		addOperatorField(MappingMessages.getDateNow(), MappingMessages.getDateNowKeyword(), MappingMessages.getDateNowInfo(), FieldType.BOOLEAN);
		addOperatorField(MappingMessages.getDate(), MappingMessages.getDateKeyword(), MappingMessages.getDateInfo(), FieldType.DATE);
		//CATEGORIES
		List<String> categories = new ArrayList<String>();
		for(Category category: this.categoryDao.getAllCategories(this.mappingSetupDao.getDefaultSetup())){
			categories.add(category.getTitle());
		}
		addOperatorField(MappingMessages.getCategories(), MappingMessages.getCategoriesKeyword(), MappingMessages.getCategoriesInfo(), FieldType.CHECKLIST, 
							categories.toArray(new String[categories.size()]));
		//LOCATION
		addOperatorField(MappingMessages.getDefaultLocation(), MappingMessages.getDefaultLocationKeyword(), MappingMessages.getDefaultLocationInfo(), FieldType.BOOLEAN);
		List<String> locations = new ArrayList<String>();
		for(Location location: this.locationDao.getAllLocations(this.mappingSetupDao.getDefaultSetup())) {
			if (location.getName() != null && location.getName().length() > 0 && location.getName().equalsIgnoreCase("unknown") == false) {
				locations.add(location.getName());
			}
		}
		addOperatorField(MappingMessages.getLocation(), MappingMessages.getLocationKeyword(), MappingMessages.getLocationInfo(), FieldType.MULTICHOICE, 
							locations.toArray(new String[locations.size()]));
		//NEWS
		addOperatorField(MappingMessages.getNews(), MappingMessages.getNewsKeyword(), MappingMessages.getNewsInfo(), FieldType.PLAINTEXT);
		//VIDEO
		addOperatorField(MappingMessages.getVideo(), MappingMessages.getVideoKeyword(), MappingMessages.getVideoInfo(), FieldType.PLAINTEXT);
		//CONTACT
		addOperatorField(MappingMessages.getFirstName(), MappingMessages.getFirstNameKeyword(), MappingMessages.getFirstNameInfo(), FieldType.PLAINTEXT);
		addOperatorField(MappingMessages.getLastName(), MappingMessages.getLastNameKeyword(), MappingMessages.getLastNameInfo(), FieldType.PLAINTEXT);
		addOperatorField(MappingMessages.getEmail(), MappingMessages.getEmailKeyword(), MappingMessages.getEmailInfo(), FieldType.PLAINTEXT);
	}
	
	/**
	 * Handle incoming FrontlineEventNotification
	 */
	@SuppressWarnings("unchecked")
	public void notify(FrontlineEventNotification notification) {
		if (notification instanceof DatabaseEntityNotification) {
			DatabaseEntityNotification databaseEntityNotification = (DatabaseEntityNotification)notification;
			if (databaseEntityNotification.getDatabaseEntity() instanceof FieldResponse) {
				FieldResponse fieldResponse = (FieldResponse)databaseEntityNotification.getDatabaseEntity();
				if (fieldDictionary.containsKey(fieldResponse.getMappingKeyword())) {
					Field field = fieldResponse.getMapping();
					LOG.debug("Ushahidi Field Received [%s, %s, %s]", field.getName(), field.getKeyword(), field.getType());
				}
			}
		}
	}
	
	/**
	 * Create Ushahidi-specific Operator fields
	 * @param name name
	 * @param keyword keyword
	 * @param infoSnippet info snippet
	 * @param type operator type
	 * @param schema operator schema
	 */
	private void addOperatorField(String name, String keyword, String infoSnippet, String type) {
		addOperatorField(name, keyword, infoSnippet, type, null);
	}
	
	/**
	 * Create Ushahidi-specific Operator fields
	 * @param name name
	 * @param keyword keyword
	 * @param infoSnippet info snippet
	 * @param type operator type
	 * @param schema operator schema
	 * @param choices list of choices
	 */
	private void addOperatorField(String name, String keyword, String infoSnippet, String type, String [] choices) {
		try {
			List<String> choiceList = choices != null ? Arrays.asList(choices) : null;
			Field field = FieldMappingFactory.createField(name, keyword, infoSnippet, type, null, choiceList);
			this.fieldMappingDao.saveFieldMapping(field);
			LOG.debug("Field Created [%s, %s, %s]", field.getName(), field.getKeyword(), field.getType());
			this.fieldDictionary.put(keyword, field);
		} 
		catch (DuplicateKeyException ex) {
			LOG.error("Field Loaded [%s, %s, %s]", name, keyword, type);
			Field field = this.fieldMappingDao.getFieldForKeyword(keyword);
			this.fieldDictionary.put(keyword, field);
		}
		catch (Exception ex) {
			Field field = this.fieldMappingDao.getFieldForKeyword(keyword);
			LOG.error("Field Loaded [%s, %s, %s]", name, keyword, type);
			this.fieldDictionary.put(keyword, field);
		}
	}
	
	/**
	 * Get ResourceMapperPluginController
	 * @param frontlineController ResourceMapperPluginController
	 * @return ResourceMapperPluginController
	 */
	private ResourceMapperPluginController getPluginController(FrontlineSMS frontlineController) {
		for (PluginController pluginController : frontlineController.getPluginManager().getPluginControllers()) {
			if (pluginController instanceof ResourceMapperPluginController) {
				return (ResourceMapperPluginController)pluginController;
			}
		}
		return null;
	}
}