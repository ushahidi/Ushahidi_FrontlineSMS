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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.arcane.ArcaneDataImporter;
import net.frontlinesms.properties.PropertySet;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.i18n.LanguageBundle;
import thinlet.FrameLauncher;

import static net.frontlinesms.FrontlineSMSConstants.*;

/**
 * This class is responsible for showing the first time wizard and handling its events.
 * 
 * @author Carlos Eduardo Genz kadu(at)masabi(dot)com
 */
public class FirstTimeWizard extends FrontlineUI {
	private static final long serialVersionUID = 2582309305039938262L;
	
	private static final String COMPONENT_PN_BOTTOM = "pnBottom";
	private static final String COMPONENT_PN_INFO = "pnInfo";
	private static final String COMPONENT_LB_WAIT = "lbWait";
	private static final String COMPONENT_STATUS = "status";
	private static final String COMPONENT_PROGRESS = "progress";
	private static final String COMPONENT_LANGUAGES_LIST = "languagesList";
	
	private static final String COUNTRY_GB = "gb";
	
	private static final String UI_FILE_LANGUAGE_SELECTION = "/ui/wizard/languageSelect.xml";
	private static final String UI_FILE_USED_BEFORE = "/ui/wizard/usedBefore.xml";
	private static final String UI_FILE_MODE_SELECTION = "/ui/wizard/modeSelection.xml";
	private static final String UI_FILE_IMPORT_DATA = "/ui/wizard/importData.xml";
	private static final String UI_FILE_START_FORM = "/ui/wizard/startForm.xml";
	
	private static final String ICON_ADVANCED = "/icons/wizard/tabAdvanced.png";
	private static final String ICON_CLASSIC = "/icons/wizard/tabClassic.png";
	
	private Object currentPage;
	private int index = 0;
	private FrontlineSMS frontline;
	private List<Object> forms = new ArrayList<Object>();
	private FrameLauncher frameLauncher;
	
	private String viewMode;
	
	public FirstTimeWizard(FrontlineSMS frontline) {
		super();
		this.frontline = frontline;
		
		frameLauncher = new FrameLauncher(InternationalisationUtils.getI18NString(COMMON_FIRST_TIME_WIZARD), this, 510, 380, getIcon(Icon.FRONTLINE_ICON));
		frameLauncher.setResizable(false);
		showLanguageSelection();
	}

	/**
	 * Show the language selection form.
	 */
	private void showLanguageSelection() {
		Object language = loadComponentFromFile(UI_FILE_LANGUAGE_SELECTION);
		Object languagesList = find(language, COMPONENT_LANGUAGES_LIST);
		for (LanguageBundle languageBundle : InternationalisationUtils.getLanguageBundles()) {
			Object item = createListItem(languageBundle.getLanguage(), languageBundle.getFilename());
			setIcon(item, getFlagIcon(languageBundle));
			int index = -1;
			if (languageBundle.getCountry().equals(COUNTRY_GB)) {
				setSelected(item, true);
				index = 0;
			}
			add(languagesList, item, index);
		}
		if (currentPage != null) remove(currentPage);
		currentPage = language;
		add(language);
	}
	
	/**
	 * Method called when user change his option about the application mode.
	 * 
	 * @param isAdvanced
	 */
	public void modeChanged(boolean isAdvanced) {
		Object screenshot = find(currentPage, "screenshot");
		String iconPath = isAdvanced ? ICON_ADVANCED : ICON_CLASSIC;
		setIcon(screenshot, iconPath);
	}
	
	/**
	 * Method called when user make the mode selection.
	 * 
	 * @param isAdvanced
	 */
	public void selectedMode(boolean isAdvanced) {
		viewMode = isAdvanced ? NEW_MODE : CLASSIC_MODE;
		index++;
		showPage();
	}
	
	/**
	 * Save the selected language
	 * 
	 * @param list
	 */
	public void setLanguage(Object list) {
		Object sel = getSelectedItem(list);
		PropertySet appProperties = PropertySet.load(PROPERTIES_APP);
		String filename = getAttachedObject(sel).toString();
		appProperties.setProperty(PROPERTIES_LANGUAGE_FILENAME, filename);
		LanguageBundle languageBundle = InternationalisationUtils.getLanguageBundle(new File(ResourceUtils.getConfigDirectoryPath() + "languages/" + filename));
		FrontlineUI.currentResourceBundle = languageBundle;
		setResourceBundle(languageBundle.getProperties(), languageBundle.isRightToLeft());
		appProperties.saveToDisk();
		frameLauncher.setTitle(InternationalisationUtils.getI18NString(COMMON_FIRST_TIME_WIZARD));
		loadPages();
		showPage();
	}
	
