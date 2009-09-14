package com.ushahidi.plugins.mapping;

import com.ushahidi.plugins.mapping.ui.MappingUIController;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.plugins.PluginController;
import net.frontlinesms.ui.UiGeneratorController;

public class MappingPluginController implements PluginController {
	
	private static final String XML_MAPPING_TAB = "/ui/plugins/mapping/mappingTab.xml";
	private FrontlineSMS frontlineController;
	private MappingUIController mappingUIController;
	public String getName() {		
		return "Ushahidi";
	}
	
	public void init(FrontlineSMS frontlineController) {
		this.frontlineController = frontlineController;
	}
	

	public Object getTab(UiGeneratorController uiController) {
		mappingUIController = new MappingUIController(frontlineController, uiController);
		return uiController.loadComponentFromFile(XML_MAPPING_TAB, mappingUIController);
	}

	public void initializePluginData() {
		mappingUIController.init();
	}

}
