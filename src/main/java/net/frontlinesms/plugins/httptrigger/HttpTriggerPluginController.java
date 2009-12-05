/**
 * 
 */
package net.frontlinesms.plugins.httptrigger;

import org.springframework.context.ApplicationContext;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.plugins.PluginController;
import net.frontlinesms.plugins.PluginInitialisationException;
import net.frontlinesms.plugins.httptrigger.httplistener.HttpTriggerServer;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

/**
 * This plugin controls an HTTP listener for triggering SMS from outside FrontlineSMS.
 * @author Alex
 */
public class HttpTriggerPluginController implements PluginController, ThinletUiEventHandler, HttpTriggerEventListener {
//> STATIC CONSTANTS
	/** Filename and path of the XML for the HTTP Trigger tab. */
	private static final String UI_FILE_TAB = "/ui/plugins/httptrigger/httpTriggerTab.xml";

//> INSTANCE PROPERTIES
	private HttpTriggerListener httpListener;
	private HttpTriggerThinletTabController tabController;
	/** the {@link FrontlineSMS} instance that this plugin is attached to */
	private FrontlineSMS frontlineController;

//> CONSTRUCTORS

//> ACCESSORS
	/** @see net.frontlinesms.plugins.PluginController#getHibernateConfigPath() */
	public String getHibernateConfigPath() {
		// No hibernate config is required.
		return null;
	}

	/** @see net.frontlinesms.plugins.PluginController#getName() */
	public String getName() {
		return "HttpTrigger";
	}

	/** @see net.frontlinesms.plugins.PluginController#getSpringConfigPath() */
	public String getSpringConfigPath() {
		// For now, we have no spring config to do here
		return null;
	}

	/** @see net.frontlinesms.plugins.PluginController#getTab(net.frontlinesms.ui.UiGeneratorController) */
	public Object getTab(UiGeneratorController uiController) {
		this.tabController = new HttpTriggerThinletTabController(this, uiController);

		Object httpTriggerTab = uiController.loadComponentFromFile(UI_FILE_TAB, tabController);
		tabController.setTabComponent(httpTriggerTab);
		
		return httpTriggerTab;
	}

	/** @see net.frontlinesms.plugins.PluginController#init(net.frontlinesms.FrontlineSMS, org.springframework.context.ApplicationContext) */
	public void init(FrontlineSMS frontlineController, ApplicationContext applicationContext) throws PluginInitialisationException {
		this.frontlineController = frontlineController;
	}

	public void startListener(int portNumber) {
		this.stopListener();
		this.httpListener = new HttpTriggerServer(this, portNumber);
		this.httpListener.start();
	}

	public void stopListener() {
		if(this.httpListener != null) {
			this.httpListener.pleaseStop();
			this.log("Listener stopping: " + this.httpListener);
			this.httpListener = null;
		}
	}

//> INSTANCE HELPER METHODS
	
//> HTEL METHODS
	/** @see HttpTriggerEventListener#log(String) */
	public void log(String message) {
		this.tabController.log(message);
	}
	
	/** @see net.frontlinesms.plugins.httptrigger.HttpTriggerEventListener#sendSms(java.lang.String, java.lang.String) */
	public void sendSms(String toPhoneNumber, String message) {
		this.log("Sending SMS to " + toPhoneNumber + ": " + message);
		frontlineController.sendTextMessage(toPhoneNumber, message);
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
