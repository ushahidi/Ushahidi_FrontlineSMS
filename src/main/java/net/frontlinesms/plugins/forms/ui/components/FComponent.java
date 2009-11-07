/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.plugins.forms.ui.components;

import java.awt.Container;
import java.io.Serializable;
import java.util.HashMap;

import net.frontlinesms.plugins.forms.data.domain.FormFieldType;

@SuppressWarnings("serial")
public abstract class FComponent implements Serializable, Cloneable {
	
//> CONSTANTS
	/** Property name for property: Label */
	public static final String PROPERTY_LABEL = "Label";
	/** Property name for property: Type */
	public static final String PROPERTY_TYPE = "Type";
	
	/** Value to display for {@link #label} when it is <code>null</code> */
	public static final String PROPERTY_DISPLAY_VALUE_NO_LABEL = "(no label)";
	
	private int renderHeight = 30;
	protected static final int renderWidth = 205;

	private String label;
	
//> ABSTRACT ACCESSORS
	/** @return the path of the icon to be displayed with this component. */
	public abstract String getIcon();
	public abstract String getDescription();
	public abstract Container getDrawingComponent();
	
//> ACCESSORS
	/** @param renderHeight new value for {@link #renderHeight} */
	public void setRenderHeight(int renderHeight) {
		this.renderHeight = renderHeight;
	}
	
	/** @return {@link #renderHeight} */
	public int getHeight() {
		return renderHeight;
	}
	
	/** @return {@link #label} */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Get the label, or a placeholder if the label is null.  This method should be used for rendering
	 * components so that there is always something displayed for empty labels.
	 * @return {@link #label} value, or a placeholder if it is <code>null</code>
	 */
	public String getDisplayLabel() {
		if(this.label != null && this.label.length() > 0) {
			return this.label;
		} else {
			return PROPERTY_DISPLAY_VALUE_NO_LABEL;
		}
	}
	
	/** @param label new value for {@link #label} */
	public void setLabel(String label) {
		this.label = label;
	}

//> INSTANCE METHODS
	/** @see Object#clone() */
	public FComponent clone() {
		try {
			FComponent clone = (FComponent) super.clone();
			clone.setLabel(this.getLabel());
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
//> STATIC HELPER METHODS
	public static FormFieldType getFieldType(Class<? extends FComponent> componentClass) {
		if(componentClass == CheckBox.class) return FormFieldType.CHECK_BOX;
		if(componentClass == CurrencyField.class) return FormFieldType.CURRENCY_FIELD;
		if(componentClass == DateField.class) return FormFieldType.DATE_FIELD;
		if(componentClass == EmailField.class) return FormFieldType.EMAIL_FIELD;
		if(componentClass == NumericTextField.class) return FormFieldType.NUMERIC_TEXT_FIELD;
		if(componentClass == PasswordField.class) return FormFieldType.PASSWORD_FIELD;
		if(componentClass == PhoneNumberField.class) return FormFieldType.PHONE_NUMBER_FIELD;
		if(componentClass == TextArea.class) return FormFieldType.TEXT_AREA;
		if(componentClass == TextField.class) return FormFieldType.TEXT_FIELD;
		if(componentClass == TimeField.class) return FormFieldType.TIME_FIELD;
		if(componentClass == TruncatedText.class) return FormFieldType.TRUNCATED_TEXT;
		if(componentClass == WrappedText.class) return FormFieldType.WRAPPED_TEXT;
		throw new IllegalStateException("Unknown component type: " + componentClass);
	}
	
	public static Class<? extends FComponent> getComponentClass(FormFieldType fieldType) {
		if(fieldType == FormFieldType.CHECK_BOX) 			return CheckBox.class;
		if(fieldType == FormFieldType.CURRENCY_FIELD) 		return CurrencyField.class;
		if(fieldType == FormFieldType.DATE_FIELD) 			return DateField.class;
		if(fieldType == FormFieldType.EMAIL_FIELD) 			return EmailField.class;
		if(fieldType == FormFieldType.NUMERIC_TEXT_FIELD) 	return NumericTextField.class;
		if(fieldType == FormFieldType.PASSWORD_FIELD) 		return PasswordField.class;
		if(fieldType == FormFieldType.PHONE_NUMBER_FIELD) 	return PhoneNumberField.class;
		if(fieldType == FormFieldType.TEXT_AREA) 			return TextArea.class;
		if(fieldType == FormFieldType.TEXT_FIELD) 			return TextField.class;
		if(fieldType == FormFieldType.TIME_FIELD) 			return TimeField.class;
		if(fieldType == FormFieldType.TRUNCATED_TEXT) 		return TruncatedText.class;
		if(fieldType == FormFieldType.WRAPPED_TEXT) 		return WrappedText.class;
		throw new IllegalStateException("No handling for form field type: " + fieldType);
	}
}
