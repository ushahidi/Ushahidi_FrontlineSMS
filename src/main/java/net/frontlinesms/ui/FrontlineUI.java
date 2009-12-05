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
import java.util.LinkedList;

import net.frontlinesms.*;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.i18n.LanguageBundle;

import org.apache.log4j.Logger;

import thinlet.*;

/**
 * Base UI used for FrontlineSMS.
 */
@SuppressWarnings("serial")
public abstract class FrontlineUI extends ExtendedThinlet implements ThinletUiEventHandler {
	
//> CONSTANTS
	/** Logging object */
	private static Logger LOG = Utils.getLogger(FrontlineUI.class);
	
//> UI DEFINITION FILES
	/** Thinlet UI layout File: alert popup box */
	protected static final String UI_FILE_ALERT = "/ui/dialog/alert.xml";
	/** Thinlet UI layout File: file choosing dialog */
	protected static final String UI_FILE_FILE_CHOOSER_FORM = "/ui/dialog/fileChooserForm.xml";

//> UI COMPONENTS
	private static final String COMPONENT_FILE_CHOOSER_LIST = "fileChooser_list";
	private static final String COMPONENT_LABEL_DIRECTORY = "lbDirectory";
	/** Component of {@link #UI_FILE_ALERT} which contains the message to display */
	private static final String COMPONENT_ALERT_MESSAGE = "alertMessage";
	
//> INSTANCE PROPERTIES
	/** The language bundle currently in use */
	public static LanguageBundle currentResourceBundle;
	/** Frame launcher that this UI instance is displayed within.  We need to keep a handle on it so that we can dispose of it when we quit or change UI modes. */
	protected FrameLauncher frameLauncher;
	/** The {@link FrontlineSMS} instance that this UI is attached to. */
	protected FrontlineSMS frontlineController;

	/**
	 * Gets the icon for a specific language bundle
	 * @param languageBundle
	 * @return the flag image for the language bundle, or <code>null</code> if none could be found.
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
	 * @param filename path of the UI XML file to load from
	 * @return thinlet component loaded from the file
	 */
	protected Object loadComponentFromFile(String filename) {
		return loadComponentFromFile(filename, this);
	}
	
	/**
	 * Loads a Thinlet UI descriptor from an XML file and sets the provided event handler.
	 * If there are any problems loading the file, this will log Throwables thrown and 
	 * allow the program to continue running.
	 * @param filename path of the UI XML file to load from
	 * @param thinletEventHandler event handler for the UI component
	 * @return thinlet component loaded from the file
	 */
	public Object loadComponentFromFile(String filename, ThinletUiEventHandler thinletEventHandler) {
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
	
	/**
	 * Event fired when the browse button is pressed, during an export action. 
	 * This method opens a fileChooser, showing only directories.
	 * The user will select a directory and write the file name he/she wants.
	 * @param textFieldToBeSet The text field whose value should be sert to the chosen file
	 */
	public void showSaveModeFileChooser(Object textFieldToBeSet) {
		showFileChooser(textFieldToBeSet, FrontlineSMSConstants.SAVE_MODE);
	}
	
	/**
	 * Event fired when the browse button is pressed, during an import action. 
	 * This method opens a fileChooser, showing directories and files.
	 * The user will select a directory and write the file name he/she wants.
	 * @param textFieldToBeSet The text field whose value should be sert to the chosen file
	 */
	public void showOpenModeFileChooser(Object textFieldToBeSet) {
		showFileChooser(textFieldToBeSet, FrontlineSMSConstants.OPEN_MODE);
	}
	/**
	 * This method opens a fileChooser, showing directories and files.
	 * @param textFieldToBeSet The text field whose value should be sert to the chosen file
	 * @param fileMode either {@link FrontlineSMSConstants#OPEN_MODE} or {@link FrontlineSMSConstants#SAVE_MODE}
	 */
	private void showFileChooser(Object textFieldToBeSet, String fileMode) {
		Object fileChooserDialog = loadComponentFromFile(UI_FILE_FILE_CHOOSER_FORM);
		setAttachedObject(fileChooserDialog, textFieldToBeSet);
		putProperty(fileChooserDialog, FrontlineSMSConstants.PROPERTY_TYPE, fileMode);
		addFilesToList(new File(ResourceUtils.getUserHome()), find(fileChooserDialog, COMPONENT_FILE_CHOOSER_LIST), fileChooserDialog);
		add(fileChooserDialog);
	}
	
	/**
	 * Adds the files under the desired directory to the file list in the file chooser dialog.
	 * @param parent 
	 * @param list 
	 * @param dialog 
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
	 * @param list 
	 * @param dialog 
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
	 * @param fileList 
	 * @param dialog 
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
	 * @param tfFilename 
	 * @param list 
	 * @param dialog 
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
	 * Method called when the user finishes to browse files and select one to be the export file.
	 * @param list 
	 * @param dialog 
	 * @param filename 
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
	
	/**
	 * Opens a link in the system browser
	 * @param url the url to open
	 * @see Utils#openExternalBrowser(String)
	 */
	public void openBrowser(String url) {
		Utils.openExternalBrowser(url);
	}

	/**
	 * Opens a page of the help manual
	 * @param page The name of the help manual page, including file extension.
	 */
	public void showHelpPage(String page) {
		String url = "help" + File.separatorChar
			+ page;
		Utils.openExternalBrowser(url);
	}
	
	/**
	 * Shows an error dialog informing the user that an unhandled error has occurred.
	 */
	@Override
	protected void handleException(Throwable throwable) {
		LOG.error("Unhandled exception from thinlet.", throwable);
		ErrorUtils.showErrorDialog("Unexpected error", "There was an unexpected error.", throwable, false);
	}
	
	/**
	 * Reloads the ui.
	 * @param useNewFrontlineController <code>true</code> if a new {@link FrontlineSMS} should be isntantiated; <code>false</code> if the current one should be reused
	 */
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
				new UiGeneratorController(new FrontlineSMS(new ThinletDatabaseConnectionTestHandler()), false);
			} else {
				new UiGeneratorController(frontlineController, false);
			}
			
		} catch(Throwable t) {
			LOG.error("Unable to reload frontlineSMS.", t);
		}
	}
	
	
}
