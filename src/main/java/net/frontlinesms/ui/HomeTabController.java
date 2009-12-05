/**
 * 
 */
package net.frontlinesms.ui;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import net.frontlinesms.data.domain.Contact;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.i18n.LanguageBundle;

/**
 * Event handler for the Home tab and associated dialogs
 * @author Alex
 */
public class HomeTabController implements ThinletUiEventHandler {
//> STATIC CONSTANTS
	/** Limit of the number of events to be displayed on the home screen */
	static final int EVENTS_LIMIT = 30;
	
	/** UI XML File Path: the Home Tab itself */
	protected static final String UI_FILE_HOME_TAB = "/ui/core/home/homeTab.xml";
	/** UI XML File Path: settings dialog for the home tab */
	private static final String UI_FILE_HOME_TAB_SETTINGS = "/ui/dialog/homeTabSettingsDialog.xml";
	/** Thinlet Component Name: Home Tab: logo */
	private static final String COMPONENT_LB_HOME_TAB_LOGO = "lbHomeTabLogo";
	/** Thinlet Component Name: Settings dialog: checkbox indicating if the logo is visible */
	private static final String COMPONENT_CB_HOME_TAB_LOGO_VISIBLE = "cbHomeTabLogoVisible";
	/** Thinlet Component Name: Settings dialog: textfield inidicating the path of the image file for the logo */
	private static final String COMPONENT_TF_IMAGE_SOURCE = "tfImageSource";

	/** Default FrontlineSMS home logo */
	private static final String FRONTLINE_LOGO = "/icons/frontlineSMS_logo.png";


//> INSTANCE PROPERTIES
	/** Logging object */
	private final Logger log = Utils.getLogger(this.getClass());
	/** The {@link UiGeneratorController} that shows the tab. */
	private final UiGeneratorController uiController;
	/** The UI tab component */
	private final Object tabComponent;

//> CONSTRUCTORS
	/**
	 * Create a new instance of this class.
	 * @param uiController value for {@link #uiController}
	 */
	public HomeTabController(UiGeneratorController uiController) {
		this.uiController = uiController;
		this.tabComponent = uiController.loadComponentFromFile(UI_FILE_HOME_TAB, this);
	}

//> ACCESSORS
	/**
	 * Load the home tab from the XML, and initialise relevant fields.
	 * @return a newly-initialised instance of the home tab
	 */
	public Object getTab() {
		initialiseTab();
		return this.tabComponent;
	}

//> UI METHODS
	/** Show the settings dialog for the home tab. */
	public void showHomeTabSettings() {
		log.trace("ENTER");
		Object homeTabSettings = uiController.loadComponentFromFile(UI_FILE_HOME_TAB_SETTINGS, this);
		UiProperties uiProperties = UiProperties.getInstance();
		boolean visible = uiProperties.isHometabLogoVisible();
		String imageLocation = uiProperties.getHomtabLogoPath();
		log.debug("Visible? " + visible);
		log.debug("Image location [" + imageLocation + "]");
		uiController.setSelected(uiController.find(homeTabSettings, COMPONENT_CB_HOME_TAB_LOGO_VISIBLE), visible);
		if (imageLocation != null && imageLocation.length() > 0) {
			uiController.setText(uiController.find(homeTabSettings, COMPONENT_TF_IMAGE_SOURCE), imageLocation);
		}
		homeTabLogoVisibilityChanged(uiController.find(homeTabSettings, "pnImgSource"), visible);
		uiController.add(homeTabSettings);
		log.trace("EXIT");
	}
	
	/**
	 * Save the home tab settings from the settings dialog, and remove the dialog.
	 * @param panel
	 */
	public void saveHomeTabSettings(Object panel) {
		log.trace("ENTER");
		boolean visible = uiController.isSelected(uiController.find(panel, COMPONENT_CB_HOME_TAB_LOGO_VISIBLE));
		String imgSource = uiController.getText(uiController.find(panel, COMPONENT_TF_IMAGE_SOURCE));
		log.debug("Visible? " + visible);
		log.debug("Image location [" + imgSource + "]");
		UiProperties uiProperties = UiProperties.getInstance();
		uiProperties.setHometabLogoVisible(visible);
		if (imgSource == null) {
			// FIXME Move this NULL -> "" mapping into the setHometabLogoPath method
			imgSource = "";
		}
		uiProperties.setHometabLogoPath(imgSource);
		uiProperties.saveToDisk();
		uiController.remove(panel);
		log.trace("EXIT");
	}
	
