package com.ushahidi.plugins.mapping.sync;

import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author Emmanuel Kala <emkala@googlemail.com>
 *
 */
public class SynchronizationTask {
	/** Type of synchronization to be performed*/
	private String taskType;
	
	/** Name of the synchronization job */
	private String taskName;
	
	/** Parameters to be appended to the task name */
	private String requestParameter = null;
	
	/** List of values for the URL parameters */
	private List<String> taskValues;
	
	public SynchronizationTask(String type, String name){
		this.taskType = type;
		this.taskName = name;
	}
	
	/**
	 * Sets the requestParameter
	 * @param param
	 */
	public void setRequestParameter(String param){
		this.requestParameter = param;
	}
	
	public void setTaksValues(List<String> values){
		this.taskValues = values;
	}
	
	/**
	 * Gets the type of the synchronization task
	 * @return
	 */
	public String getTaskType(){
		return this.taskType;
	}
	
	/**
	 * Gets the name of the synchronization task
	 * @return
	 */
	public String getTaskName(){
		return this.taskName;
	}
	
	/**
	 * Gets the task values
	 * @return
	 */
	public List<String> getTaskValues(){
		return (taskValues == null)? new ArrayList<String>() : taskValues;
	}
	
	/** Gets the request parameter */
	public String getRequestParameter(){
		return this.requestParameter;
	}
}
