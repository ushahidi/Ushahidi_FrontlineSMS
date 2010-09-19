package com.ushahidi.plugins.mapping.ui;

import thinlet.Thinlet;

import com.ushahidi.plugins.mapping.MappingPluginController;
import com.ushahidi.plugins.mapping.util.MappingLogger;

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
		ui.setText(lblCurrentTask, "1");
		ui.setText(lblTotalTasks, "1");
		ui.add(mainDialog);
	}
	
	public void hideDialog() {
		ui.remove(mainDialog);
	}
	
	/**
	 * Updates the current value of the synchronization progress bar
	 * 
	 */
	public synchronized void setProgress(int tasks, int completed){
		int maximum = getInteger(pbarSynchronization, Thinlet.MAXIMUM);
		float progress = (completed <= tasks) ? ((float)completed / (float)tasks) * (float)maximum : (float)maximum;
		ui.setInteger(pbarSynchronization, Thinlet.VALUE, Math.round(progress));
		ui.setText(lblCurrentTask, Integer.toString(completed));
		ui.setText(lblTotalTasks, Integer.toString(tasks));
	}
	
	public void removeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	public void removeDialog() {
		ui.remove(mainDialog);
	}
}