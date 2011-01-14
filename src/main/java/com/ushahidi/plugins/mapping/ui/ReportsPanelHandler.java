package com.ushahidi.plugins.mapping.ui;

import thinlet.ThinletText;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
import com.ushahidi.plugins.mapping.data.repository.CategoryDao;
import com.ushahidi.plugins.mapping.data.repository.IncidentDao;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.util.MappingLogger;
import com.ushahidi.plugins.mapping.util.MappingMessages;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.Icon;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

@SuppressWarnings("serial")
public class ReportsPanelHandler extends ExtendedThinlet implements ThinletUiEventHandler {

	private static MappingLogger LOG = new MappingLogger(ReportsPanelHandler.class);
	
	private static final String UI_PANEL_XML = "/ui/plugins/mapping/reportsPanel.xml";
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final IncidentDao incidentDao;
	private final CategoryDao categoryDao;
	private final MappingSetupDao mappingSetupDao;
	
	private final Object mainPanel;
	private final UIFields fields;
	private class UIFields extends Fields {
		public UIFields(UiGeneratorController uiController, Object parent) {
			super(uiController, parent);
		}
		public Object tblReports;
		public Object cbxCategories;
		public Object txtSearch;
	}
	
	public ReportsPanelHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.incidentDao = pluginController.getIncidentDao();
		this.categoryDao = pluginController.getCategoryDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		this.mainPanel = this.ui.loadComponentFromFile(UI_PANEL_XML, this);
		this.fields = new UIFields(ui, mainPanel);
	}
	
	public Object getMainPanel() {
		return this.mainPanel;
	}
	
	public void init() {
		ui.removeAll(fields.tblReports);
		ui.removeAll(fields.cbxCategories);
		if (mappingSetupDao.getDefaultSetup() != null) {
			if (incidentDao.getCount() > 0) {
				for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
					LOG.debug("Loading incident %s", incident.getTitle());
					ui.add(fields.tblReports, getRow(incident));
				}
			}
			ui.add(fields.cbxCategories, createComboboxChoice(MappingMessages.getAllCategories(), null));
			for(Category category : categoryDao.getAllCategories(mappingSetupDao.getDefaultSetup())){
				LOG.debug("Loading category %s", category.getTitle());
				ui.add(fields.cbxCategories, createComboboxChoice(category.getTitle(), category));
			}
			ui.setSelectedIndex(fields.cbxCategories, 0);	
			ui.setText(fields.txtSearch, MappingMessages.getSearchIncidents());
			ui.setEnabled(fields.cbxCategories, true);
			ui.setEnabled(fields.txtSearch, true);
		}
		else {
			ui.setText(fields.txtSearch, "");
			ui.setEnabled(fields.txtSearch, false);
			ui.setEnabled(fields.cbxCategories, false);
		}
	}
	
	public void refresh() {
		ui.removeAll(fields.tblReports);
		for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
			LOG.debug("Loading incident %s", incident.getTitle());
			ui.add(fields.tblReports, getRow(incident));
		}
	}
	
	public void search(Object textField, Object comboBox) {
		Object selectedItem =  getSelectedItem(comboBox);
		Category category = selectedItem != null ? getAttachedObject(selectedItem, Category.class) : null;
		String searchText = ui.getText(textField).trim().toLowerCase();
		LOG.debug("searchText=%s", searchText);
		ui.removeAll(fields.tblReports);
		for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
			if (category == null || incident.hasCategory(category)) {
				if (searchText.length() == 0 || 
					searchText.equalsIgnoreCase(MappingMessages.getSearchIncidents()) || 
					incident.getTitle().toLowerCase().indexOf(searchText) > -1) {
					ui.add(fields.tblReports, getRow(incident));
				}
			}
		}
	}
	
	public void focusGained(Object textfield) {
		String searchText = ui.getText(textfield);
		if (searchText.equalsIgnoreCase(MappingMessages.getSearchIncidents())) {
			ui.setText(textfield, "");
		}
	}
	
	public void focusLost(Object textfield) {
		String searchText = ui.getText(textfield);
		if (searchText == null || searchText.length() == 0) {
			ui.setText(textfield, MappingMessages.getSearchIncidents());
		}
	}
	
	/**
	 * Show the details of the selected report item
	 * @param item
	 */
	public void showReportDetails(Object table, Object item){
		Object selectedItem = ui.getSelectedItem(table);
		Incident incident = ui.getAttachedObject(selectedItem, Incident.class);
		ReportDialogHandler reportDialog = new ReportDialogHandler(pluginController, frontlineController, ui);
		reportDialog.showDialog(incident);
	}
	
	/**
	 * Returns a row with an incident attached to it
	 * 
	 * @param incident see {@link #Incident}
	 * 
	 * @return
	 */
	private Object getRow(Incident incident){
		Object row = createTableRow(incident);
		if (incident.isMarked()) {
			if (incident.hasSyncStatus()) {
				createIconTableCell(row, Icon.CANCEL);
			}
			else {
				createIconTableCell(row, Icon.TICK);
			}
		}
		else {
			ui.add(row, ui.createTableCell(""));
		}
		if (incident.isVerified()) {
			createIconTableCell(row, Icon.TICK);
		}
		else {
			ui.add(row, ui.createTableCell(""));
		}
		createTableCell(row, incident.getTitle());
		if (incident.getLocation() != null) {
			createTableCell(row, incident.getLocation().getName());
		}
		else {
			createTableCell(row, "");
		}
		if (incident.getIncidentDate() != null) {
			createTableCell(row, InternationalisationUtils.getDateFormat().format(incident.getIncidentDate()));
		}
		else {
			createTableCell(row, "");
		}
		createTableCell(row, incident.getCategoryNames());
		createTableCell(row, incident.getDescription());
		return row;
	}
	
	private Object createIconTableCell(Object row, String icon) {
		Object cell = this.createTableCell("");
		ui.setIcon(cell, icon);
		ui.setChoice(cell, ThinletText.ALIGNMENT, ThinletText.CENTER);
		ui.add(row, cell);
		return cell;
	}
	
}