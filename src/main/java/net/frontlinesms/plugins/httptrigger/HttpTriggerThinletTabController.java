/**
 * 
 */
package net.frontlinesms.plugins.httptrigger;

import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

/**
 * @author Alex
 */
public class HttpTriggerThinletTabController implements ThinletUiEventHandler {

//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/** Controller for the plugin */
	private final HttpTriggerPluginController httpTriggerController;
	/** Thinlet UI controller */
	private final UiGeneratorController uiController;
	/** Thinlet tab component which this class controls UI methods for. */
	private Object httpTriggerTab;

//> CONSTRUCTORS
	/**
	 * @param httpTriggerController value for {@link #httpTriggerController}
	 * @param uiController value for {@link #uiController}
	 */
	public HttpTriggerThinletTabController(HttpTriggerPluginController httpTriggerController, UiGeneratorController uiController) {
		this.httpTriggerController = httpTriggerController;
		this.uiController = uiController;
	}

	/** @param httpTriggerTab value for {@link #httpTriggerTab} */
	public void setTabComponent(Object httpTriggerTab) {
		this.httpTriggerTab = httpTriggerTab;
	}
	
//> PUBLIC UI METHODS
	/**
	 * The "start" button has been clicked on the UI.  Try to get the details of the listener
	 * setup, and start it on the suggested port.
	 */
	public void startListener(String portNumberAsString) {
		int portNumber;
		try {
			portNumber = Integer.parseInt(portNumberAsString);
		} catch(NumberFormatException ex) {
			// Port number failed to parse.  Warn the user and do not change the state of the listener 
			this.uiController.alert("This is not a valid port number."); // FIXME i18n
			return;
		}

		// Stop the old listener, if one is running
		this.httpTriggerController.stopListener();
		
		// Start the new listener
		this.httpTriggerController.startListener(portNumber);
		
		// Disable the start button and enable the stop button
		uiController.setEnabled(getStartButton(), false);
		uiController.setEnabled(getStopButton(), true);
	}
	
	public void stopListener() {
		// Stop the listener
		this.httpTriggerController.stopListener();
		
		// Disable the stop button and enable the start button
		uiController.setEnabled(getStartButton(), true);
		uiController.setEnabled(getStopButton(), false);
	}
	
	public void removeAll(Object listComponent) {
		this.uiController.removeAll(listComponent);
	}
	
//> ACCESSORS
	/** @return Thinlet button for starting the listener */
	private Object getStartButton() {
		return uiController.find(this.httpTriggerTab, "btStart");
	}
	/** @return Thinlet button for stopping */
	private Object getStopButton() {
		return uiController.find(this.httpTriggerTab, "btStop");
	}
	/** @return Thinlet list containing log entries */
	private Object getLogList() {
		return uiController.find(this.httpTriggerTab, "lsHttpTriggerLog");
	}
	
//> EVENT LISTENER METHODS
	/**
	 * Adds a new item to the log list.  The item is added at the top of the list. 
	 * @param message Message to log
	 * @see HttpTriggerEventListener#log(String)
	 */
	public void log(String message) {
		// TODO add timestamp to this list item
		uiController.add(getLogList(), uiController.createListItem(message, null), 0);
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