	/**
	 * If the user never has used frontline, we go to the end of the wizard,
	 * otherwise we go one page further.
	 * 
	 * @param yes
	 */
	public void selectedUsedBefore(boolean yes) {
		index = yes ? index + 1 : forms.size() - 1; 
		viewMode = NEW_MODE;
		showPage();
	}
	
	/**
	 * Go to the previous page.
	 */
	public void goBack() {
		index--;
		showPage();
	}
	
	public void startImport() {
		Object progress = find(currentPage, COMPONENT_PROGRESS);
		Object status = find(currentPage, COMPONENT_STATUS);
		setInteger(progress, VALUE, 0);
		setBoolean(progress, VISIBLE, true);
		setBoolean(status, VISIBLE, true);
		setBoolean(find(currentPage, COMPONENT_LB_WAIT), VISIBLE, true);
		
		deactivate(find(currentPage, COMPONENT_PN_INFO));
		deactivate(find(currentPage, COMPONENT_PN_BOTTOM));
	}
	
	public void finishImport() {
		Object progress = find(currentPage, COMPONENT_PROGRESS);
		Object status = find(currentPage, COMPONENT_STATUS);
		setVisible(progress, false);
		setVisible(status, false);
		setVisible(find(currentPage, COMPONENT_LB_WAIT), false);
		
		activate(find(currentPage, COMPONENT_PN_INFO));
		activate(find(currentPage, COMPONENT_PN_BOTTOM));
	}
	
	public void setImportStatus(String key) {
		Object progress = find(currentPage, COMPONENT_STATUS);
		setText(progress, InternationalisationUtils.getI18NString(key));
	}
	
	public void selectedImportData(boolean isImport, final String path) {
		if (isImport) {
			new Thread(){
				public void run() {
					startImport();
					try {
						incProgress();
						setImportStatus(MESSAGE_IMPORTING_CONTACTS_GROUPS);
						ArcaneDataImporter importer = new ArcaneDataImporter(frontline, path);
						importer.importContactsAndGroups();
						incProgress();
						setImportStatus(MESSAGE_IMPORTING_KEYWORDACTIONS);
						importer.importKeywordActions();
						incProgress();
						setImportStatus(MESSAGE_IMPORTING_SENT_MESSAGES);
						importer.importSentMessages();
						incProgress();
						setImportStatus(MESSAGE_IMPORTING_RECEIVED_MESSAGES);
						importer.importReceivedMessages();
						finishImport();
						index++;
						showPage();
					} catch(IOException ex) {
						finishImport();
						alert(InternationalisationUtils.getI18NString(MESSAGE_IMPORT_FAILED));
					} 
				}
			}.start();
		} else {
			index++;
			showPage();
		}
	}
	
	public void startFrontline() throws Throwable {
		PropertySet appProperties = PropertySet.load(PROPERTIES_APP);
		appProperties.setProperty(PROPERTIES_SHOW_WIZARD, String.valueOf(false));
		appProperties.saveToDisk();
		
		PropertySet uiProperties = PropertySet.load(PROPERTIES_UI);
		uiProperties.setProperty(PROPERTIES_VIEW_MODE, viewMode);
		uiProperties.saveToDisk();
		
		frameLauncher.dispose();
		new UiGeneratorController(frontline, true);
	}
	
	public void incProgress() {
		Object progress = find(currentPage, COMPONENT_PROGRESS);
		setInteger(progress, VALUE, getInteger(progress, VALUE) + 1);
	}

	/**
	 * Show the page from current index.
	 */
	private void showPage() {
		remove(currentPage);
		if (index != -1) {
			currentPage = forms.get(index);
			add(currentPage);
		} else {
			showLanguageSelection();
		}
	}

	/**
	 * Loads all pages
	 */
	private void loadPages() {
		index = 0;
		forms.clear();
		forms.add(loadComponentFromFile(UI_FILE_USED_BEFORE));
		forms.add(loadComponentFromFile(UI_FILE_MODE_SELECTION));
		forms.add(loadComponentFromFile(UI_FILE_IMPORT_DATA));
		forms.add(loadComponentFromFile(UI_FILE_START_FORM));
	}
}
