package com.ushahidi.plugins.mapping.ui;

import thinlet.ThinletText;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.utils.MappingLogger;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.Icon;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

@SuppressWarnings("serial")
public class ReportsPanelHandler extends ExtendedThinlet implements ThinletUiEventHandler {

	public static MappingLogger LOG = MappingLogger.getLogger(ReportsPanelHandler.class);
	
	private static final String UI_PANEL_XML = "/ui/plugins/mapping/reportsPanel.xml";
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final IncidentDao incidentDao;
	private final CategoryDao categoryDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final Object mainPanel;
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
		
		this.tblReports = this.ui.find(this.mainPanel, "tblReports");
		this.cbxCategories = this.ui.find(this.mainPanel, "cbxCategories");
		this.txtSearch = this.ui.find(this.mainPanel, "txtSearch");
	}
	
	public Object getMainPanel() {
		return this.mainPanel;
	}
	
	public void init() {
		if (incidentDao.getCount() > 0) {
			ui.removeAll(tblReports);
			for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
				LOG.debug("Loading incident %s", incident.getTitle());
				ui.add(tblReports, getRow(incident));
			}
		}
		ui.removeAll(cbxCategories);
		ui.add(cbxCategories, createComboboxChoice(SHOW_ALL_CATEGORIES, null));
		for(Category category : categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
			LOG.debug("Loading category %s", category.getTitle());
			ui.add(cbxCategories, createComboboxChoice(category.getTitle(), category));
		}
		ui.setSelectedIndex(cbxCategories, 0);
		ui.setText(txtSearch, SEARCH_PLACEHOLDER);
	}
	
	public void search(Object textField, Object comboBox) {
		Object selectedItem =  getSelectedItem(comboBox);
		Category category = selectedItem != null ? getAttachedObject(selectedItem, Category.class) : null;
		String searchText = ui.getText(textField).toLowerCase();
		LOG.debug("searchText=%s", searchText);
		ui.removeAll(tblReports);
		for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
			if (category == null || incident.hasCategory(category)) {
				if ("".equalsIgnoreCase(searchText) || SEARCH_PLACEHOLDER.equalsIgnoreCase(searchText) || incident.getTitle().toLowerCase().indexOf(searchText) > -1) {
					ui.add(tblReports, getRow(incident));
				}
			}
		}
	}
	
	public void focusGained(Object textfield) {
		String searchText = ui.getText(textfield);
		if (searchText.equalsIgnoreCase(SEARCH_PLACEHOLDER)) {
			ui.setText(textfield, "");
		}
	}
	
	public void focusLost(Object textfield) {
		String searchText = ui.getText(textfield);
		if (searchText == null || searchText.length() == 0) {
			ui.setText(textfield, SEARCH_PLACEHOLDER);
		}
	}
	
	/**
	 * Show the details of the selected report item
	 * @param item
	 */
	public void showReportDetails(Object item){
		Incident incident = (Incident)getAttachedObject(item);
		if (incident != null) {
			ReportDialogHandler reportDialog = new ReportDialogHandler(pluginController, frontlineController, ui);
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
		if (incident.getLocation() != null) {
			createTableCell(row, incident.getLocation().getName());
		}
		else {
			createTableCell(row, "");
		}
		createTableCell(row, InternationalisationUtils.getDateFormat().format(incident.getIncidentDate()));
		createTableCell(row, incident.getCategoryNames());
		createTableCell(row, incident.getDescription());
		return row;
	}
	
	private void createTableCell(Object row, boolean checked) {
		if (checked) {
			Object cell = this.createTableCell("");
			ui.setIcon(cell, Icon.TICK);
			ui.setChoice(cell, ThinletText.ALIGNMENT, ThinletText.CENTER);
			ui.add(row, cell);
		}
		else {
			ui.add(row, ui.createTableCell(""));
		}
	}
	
}