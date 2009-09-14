/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.ui;

import java.awt.Image;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import net.frontlinesms.*;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.i18n.LanguageBundle;

import org.apache.log4j.Logger;

import thinlet.*;

public abstract class FrontlineUI extends Thinlet {
	private static Logger LOG = Utils.getLogger(FrontlineUI.class);
	private static final long serialVersionUID = -7786460043284936698L;
	public static LanguageBundle currentResourceBundle;

	/** Frame launcher that this UI instance is displayed within.  We need to keep a handle on it so that we can dispose of it when we quit or change UI modes. */
	protected FrameLauncher frameLauncher;
	protected FrontlineSMS frontlineController;
	
	protected static final String UI_FILE_HOME = "/ui/frontline.xml";
	protected static final String UI_FILE_NEW_GROUP_FORM = "/ui/dialog/newGroupForm.xml";
	protected static final String UI_FILE_DATE_PANEL = "/ui/dialog/datePanel.xml";
	protected static final String UI_FILE_PENDING_MESSAGES_FORM = "/ui/dialog/pendingMessagesDialog.xml";
	protected static final String UI_FILE_EXPORT_WIZARD_FORM = "/ui/dialog/exportWizardForm.xml";
	protected static final String UI_FILE_IMPORT_CONTACTS_FORM = "/ui/dialog/importContactsForm.xml";
	protected static final String UI_FILE_EXPORT_WIZARD_CONTACT = "/ui/dialog/panelExportContact.xml";
	protected static final String UI_FILE_EXPORT_WIZARD_MESSAGE = "/ui/dialog/panelExportMessage.xml";
	protected static final String UI_FILE_EXPORT_WIZARD_KEYWORD = "/ui/dialog/panelExportKeyword.xml";
	protected static final String UI_FILE_COMPOSE_MESSAGE_FORM = "/ui/dialog/composeMessageForm.xml";
	protected static final String UI_FILE_GROUP_SELECTER = "/ui/dialog/groupSelecter.xml";
	protected static final String UI_FILE_CONTACT_SELECTER = "/ui/dialog/contactSelecter.xml";
	protected static final String UI_FILE_NEW_KEYWORD_FORM = "/ui/dialog/newKeywordForm.xml";
	protected static final String UI_FILE_NEW_KACTION_REPLY_FORM = "/ui/dialog/newKActionReplyForm.xml";
	protected static final String UI_FILE_NEW_KACTION_FORWARD_FORM = "/ui/dialog/newKActionForwardForm.xml";
	protected static final String UI_FILE_NEW_KACTION_EXTERNAL_COMMAND_FORM = "/ui/dialog/externalCommandDialog.xml";
	protected static final String UI_FILE_NEW_KACTION_EMAIL_FORM = "/ui/dialog/emailDialog.xml";
	protected static final String UI_FILE_NEW_KACTION_SURVEY_FORM = "/ui/dialog/surveyDialog.xml";
	protected static final String UI_FILE_EDIT_KEYWORD_LIST_FORM = "/ui/dialog/editKeywordListDialog.xml";
	protected static final String UI_FILE_EMAIL_ACCOUNTS_SETTINGS_FORM = "/ui/dialog/emailServerConfigDialog.xml";
	protected static final String UI_FILE_SMS_SERVICES_ACCOUNTS_SETTINGS_FORM = "/ui/dialog/smsHttpServerConfigDialog.xml";
	protected static final String UI_FILE_SMS_SERVICES_ACCOUNTS_INTELLISMS_SETTINGS_FORM = "/ui/dialog/smsHttpServerIntelliSmsConfigDialog.xml";
	protected static final String UI_FILE_NEW_TASK_FORM = "/ui/dialog/scheduleTaskDialog.xml";
	protected static final String UI_FILE_ALERT = "/ui/dialog/alert.xml";
	protected static final String UI_FILE_EXPORT_DIALOG_FORM = "/ui/dialog/exportDialogForm.xml";
	protected static final String UI_FILE_FILE_CHOOSER_FORM = "/ui/dialog/fileChooserForm.xml";
	protected static final String UI_FILE_PHONE_SETTINGS_FORM = "/ui/dialog/phoneSettingsForm.xml";
	protected static final String UI_FILE_SMS_HTTP_SERVICE_SETTINGS_FORM = "/ui/dialog/smsHttpServiceSettings.xml";
	protected static final String UI_FILE_EMAIL_ACCOUNT_FORM = "/ui/dialog/emailAccountSettings.xml";
	protected static final String UI_FILE_DELETE_OPTION_DIALOG_FORM = "/ui/dialog/deleteOptionDialogForm.xml";
	protected static final String UI_FILE_CONNECTION_WARNING_FORM = "/ui/dialog/connectionWarningForm.xml";
	protected static final String UI_FILE_CONFIRMATION_DIALOG_FORM = "/ui/dialog/confirmationDialog.xml";
	protected static final String UI_FILE_CREATE_CONTACT_FORM = "/ui/dialog/createContactDialog.xml";
	protected static final String UI_FILE_HISTORY_FORM = "/ui/dialog/historyForm.xml";
	protected static final String UI_FILE_MESSAGE_PANEL = "/ui/dialog/messagePanel.xml";
	// FIXME this should probably be abstracted via a getter in UIGC or similar
	public static final String UI_FILE_PAGE_PANEL = "/ui/dialog/pagePanel.xml";
	protected static final String UI_FILE_ABOUT_PANEL = "/ui/dialog/about.xml";
	protected static final String UI_FILE_SENDER_NAME_PANEL = "/ui/dialog/senderNamePanel.xml";
	protected static final String UI_FILE_MSG_DETAILS_FORM = "/ui/dialog/messageDetailsDialog.xml";
	protected static final String UI_FILE_INCOMING_NUMBER_SETTINGS_FORM = "/ui/dialog/incomingNumberSettingsDialog.xml";
	protected static final String UI_FILE_USER_DETAILS_DIALOG = "/ui/dialog/userDetailsDialog.xml";
	
