package com.ushahidi.plugins.mapping.ui;

import java.io.File;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.data.domain.MappingSetup;
import com.ushahidi.plugins.mapping.data.repository.MappingSetupDao;
import com.ushahidi.plugins.mapping.maps.TileSaver;
import com.ushahidi.plugins.mapping.maps.TiledMap;
import com.ushahidi.plugins.mapping.util.MappingLogger;

@SuppressWarnings("serial")
public class MapSaveDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler {

	private static final String UI_DIALOG_XML = "/ui/plugins/mapping/mapSaveDialog.xml";
	
	public static MappingLogger LOG = MappingLogger.getLogger(MapSaveDialogHandler.class);
	
	@SuppressWarnings("unused")
	private final MappingPluginController pluginController;
	@SuppressWarnings("unused")
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final Object mainDialog;
	
	private final MappingSetupDao mappingSetupDao;
	private MapBean mapBean;
	
	public MapSaveDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		
		this.mappingSetupDao = pluginController.getMappingSetupDao();
		
		this.mainDialog = this.ui.loadComponentFromFile(UI_DIALOG_XML, this);
	}
	
	public void showDialog(MapBean mapBean) {
		this.mapBean = mapBean;
		this.ui.add(this.mainDialog);
	}
	
	/**
	 * Saves the map tiles to a file on the disk
	 * 
	 * @param dialog Map Save dialog
	 * @param mapName Name of the file to save the map to
	 */
	public void doMapSave(Object dialog, String mapName) {
		TiledMap map = this.mapBean.getMap();
		//Create maps dir in config dir if it doesn't exist
		File file = new File(ResourceUtils.getConfigDirectoryPath() + "/maps");
		if(!file.exists()) {
			if(!file.mkdir()) {
				ui.alert("Unable to create maps dir!");
				return;
			}
		}
		String filename = ResourceUtils.getConfigDirectoryPath() + "/maps/" + mapName + ".zip";
		TileSaver tileSaver = new TileSaver(map, map.topLeftCoord(), map.btmRightCoord(), filename, true);
		tileSaver.startSave();
		tileSaver.done();
		
		MappingSetup mappingSetup = mappingSetupDao.getDefaultSetup();
		mappingSetup.setOfflineMapFile(filename);
		mappingSetup.setOffline(true);
		try{
			mappingSetupDao.updateMappingSetup(mappingSetup);
		}
		catch(DuplicateKeyException de){
			de.printStackTrace();
			LOG.debug("Could not update map setup", de);
			ui.alert("Update of map setup failed");
		}
		ui.remove(dialog);
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
}