/**
 * 
 */
package net.frontlinesms.plugins.forms.request;

import java.util.List;

import net.frontlinesms.plugins.forms.data.domain.ResponseValue;

/**
 * Submitted data for a single form.
 * @author Alex
 */
public class SubmittedFormData {
	/** ID of the form that this data was submitted for. */
	private final int formMobileId;
	/** ID of this data set */
	private final int dataId;
	/** List of data values that were submitted. */
	private final List<ResponseValue> dataValues;
	
	/**
	 * Create a new instance of this class.
	 * @param formMobileId
	 * @param dataId 
	 * @param dataValues
	 */
	public SubmittedFormData(int formMobileId, int dataId, List<ResponseValue> dataValues) {
		this.formMobileId = formMobileId;
		this.dataId = dataId;
		this.dataValues = dataValues;
	}

	/** @return {@link #formMobileId} */
	public int getFormMobileId() {
		return formMobileId;
	}

	/** @return {@link #dataValues} */
	public List<ResponseValue> getDataValues() {
		return dataValues;
	}

	/** @return {@link #dataId} */
	public int getDataId() {
		return this.dataId;
	}
}