	//Advanced view
	protected static final String UI_FILE_HOME_TAB = "/ui/advanced/homeTab.xml";
	protected static final String UI_FILE_CONTACTS_TAB = "/ui/advanced/contactsTab.xml";
	//keyword panel
	protected static final String UI_FILE_KEYWORDS_TAB = "/ui/advanced/keywordsTab.xml";
	protected static final String UI_FILE_KEYWORDS_SIMPLE_VIEW = "/ui/advanced/keywordSimpleView.xml";
	protected static final String UI_FILE_KEYWORDS_ADVANCED_VIEW = "/ui/advanced/keywordAdvancedView.xml";
	
	protected static final String UI_FILE_MESSAGES_TAB = "/ui/advanced/messagesTab.xml";
	protected static final String UI_FILE_EMAILS_TAB = "/ui/advanced/emailsTab.xml";
	protected static final String UI_FILE_PHONES_TAB = "/ui/advanced/phonesTab.xml";
	
	//Classic view
	protected static final String UI_FILE_CONTACT_MANAGER_TAB = "/ui/classic/contactManagerTab.xml";
	protected static final String UI_FILE_SURVEY_MANAGER_TAB = "/ui/classic/surveyManagerTab.xml";
	protected static final String UI_FILE_SURVEY_ANALYST_TAB = "/ui/classic/surveyAnalystTab.xml";
	protected static final String UI_FILE_SEND_CONSOLE_TAB = "/ui/classic/sendConsoleTab.xml";
	protected static final String UI_FILE_MESSAGE_TRACKER_TAB = "/ui/classic/messageTrackerTab.xml";
	protected static final String UI_FILE_RECEIVE_CONSOLE_TAB = "/ui/classic/receiveConsoleTab.xml";
	protected static final String UI_FILE_REPLY_MANAGER_TAB = "/ui/classic/replyManagerTab.xml";
	protected static final String UI_FILE_PHONE_MANAGER_TAB = "/ui/classic/phoneManagerTab.xml";

	/**
	 * Gets the icon for a specific language bundle
	 * @param languageBundle
	 * @return
	 */
	protected Image getFlagIcon(LanguageBundle languageBundle) {
		String country = languageBundle.getCountry();
		String flagFile = country != null ? "/icons/flags/" + country + ".png" : null;
		return country == null ? null : getIcon(flagFile);
	}
	
	/**
	 * Loads a Thinlet UI descriptor from an XML file.  If there are any
	 * problems loading the file, this will log Throwables thrown and 
	 * allow the program to continue running.
	 * 
	 * {@link #loadComponentFromFile(String, Object)} should always be used by external handlers in preference to this.
	 * @param fileName
	 * @return
	 */
	protected Object loadComponentFromFile(String filename) {
		return loadComponentFromFile(filename, this);
	}
	
