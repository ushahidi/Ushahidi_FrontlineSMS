package com.ushahidi.plugins.mapping;

import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.ui.MappingUIController;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.Utils;
import net.frontlinesms.plugins.PluginController;
import net.frontlinesms.plugins.PluginInitialisationException;
import net.frontlinesms.ui.UiGeneratorController;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

public class MappingPluginController implements PluginController {
	/** Filename and path of the XML containing the mapping tab */
	private static final String XML_MAPPING_TAB = "/ui/plugins/mapping/mappingTab.xml";
	/** Logger */
	private static final Logger LOG = Utils.getLogger(MappingPluginController.class);
	
//>	INSTANCE variables
	private FrontlineSMS frontlineController;
	
	private MappingUIController mappingUIController;
	/** DAO for categories */
	private CategoryDao categoryDao;
	/** DAO for locations */
	private LocationDao locationDao;
	/** DAO for incidents */
	private IncidentDao incidentDao;
	/** DAO for mapping setup */
	private MappingSetupDao mappingSetupDao;
	
	public String getHibernateConfigPath() {
		return "classpath:com/ushahidi/plugins/mapping/mapping.hibernate.cfg.xml";
	}

	public String getSpringConfigPath() {
		return "classpath:com/ushahidi/plugins/mapping/mapping-spring-hibernate.xml";
	}

	public void init(FrontlineSMS frontlineController, ApplicationContext applicationContext) 
		throws PluginInitialisationException {
		this.frontlineController = frontlineController;
		
		try{
			locationDao = (LocationDao)applicationContext.getBean("locationDao");
			incidentDao = (IncidentDao)applicationContext.getBean("incidentDao");
			mappingSetupDao = (MappingSetupDao)applicationContext.getBean("mappingSetupDao");
			categoryDao = (CategoryDao)applicationContext.getBean("categoryDao");
		}catch(Throwable t){
			LOG.warn("Unable to initialize mapping plugin");
			throw new PluginInitialisationException(t);
		}
		
	}

	public String getName() {		
		return "Ushahidi";
	}
	
	public void init(FrontlineSMS frontlineController) {
		this.frontlineController = frontlineController;
	}
	

	public Object getTab(UiGeneratorController uiController) {
		mappingUIController = new MappingUIController(this, frontlineController, uiController);			
		return uiController.loadComponentFromFile(XML_MAPPING_TAB, mappingUIController);
	}

	public void initializePluginData() {
		mappingUIController.initUIController();
	}
	
	public LocationDao getLocationDao(){
		return locationDao;
	}
	
	public CategoryDao getCategoryDao(){
		return categoryDao;
	}
	
	public IncidentDao getIncidentDao(){
		return incidentDao;
	}
	
	public MappingSetupDao getMappingSetupDao(){
		return mappingSetupDao;
	}
	
}
