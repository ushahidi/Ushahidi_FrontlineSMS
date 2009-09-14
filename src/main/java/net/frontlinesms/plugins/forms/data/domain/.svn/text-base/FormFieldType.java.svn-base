/**
 * 
 */
package net.frontlinesms.plugins.forms.data.domain;

/**
 * The different types of form field that are available.
 * @author Alex
 */
public enum FormFieldType {
	CHECK_BOX(true),
	CURRENCY_FIELD(true),
	DATE_FIELD(true),
	EMAIL_FIELD(true),
	NUMERIC_TEXT_FIELD(true),
	PASSWORD_FIELD(true),
	PHONE_NUMBER_FIELD(true),
	TEXT_AREA(true),
	TEXT_FIELD(true),
	TIME_FIELD(true),
	TRUNCATED_TEXT(false),
	WRAPPED_TEXT(false);
	
	/** Indicates whether fields of this type can have a value set. */
	private final boolean hasValue;
	
	/**
	 * Creates a new {@link FormFieldType}.
	 * @param hasValue value for {@link #hasValue}
	 */
	private FormFieldType(boolean hasValue) {
		this.hasValue = hasValue;
	}
	
	/** @return {@link #hasValue} */
	public boolean hasValue() {
		return this.hasValue;
	}
}