	/**
	 * Loads a Thinlet UI descriptor from an XML file and sets the provided event handler.
	 * If there are any problems loading the file, this will log Throwables thrown and 
	 * allow the program to continue running.
	 * @param filename
	 * @param thinletEventHandler
	 * @return
	 */
	public Object loadComponentFromFile(String filename, Object thinletEventHandler) {
		LOG.trace("ENTER");
		try {
			LOG.debug("Filename [" + filename + "]");
			LOG.trace("EXIT");
			return parse(filename, thinletEventHandler);
		} catch(Throwable t) {
			LOG.error("Error parsing file [" + filename + "]", t);
			LOG.trace("EXIT");
			throw new RuntimeException(t);
		}
	}
	
	private static final String COMPONENT_FILE_CHOOSER_LIST = "fileChooser_list";
	
	/**
	 * Event fired when the browse button is pressed, during an export action. 
	 * This method opens a fileChooser, showing only directories.
	 * The user will select a directory and write the file name he/she wants.
	 */
	public void showSaveModeFileChooser(Object textFieldToBeSet) {
		Object fileChooserDialog = loadComponentFromFile(UI_FILE_FILE_CHOOSER_FORM);
		setAttachedObject(fileChooserDialog, textFieldToBeSet);
		putProperty(fileChooserDialog, FrontlineSMSConstants.PROPERTY_TYPE, FrontlineSMSConstants.SAVE_MODE);
		addFilesToList(new File(System.getProperty(FrontlineSMSConstants.PROPERTY_USER_HOME)), find(fileChooserDialog, COMPONENT_FILE_CHOOSER_LIST), fileChooserDialog);
		add(fileChooserDialog);
	}
	
	/**
	 * Event fired when the browse button is pressed, during an import action. 
	 * This method opens a fileChooser, showing directories and files.
	 * The user will select a directory and write the file name he/she wants.
	 */
	public void showOpenModeFileChooser(Object textFieldToBeSet) {
		Object fileChooserDialog = loadComponentFromFile(UI_FILE_FILE_CHOOSER_FORM);
		setAttachedObject(fileChooserDialog, textFieldToBeSet);
		putProperty(fileChooserDialog, FrontlineSMSConstants.PROPERTY_TYPE, FrontlineSMSConstants.OPEN_MODE);
		addFilesToList(new File(System.getProperty(FrontlineSMSConstants.PROPERTY_USER_HOME)), find(fileChooserDialog, COMPONENT_FILE_CHOOSER_LIST), fileChooserDialog);
		add(fileChooserDialog);
	}
	
	private static final String COMPONENT_LABEL_DIRECTORY = "lbDirectory";
	
	/**
	 * Adds the files under the desired directory to the file list in the
	 * file chooser dialog.
	 */
	public void addFilesToList(File parent, Object list, Object dialog) {
		LOG.trace("ENTER");
		LOG.debug("Adding files under [" + parent.getAbsolutePath() + "]");
		removeAll(list);
		setAttachedObject(list, parent);
		setText(find(dialog, COMPONENT_LABEL_DIRECTORY), parent.getAbsolutePath());
		LinkedList<File> files = new LinkedList<File>();
		for (File f : parent.listFiles()) {
			files.add(f);
		}
		Collections.sort(files, new Utils.FileComparator());
		for (File child : files) {
			if (child.isDirectory() || (!child.isDirectory() && getProperty(dialog, FrontlineSMSConstants.PROPERTY_TYPE).equals(FrontlineSMSConstants.OPEN_MODE))) {
				Object item = createListItem(child.getName(), child);
				setAttachedObject(item, child);
				if (child.isDirectory()) {
					LOG.debug("Directory [" + child.getAbsolutePath() + "]");
					setIcon(item, Icon.FOLDER_CLOSED);
				} else {
					LOG.debug("File [" + child.getAbsolutePath() + "]");
					setIcon(item, Icon.FILE);
				}
				add(list, item);
			}
		}
		LOG.trace("EXIT");
	}
	
	/**
	 * Go up a directory, if possible, and show its files in the list.
	 */
	public void goUp(Object list, Object dialog) {
		Object file = getAttachedObject(list);
		File fileInDisk = (File) file;
		if (fileInDisk.getParent() == null) {
			alert(InternationalisationUtils.getI18NString(FrontlineSMSConstants.MESSAGE_IMPOSSIBLE_TO_GO_UP_A_DIRECTORY));
		} else {
			addFilesToList(fileInDisk.getParentFile(), list, dialog);
		}
	}

	private static final String COMPONENT_ALERT_MESSAGE = "alertMessage";

