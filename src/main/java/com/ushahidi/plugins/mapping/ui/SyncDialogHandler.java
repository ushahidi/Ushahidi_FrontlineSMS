package com.ushahidi.plugins.mapping.ui;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.utils.MappingLogger;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.ui.ExtendedThinlet;
import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

@SuppressWarnings("serial")
public class SyncDialogHandler extends ExtendedThinlet implements ThinletUiEventHandler {

	public static MappingLogger LOG = MappingLogger.getLogger(SyncDialogHandler.class);
	
	private static final String UI_SYNCHRONIZATION_DIALOG = "/ui/plugins/mapping/syncDialog.xml";
	
	@SuppressWarnings("unused")
	private final MappingPluginController pluginController;
	@SuppressWarnings("unused")
	private final FrontlineSMS frontlineController;
	private final UiGeneratorController ui;
	
	private final Object mainDialog;
	private final Object pbarSynchronization;
	private final Object lblCurrentTask;
	private final Object lblTotalTasks;
	
	public SyncDialogHandler(MappingPluginController pluginController, FrontlineSMS frontlineController, UiGeneratorController uiController) {
		this.pluginController = pluginController;
		this.ui = uiController;
		this.frontlineController = frontlineController;
		this.mainDialog = this.ui.loadComponentFromFile(UI_SYNCHRONIZATION_DIALOG, this);
		this.pbarSynchronization = this.ui.find(this.mainDialog, "pbarSynchronization");
		this.lblCurrentTask = this.ui.find(this.mainDialog, "lblCurrentTask");
		this.lblTotalTasks = this.ui.find(this.mainDialog, "lblTotalTasks");
	}
	
	public void showDialog() {
		setText(lblCurrentTask, "1");
		setText(lblTotalTasks, "1");
		this.ui.add(this.mainDialog);
	}
	
	public void hideDialog() {
		this.ui.remove(this.mainDialog);
	}
	
	/**
	 * Updates the current value of the synchronization progress bar
	 * 
	 * @param dialog
	 * @param taskNo
	 */
	public synchronized void setProgress(int tasks, int completed){
		setText(lblCurrentTask, Integer.toString(completed));
		setText(lblTotalTasks, Integer.toString(tasks));
		
		int currentValue = getInteger(pbarSynchronization, Thinlet.VALUE);
		int maxValue = getInteger(pbarSynchronization, Thinlet.MAXIMUM);
		currentValue += (completed <= tasks) ? (maxValue - currentValue) / tasks : (maxValue - currentValue);
		setInteger(pbarSynchronization, Thinlet.VALUE, currentValue);
		
		ui.repaint();
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	public void removeDialog() {
		ui.remove(this.mainDialog);
	}
}