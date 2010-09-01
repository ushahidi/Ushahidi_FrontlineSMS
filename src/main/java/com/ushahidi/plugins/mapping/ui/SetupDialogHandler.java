package com.ushahidi.plugins.mapping.ui;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.utils.MappingLogger;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class SetupDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler {

	public static MappingLogger LOG = MappingLogger.getLogger(SetupDialogHandler.class);
	
	private static final String UI_SETUP_DIALOG = "/ui/plugins/mapping/setupDialog.xml";
	
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final MappingSetupDao mappingSetupDao;
	
	private Object mainDialog;
	
	private static final String SETUP_DLG_COMPONENT_SOURCE_TABLE = "locationSources_Table";
	private static final String SETUP_DLG_COMPONENT_FLD_SOURCE_NAME = "txtSourceName";
	private static final String SETUP_DLG_COMPONENT_FLD_SOURCE	= "txtLocationSource";
	private static final String SETUP_DLG_COMPONENT_FLD_LONGITUDE = "txtLongitude";
	private static final String SETUP_DLG_COMPONENT_FLD_LATITUDE = "txtLatitude";
	private static final String SETUP_DLG_COMPOONENT_CHK_STATUS = "chkSourceStatus";

	public SetupDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.ui = uiController;
		this.frontlineController = frontlineController;
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		this.mainDialog = this.ui.loadComponentFromFile(UI_SETUP_DIALOG, this);
	}
	
	public void showDialog() {
		if (mappingSetupDao.getCount() > 0){
			Object table = ui.find(this.mainDialog, SETUP_DLG_COMPONENT_SOURCE_TABLE);
			this.removeAll(table);
			for(MappingSetup setup: mappingSetupDao.getAllSetupItems())
				add(table, getRow(setup));
		}
		ui.add(this.mainDialog);	
	}
	
	/**
	 * Gets a table row with {@link MappingSetup} attached
	 * @param setup
	 * @return
	 */
	private Object getRow(MappingSetup setup){
		Object row = createTableRow(setup);
		createTableCell(row, setup.getName());
		createTableCell(row, setup.getSourceURL());
		createTableCell(row, Double.toString(setup.getLatitude()));
		createTableCell(row, Double.toString(setup.getLongitude()));
		String activeStr = (setup.isDefaultSetup())?"Y":"N";
		createTableCell(row, activeStr);
		
		return row;
	}
	
	/**
	 * Loads the details of the mapping setup in the fields for editing
	 * 
	 * @param setupDialog Setup dialog for the map
	 * @param tblLocationSource Table containing the list of the mapping sources
	 */
	public void edit_MappingSource(Object setupDialog, Object tblLocationSource){
		Object item = getAttachedObject(getSelectedItem(tblLocationSource));
		if(item instanceof MappingSetup){
			MappingSetup setup = (MappingSetup)item;
			
			//Attach the setup item to the dialog
			setAttachedObject(setupDialog, setup);
			
			setText(ui.find(setupDialog, SETUP_DLG_COMPONENT_FLD_SOURCE_NAME), setup.getName());
			setText(ui.find(setupDialog, SETUP_DLG_COMPONENT_FLD_SOURCE), setup.getSourceURL());
			setText(ui.find(setupDialog, SETUP_DLG_COMPONENT_FLD_LATITUDE), Double.toString(setup.getLatitude()));
			setText(ui.find(setupDialog, SETUP_DLG_COMPONENT_FLD_LONGITUDE), Double.toString(setup.getLongitude()));
			setSelected(ui.find(setupDialog, SETUP_DLG_COMPOONENT_CHK_STATUS), setup.isDefaultSetup());
			
			ui.repaint();
		}
	}
	
	/**
	 * Adds a new set of mapping configurations to the database
	 * 
	 * @param dialog
	 */
	public void addMappingSource(Object dialog){
		String sourceName = getText(find(dialog, SETUP_DLG_COMPONENT_FLD_SOURCE_NAME));
		String sourceURL = getText(find(dialog, SETUP_DLG_COMPONENT_FLD_SOURCE));
		String lat = getText(find(dialog, SETUP_DLG_COMPONENT_FLD_LATITUDE));
		String lng = getText(find(dialog, SETUP_DLG_COMPONENT_FLD_LONGITUDE));
		
		boolean active = getBoolean(find(dialog, SETUP_DLG_COMPOONENT_CHK_STATUS), Thinlet.SELECTED);
		
		/** Validation checks */
		//source url check
		if(sourceURL == null || sourceURL.length() == 0){
			ui.alert("Please specifiy a valid source url");
			LOG.debug("Invalid or empty source url");
			return;
		}
		
		//latitude check
		if(lat == null || lat.length() ==0){
			ui.alert("Please specifiy a valid latitude");
			LOG.debug("Invalid or empty latitude value");
			return;
		}
		
		//longitude check
		if(lng == null || lng.length() == 0){
			ui.alert("Please specify a valid longitude");
			LOG.debug("Invalid or empty longitude value");
			return;
		}
		
		
		MappingSetup setup = null;
		Object item = getAttachedObject(dialog);
		
		if(item instanceof MappingSetup){
			setup = (MappingSetup)item;
			setAttachedObject(dialog,  null);
		}else{
			setup = new MappingSetup();
		}
		
		//Get the current default setup
		MappingSetup currentDefault = (mappingSetupDao.getDefaultSetup() != null)? mappingSetupDao.getDefaultSetup():null;
		
		//Set the properties for the mapping setup
		setup.setName(sourceName);
		setup.setSourceURL(sourceURL);
		setup.setLatitude(Double.parseDouble(lat));
		setup.setLongitude(Double.parseDouble(lng));		
		
		//Check if the current item is the only one and if its being unset as the default
		if(currentDefault != null && mappingSetupDao.getCount() == 1 && !active){
			LOG.debug("There must be a default configurartion for Mapping");
			ui.alert("There is only one configuration for Mapping ["+setup.getSourceURL()+"] " +
					"and it must be set as the default");
			
			return;
		}
		
		//Check for attempts to save without specifying a default mapping configuration
		if( (currentDefault != null && mappingSetupDao.getCount() > 1 && 
				setup.getId() == currentDefault.getId() && !active) || (currentDefault == null && !active)){
			LOG.debug("Default mapping setup not specified");
			ui.alert("There must be a default configuration for Mapping to work");
			
			return;
		}
		
		//Set the active flag for the mapping setup 
		setup.setDefaultSetup(active);
		
		try{
			Object table = ui.find(dialog, SETUP_DLG_COMPONENT_SOURCE_TABLE);

			//Update the current default setup if it is different from the new one
			if(currentDefault != null && Long.toString(setup.getId()) != null)
				if (setup.getId() != currentDefault.getId() && setup.isDefaultSetup()){
					currentDefault.setDefaultSetup(false);
					mappingSetupDao.updateMappingSetup(currentDefault);
					LOG.debug("Changed default mapping setup to " + setup.getName());					
				}
			
			if(item == null){
				mappingSetupDao.saveMappingSetup(setup);				
				add(table, getRow(setup));
			}else{
				mappingSetupDao.updateMappingSetup(setup);
			}
			
			removeAll(table);
			for(MappingSetup s: mappingSetupDao.getAllSetupItems())
				add(table, getRow(s));
			
		}catch(DuplicateKeyException e){
			LOG.debug("Mapping setup parameter already exists", e);
			ui.alert("Mapping setup parameter already exists");
			LOG.trace("EXIT");
			return;
		}
		
		LOG.debug("Mapping setup parameter for [" + setup.getSourceURL() +"] created!");
				
		ui.repaint();
		clearSourceFields(dialog);
		
		// If the default map setup has changed, re-initialize the keywords, incidents and map bean 
		// so as to reflect the new mapping settings
		if(currentDefault != null && currentDefault.getId() != setup.getId()){
			// Update the list of keywords
			//updateKeywordList();
			
			// Update the map container with the new map
			//initializeMapBean();
		}

	}
	
	/**
	 * Clears the input fields from the mapping setup dialog 
	 * @param dialog
	 */
	public void clearSourceFields(Object dialog){
		setText(find(dialog, SETUP_DLG_COMPONENT_FLD_SOURCE_NAME), "");
		setText(find(dialog, SETUP_DLG_COMPONENT_FLD_SOURCE),"");
		setText(find(dialog, SETUP_DLG_COMPONENT_FLD_LATITUDE),"");
		setText(find(dialog, SETUP_DLG_COMPONENT_FLD_LONGITUDE),"");
		setBoolean(find(dialog, SETUP_DLG_COMPOONENT_CHK_STATUS), Thinlet.SELECTED, false);
		ui.repaint();
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
}