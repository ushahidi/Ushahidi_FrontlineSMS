/**
 * 
 */
package net.frontlinesms.plugins.forms.data;

/**
 * @author Alex
 */
@SuppressWarnings("serial")
public class FormHandlingException extends Exception {

	/** @see Exception#Exception() */
	public FormHandlingException() {
		super();
	}
	
	/** @see Exception#Exception(String) */
	public FormHandlingException(String message) {
		super(message);
	}

	/** @see Exception#Exception(Throwable) */
	public FormHandlingException(Throwable cause) {
		super(cause);
	}

	/** @see Exception#Exception(String, Throwable) */
	public FormHandlingException(String message, Throwable cause) {
		super(message, cause);
	}

}
