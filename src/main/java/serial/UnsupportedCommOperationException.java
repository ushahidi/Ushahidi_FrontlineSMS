/**
 * 
 */
package serial;

/**
 * Wrapper for {@link gnu.io.UnsupportedCommOperationException} and {@link javax.comm.UnsupportedCommOperationException}.
 * @author Alex
 */
@SuppressWarnings("serial")
public class UnsupportedCommOperationException extends SerialException {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/** @see SerialException#SerialException(Exception) */
	public UnsupportedCommOperationException(Exception cause) {
		super(cause);
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
