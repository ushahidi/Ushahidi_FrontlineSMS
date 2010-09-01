package com.ushahidi.plugins.mapping.ui;

import java.awt.Color;
import java.awt.Component;

import org.apache.log4j.Logger;

import thinlet.ThinletText;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.FrontlineUtils;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.Icon;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

@SuppressWarnings("serial")
public class ReportsPanelHandler extends ExtendedThinlet implements ThinletUiEventHandler {

	private static final String UI_PANEL_XML = "/ui/plugins/mapping/reportsPanel.xml";
	
	public static Logger LOG = FrontlineUtils.getLogger(ReportsPanelHandler.class);	
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final IncidentDao incidentDao;
	private final CategoryDao categoryDao;
	private final MappingSetupDao mappingSetupDao;
	
	private Object mainPanel;
	private final Object tblReports;
	private final Object cbxCategories;
	private final Object txtSearch;
	
	private final String SEARCH_PLACEHOLDER = "Filter incidents...";
	private final String SHOW_ALL_CATEGORIES = "-- Show All Categories --";
	
	public ReportsPanelHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		this.incidentDao = pluginController.getIncidentDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		this.mainPanel = this.ui.loadComponentFromFile(UI_PANEL_XML, this);
		
		this.tblReports = this.find(this.mainPanel, "tblReports");
		this.cbxCategories = this.find(this.mainPanel, "cbxCategories");
		this.txtSearch = this.find(this.mainPanel, "txtSearch");
	}
	
	public Object getMainPanel() {
		return this.mainPanel;
	}
	
	public void init() {
		if (this.incidentDao.getCount() > 0) {
			this.removeAll(this.tblReports);
			for(Incident incident: this.incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
				add(this.tblReports, getRow(incident));
			}
		}
		this.removeAll(this.cbxCategories);
		this.add(this.cbxCategories, this.createComboboxChoice(SHOW_ALL_CATEGORIES, null));
		for(Category category: categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			this.add(this.cbxCategories, this.createComboboxChoice(category.getTitle(), category));
		}
		this.setSelectedIndex(this.cbxCategories, 0);
		this.setText(this.txtSearch, SEARCH_PLACEHOLDER);
	}
	
	public void search(Object textField, Object comboBox) {
		Object selectedItem =  this.getSelectedItem(comboBox);
		Category category = selectedItem != null ? this.getAttachedObject(selectedItem, Category.class) : null;
		String searchText = this.getText(textField).toLowerCase();
		this.removeAll(this.tblReports);
		for(Incident incident: this.incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
			if (category == null || incident.hasCategory(category)) {
				if ("".equalsIgnoreCase(searchText) || SEARCH_PLACEHOLDER.equalsIgnoreCase(searchText) || incident.getTitle().toLowerCase().indexOf(searchText) > -1) {
					this.add(this.tblReports, getRow(incident));
				}
			}
		}
		this.repaint(this.tblReports);
	}
	
	public void focusGained(Object textfield) {
		String searchText = this.ui.getText(textfield);
		if (searchText.equalsIgnoreCase(SEARCH_PLACEHOLDER)) {
			this.setText(textfield, "");
		}
	}
	
	public void focusLost(Object textfield) {
		String searchText = this.ui.getText(textfield);
		if (searchText == null || searchText.length() == 0) {
			this.setText(textfield, SEARCH_PLACEHOLDER);
		}
	}
	
	/**
	 * Show the details of the selected report item
	 * @param item
	 */
	public void showReportDetails(Object item){
		Incident incident = (Incident)getAttachedObject(item);
		if (incident != null) {
			ReportDialogHandler reportDialog = new ReportDialogHandler(this.pluginController, this.frontlineController, this.ui);
			reportDialog.showDialog(incident);
		}
	}
	
	/**
	 * Returns a row with an incident attached to it
	 * 
	 * @param incident see {@link #Incident}
	 * 
	 * @return
	 */
	public Object getRow(Incident incident){
		Object row = createTableRow(incident);
		createTableCell(row, incident.isMarked() == false);
		createTableCell(row, incident.isVerified());
		createTableCell(row, incident.getTitle());
		createTableCell(row, incident.getLocation().getName());
		createTableCell(row, InternationalisationUtils.getDateFormat().format(incident.getIncidentDate()));
		createTableCell(row, incident.getCategoryNames());
		createTableCell(row, incident.getDescription());
		return row;
	}
	
	private void createTableCell(Object row, boolean checked) {
		if (checked) {
			Object cell = this.createTableCell("");
			this.setIcon(cell, Icon.TICK);
			this.setChoice(cell, ThinletText.ALIGNMENT, ThinletText.CENTER);
			this.add(row, cell);
		}
		else {
			this.add(row, this.ui.createTableCell(""));
		}
	}
	
}