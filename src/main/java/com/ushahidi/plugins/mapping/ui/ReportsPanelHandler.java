package com.ushahidi.plugins.mapping.ui;

import org.apache.log4j.Logger;

import thinlet.ThinletText;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.Category;
import com.ushahidi.plugins.mapping.data.domain.Incident;
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
	
	private Object mainPanel;
	private final IncidentDao incidentDao;
	private final MappingSetupDao mappingSetupDao;
	
	private static final String COMPONENT_TBL_INCIDENT_REPORTS = "tbl_IncidentReports";
	
	public ReportsPanelHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		this.incidentDao = pluginController.getIncidentDao();
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		this.mainPanel = this.ui.loadComponentFromFile(UI_PANEL_XML, this);
	}
	
	public Object getMainPanel() {
		return this.mainPanel;
	}
	
	public void showPanel(Object container) {
		this.ui.removeAll(container);
		if (this.incidentDao.getCount() > 0) {
			Object table = this.ui.find(this.mainPanel, COMPONENT_TBL_INCIDENT_REPORTS);
			this.removeAll(table);
			for(Incident incident: incidentDao.getAllIncidents(mappingSetupDao.getDefaultSetup())){
				add(table, getRow(incident));
			}
		}
		this.ui.add(container, this.mainPanel);
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
			Object cell = this.ui.createTableCell("");
			this.ui.setIcon(cell, Icon.TICK);
			this.ui.setChoice(cell, ThinletText.ALIGNMENT, ThinletText.CENTER);
			this.ui.add(row, cell);
		}
		else {
			this.ui.add(row, this.ui.createTableCell(""));
		}
	}
}