	/**
	 * Popup an alert to the user with the supplied message.
	 * @param alertMessage
	 */
	public void alert(String alertMessage) {
		Object alertDialog = loadComponentFromFile(UI_FILE_ALERT);
		setText(find(alertDialog, COMPONENT_ALERT_MESSAGE), alertMessage);
		add(alertDialog);
	}
	
	/**
	 * Removes the supplied dialog from the application.
	 * 
	 * @param dialog
	 */
	public void removeDialog(Object dialog) {
		remove(dialog);
	}
	
	/**
	 * Handles the double-click action in the file chooser list. Double-clicking
	 * in a directory means to get into it. However, double-clicking a file during an
	 * import action, means to select it as the desired file.
	 */
	public void fileList_doubleClicked(Object fileList, Object dialog) {
		LOG.trace("ENTER");
		Object selected = getSelectedItem(fileList);
		Object file = getAttachedObject(selected);
		if (file instanceof File) {
			File f = (File) file;
			if (f.isDirectory()) {
				LOG.debug("Selected directory [" + f.getAbsolutePath() + "]");
				addFilesToList(f, fileList, dialog);
			} else if (getProperty(dialog, FrontlineSMSConstants.PROPERTY_TYPE).equals(FrontlineSMSConstants.OPEN_MODE)) {
				LOG.debug("Selected file [" + f.getAbsolutePath() + "]");
				//This is the selected file.
				//TODO Should call the method to execute the import action.
				setText(getAttachedObject(dialog), f.getAbsolutePath());
				remove(dialog);
			}
		}
		LOG.trace("EXIT");
	}
	

	/**
	 * Enters the selected directory and show its files in the list.
	 */
	public void goToDir(Object tfFilename, Object list, Object dialog) {
		File file = new File(getString(tfFilename, Thinlet.TEXT));
		if (!file.exists()) {
			alert(InternationalisationUtils.getI18NString(FrontlineSMSConstants.MESSAGE_DIRECTORY_NOT_FOUND));
		} else {
			addFilesToList(file, list, dialog);
		}
		setText(tfFilename, "");
	}
	
	/**
	 * Method called when the user finishes to browse files and select one to be
	 * the export file.
	 */
	public void doSelection(Object list, Object dialog, String filename) {
		LOG.trace("ENTER");
		Object selected = getSelectedItem(list);
		if (selected == null) {
			selected = getAttachedObject(list);
		} else {
			selected = getAttachedObject(selected);
		}
		File sel = (File) selected;
		if (getProperty(dialog, FrontlineSMSConstants.PROPERTY_TYPE).equals(FrontlineSMSConstants.SAVE_MODE)) {	
			String filePath = sel.getAbsolutePath();
			if (!filePath.endsWith(File.separator)) {
				filePath += File.separator;
			}
			LOG.debug("Selected Directory [" + filePath + "]");
			setText(getAttachedObject(dialog), filePath + filename);
		} else {
			if (selected == null) {
				alert(InternationalisationUtils.getI18NString(FrontlineSMSConstants.MESSAGE_NO_FILE_SELECTED));
				LOG.trace("EXIT");
				return;
			}
			//This is the selected file.
			setText(getAttachedObject(dialog), sel.getAbsolutePath());
		}
		remove(dialog);
		LOG.trace("EXIT");
	}
	
	public void openBrowser(String url) {
		Utils.openExternalBrowser(url);
	}
	
	public void showHelpPage(String page) {
		String url = "help" + File.separatorChar
			+ page;
		Utils.openExternalBrowser(url);
	}
	
	@Override
	protected void handleException(Throwable throwable) {
		LOG.error("Unhandled exception from thinlet.", throwable);
		ErrorUtils.showErrorDialog("Unexpected error", "There was an unexpected error.", throwable, false);
	}
	
	final void reloadUI(boolean useNewFrontlineController) {
		this.frameLauncher.dispose();
		this.frameLauncher.setContent(null);
		this.frameLauncher = null;
		this.destroy();
		try {
			if (useNewFrontlineController) {
				// If we're using a new Frontline controller, we have to properly
				// shut down the last one so that ownership of any connected phones
				// is relinquished
				if (frontlineController != null) frontlineController.destroy();
				new UiGeneratorController(new FrontlineSMS(), false);
			} else {
				new UiGeneratorController(frontlineController, false);
			}
			
		} catch(Throwable t) {
			LOG.error("Unable to reload frontlineSMS.", t);
		}
	}
	
	
}
