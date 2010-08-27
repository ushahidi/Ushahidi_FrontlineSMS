package com.ushahidi.plugins.mapping.ui;

import org.apache.log4j.Logger;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.FrontlineUtils;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class SyncDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler {

	private static final String UI_SYNCHRONIZATION_DIALOG = "/ui/plugins/mapping/syncDialog.xml";
	
	public static Logger LOG = FrontlineUtils.getLogger(SyncDialogHandler.class);	
	
	private final MappingPluginController pluginController;
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private Object mainDialog;
	
	private static final String COMPONENT_LBL_SYNC_CURRENT_TASK_NO  = "lbl_currentTaskNo";
	private static final String COMPONENT_LBL_SYNC_TOTAL_TASK_COUNT = "lbl_totalTaskCount";
	private static final String COMPONENT_SYNC_PROGRESS_BAR = "pbar_Synchronization";
	
	public SyncDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		this.mainDialog = this.ui.loadComponentFromFile(UI_SYNCHRONIZATION_DIALOG, this);
	}
	
	public void showDialog() {
		setText(ui.find(this.mainDialog, COMPONENT_LBL_SYNC_CURRENT_TASK_NO), "1");
		this.ui.add(this.mainDialog);
	}
	
	/**
	 * Updates the label in synchronization dialog with the total number of tasks to be performed
	 * 
	 * @param dialog
	 * @param count
	 */
	public synchronized void setSynchronizationTaskCount(int count){
		setText(ui.find(this.mainDialog, COMPONENT_LBL_SYNC_TOTAL_TASK_COUNT), Integer.toString(count));
		int currentVal = 100 - (count * (int)100/count);
		setInteger(ui.find(this.mainDialog, COMPONENT_SYNC_PROGRESS_BAR), Thinlet.VALUE, currentVal);
		ui.repaint();
	}
	
	/**
	 * Updates the current value of the synchronization progress bar
	 * 
	 * @param dialog
	 * @param taskNo
	 */
	public synchronized void updateProgressBar(int taskNo){
		Object progressBar = ui.find(this.mainDialog, COMPONENT_SYNC_PROGRESS_BAR);
		int taskCount = Integer.parseInt(getText(ui.find(this.mainDialog, COMPONENT_LBL_SYNC_TOTAL_TASK_COUNT)));
		int currentVal = getInteger(progressBar, Thinlet.VALUE);
		int maxValue = getInteger(progressBar, Thinlet.MAXIMUM);
		
		if(taskNo <= taskCount)
			setText(ui.find(this.mainDialog, COMPONENT_LBL_SYNC_CURRENT_TASK_NO), Integer.toString(taskNo));		
		
		//Calculate the unit increment and the current value
		currentVal += (taskNo <= taskCount)? (maxValue - currentVal)/taskCount : (maxValue - currentVal);
		
		//Update the progress bar with the current value
		setInteger(progressBar, Thinlet.VALUE, currentVal);
		
		ui.repaint();
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	public void removeDialog() {
		ui.remove(this.mainDialog);
	}
}