	/**
	 * Changes the visibility of the home tab logo.
	 * @param panel
	 * @param visible <code>true</code> if the logo should be visible; <code>false</code> otherwise.
	 */
	public void homeTabLogoVisibilityChanged(Object panel, boolean visible) {
		uiController.setEnabled(panel, visible);
		for (Object obj : uiController.getItems(panel)) {
			uiController.setEnabled(obj, visible);
		}
	}

	/**
	 * Sets the phone number of the selected contact.
	 * 
	 * This method is triggered by the contact selected, as detailed in {@link #selectMessageRecipient()}.
	 * 
	 * @param contactSelecter_contactList
	 * @param dialog
	 */
	public void setRecipientTextfield(Object contactSelecter_contactList, Object dialog) {
		Object tfRecipient = uiController.find(this.tabComponent, UiGeneratorControllerConstants.COMPONENT_TF_RECIPIENT);
		Object selectedItem = uiController.getSelectedItem(contactSelecter_contactList);
		if (selectedItem == null) {
			uiController.alert(InternationalisationUtils.getI18NString(FrontlineSMSConstants.MESSAGE_NO_CONTACT_SELECTED));
			return;
		}
		Contact selectedContact = uiController.getContact(selectedItem);
		uiController.setText(tfRecipient, selectedContact.getMsisdn());
		uiController.remove(dialog);
		uiController.numberToSend = 1;
		uiController.updateCost();
	}

	/**
	 * Method which triggers showing of the contact selecter.
	 */
	public void selectMessageRecipient() {
		uiController.showContactSelecter(
				InternationalisationUtils.getI18NString(FrontlineSMSConstants.SENTENCE_SELECT_MESSAGE_RECIPIENT_TITLE),
				"setRecipientTextfield(contactSelecter_contactList, contactSelecter)",
				null,
				this);
	}
	
//> UI PASSTHRU METHODS TO UiGC
	/**
	 * @param page The file name of the help page
	 * @see UiGeneratorController#showHelpPage(String)
	 */
	public void showHelpPage(String page) {
		this.uiController.showHelpPage(page);
	}
	/**
	 * @param component Component whose contents are to be removed
	 * @see UiGeneratorController#removeAll()
	 */
	public void removeAll(Object component) {
		this.uiController.removeAll(component);
	}
	/**
	 * @param component The component to remove
	 * @see UiGeneratorController#removeDialog(Object)
	 */
	public void removeDialog(Object component) {
		this.uiController.removeDialog(component);
	}
	
//> INSTANCE HELPER METHODS	
	/**
	 * Refresh the contents of the tab.
	 */
	private void initialiseTab() {
		Object pnSend = uiController.find(this.tabComponent, UiGeneratorControllerConstants.COMPONENT_PN_SEND);
		Object pnMessage = new MessagePanelController(this.uiController).getPanel();
		uiController.add(pnSend, pnMessage);
		
		if (!UiProperties.getInstance().isHometabLogoVisible()) {
			uiController.remove(uiController.find(this.tabComponent, COMPONENT_LB_HOME_TAB_LOGO));
		} else {
			Object lbLogo = uiController.find(this.tabComponent, COMPONENT_LB_HOME_TAB_LOGO);
			String imageLocation = UiProperties.getInstance().getHomtabLogoPath();
			boolean useDefault = true;
			if (imageLocation != null && imageLocation.length() > 0) {
				// Absolute or relative path provided
				try {
					uiController.setIcon(lbLogo, ImageIO.read(new File(imageLocation)));
					useDefault = false;
				} catch (IOException e) {
					// We are unable to find the specified image, using the default
					log.warn("We are unable to find the specified image [" + imageLocation + "], using the default one.", e);
				}
			}
			if (useDefault) {
				// We go for the default one, inside the package
				uiController.setIcon(lbLogo, uiController.getIcon(FRONTLINE_LOGO));
			}
		}
		
		Object fastLanguageSwitch = uiController.find(this.tabComponent, "fastLanguageSwitch");
		int shown = 0;
		for (LanguageBundle languageBundle : InternationalisationUtils.getLanguageBundles()) {
			if(++shown > 10) break;
			Object button = uiController.createButton("", "changeLanguage(this)", this.tabComponent);
			uiController.setIcon(button, uiController.getFlagIcon(languageBundle));
			uiController.setString(button, "tooltip", languageBundle.getLanguage());
			uiController.setWeight(button, 1, 0);
			uiController.setChoice(button, "type", "link");
			uiController.setAttachedObject(button, languageBundle.getFilename());
			uiController.add(fastLanguageSwitch, button);
		}
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
