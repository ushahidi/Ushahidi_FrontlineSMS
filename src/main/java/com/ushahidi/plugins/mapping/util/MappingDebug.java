package com.ushahidi.plugins.mapping.util;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.ushahidi.plugins.mapping.managers.FormsManager;
import com.ushahidi.plugins.mapping.managers.SurveysManager;

/**
 * MappingDebug
 * @author dalezak
 *
 */
public class MappingDebug {
	
	private static MappingLogger LOG = MappingLogger.getLogger(MappingDebug.class);	
	
	private final FormsManager formsManager;
	private final SurveysManager surveysManager;
	
	/**
	 * MappingDebug
	 * @param pluginController MappingPluginController
	 */
	public MappingDebug(FormsManager formsManager, SurveysManager surveysManager) {
		this.formsManager = formsManager;
		this.surveysManager = surveysManager;
	}
	
	/**
	 * Start debug terminal
	 */
	public void startDebugTerminal() {
		Thread thread = new DebugTerminal();
		thread.start();
    }
	
	/**
	 * Inner threaded class for listening to System.in
	 * @author dalezak
	 *
	 */
	private class DebugTerminal extends Thread {
		public void run() {
			List<String> exitKeywords = Arrays.asList("exit", "x", "quit", "q");
			LOG.error("Debug Terminal Started...");
	        Scanner scanner = new Scanner(System.in);
	        while(true) { 
	            String message = scanner.nextLine().trim();
	            String[] words = message.split(" ", 2);
	            if (exitKeywords.contains(message.toLowerCase())) {
	            	break;
	            }
	            else if (message.toLowerCase().startsWith("form")){
	            	String title = words.length > 1 ? words[1] : null;
	            	formsManager.addFormResponse(title);
	            }
	            else if (message.toLowerCase().startsWith("survey")){
	            	String title = words.length > 1 ? words[1] : null;
	            	surveysManager.addSurveyAnswers(title);
	            }
	            else if (message.equalsIgnoreCase("help")){
	            	LOG.error("Enter 'form' to create a sample Form, 'survey' to create a sample Survey or 'exit' to terminate console.");
	            }
	        }
		 }
	}
	
}