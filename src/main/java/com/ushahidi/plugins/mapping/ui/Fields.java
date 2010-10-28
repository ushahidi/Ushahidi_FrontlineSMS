package com.ushahidi.plugins.mapping.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.ushahidi.plugins.mapping.util.MappingLogger;

import net.frontlinesms.ui.UiGeneratorController;

/**
 * Abstract class for storing and loading UI fields
 * @author dalezak
 *
 */
public abstract class Fields {
	
	private final static MappingLogger LOG = MappingLogger.getLogger(Fields.class);
	
	/**
	 * Load all declared fields
	 * @param ui UiGeneratorController
	 * @param parent Panel or Dialog
	 */
	public Fields(UiGeneratorController ui, Object parent) {
		for (Field field : this.getClass().getDeclaredFields()) {
			try {
				if (Modifier.isFinal(field.getModifiers()) == false) {
					LOG.debug("Loading Field: %s", field.getName());
					field.set(this, ui.find(parent, field.getName()));		
				}
			} 
			catch (IllegalArgumentException e) {
				LOG.error(e);
			} 
			catch (IllegalAccessException e) {
				LOG.error(e);
			}
		}
	}
}