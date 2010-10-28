package com.ushahidi.plugins.mapping.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * ImageChooser
 * @author dalezak
 *
 */
public class ImageChooser {
	
	private JFileChooser fileChooser;
	private Component parent;
	private String toolTipText;
	private String buttonText;
	
	public ImageChooser(Component parent) {
		this(parent, null, null);
	}
	
	public ImageChooser(Component parent, String selectText) {
		this(parent, selectText, null);
	}
	
	public ImageChooser(Component parent, String selectText, String toolTipText){
		this.parent = parent;
		this.buttonText = selectText;
		this.toolTipText = toolTipText;
	}
	
	public void setButtonText(String selectText) {
		this.buttonText = selectText;
	}
	
	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}
	
	/**
	 * Show Image Dialog
	 * @return file path
	 */
	public String showDialog() {
		 if (fileChooser == null) {
	         fileChooser = new JFileChooser();
		 }
		 ImageFilter imageFilter = new ImageFilter();
		 fileChooser.setToolTipText(toolTipText);
		 fileChooser.addChoosableFileFilter(imageFilter);
         fileChooser.setAcceptAllFileFilterUsed(false);
         fileChooser.setFileFilter(imageFilter);
         fileChooser.setMultiSelectionEnabled(false);
         if(fileChooser.showDialog(parent, buttonText) == JFileChooser.APPROVE_OPTION) {
        	 File file = fileChooser.getSelectedFile();
        	 if (file.exists()) {
        		 return file.getAbsolutePath();
        	 }
         }
         return null;
	}
}