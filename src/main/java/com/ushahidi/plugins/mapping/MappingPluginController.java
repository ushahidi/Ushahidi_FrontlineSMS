package com.ushahidi.plugins.mapping;

import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.LocationDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.ui.MappingUIController;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.FrontlineUtils;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.listener.IncomingMessageListener;
import net.frontlinesms.plugins.BasePluginController;
import net.frontlinesms.plugins.PluginControllerProperties;
import net.frontlinesms.plugins.PluginInitialisationException;
import net.frontlinesms.ui.UiGeneratorController;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author ekala
 *
 */
@PluginControllerProperties(name="Mapping", iconPath="/icons/map.png", i18nKey = "plugins.ushahidi",
        springConfigLocation="classpath:com/ushahidi/plugins/mapping/mapping-spring-hibernate.xml",
        hibernateConfigPath="classpath:com/ushahidi/plugins/mapping/mapping.hibernate.cfg.xml")
public class MappingPluginController extends BasePluginController implements IncomingMessageListener {
    /** Logger */
    private static final Logger LOG = FrontlineUtils.getLogger(MappingPluginController.class);

    //> INSTANCE variables
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
        this.frontlineController.addIncomingMessageListener(this);

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

    public void init(FrontlineSMS frontlineController) {
        this.frontlineController = frontlineController;
    }

    public void deinit() {
    	if (this.frontlineController != null) {
            this.frontlineController.removeIncomingMessageListener(this);
    	}
    	if (this.mappingUIController != null) {
            this.mappingUIController.shutdownUIController();
    	}
    }

    public Object initThinletTab(UiGeneratorController uiController) {
        mappingUIController = new MappingUIController(this, frontlineController, uiController);         
        mappingUIController.initUIController();
        return mappingUIController.getTab();
    }

    public void incomingMessageEvent(FrontlineMessage message) {
        LOG.debug("Incident report received");
        mappingUIController.handleIncomingMessage(message);
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
