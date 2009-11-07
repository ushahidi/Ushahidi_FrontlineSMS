/**
 * 
 */
package serial;

/**
 * Wrapper for {@link gnu.io.NoSuchPortException} and {@link javax.comm.NoSuchPortException}.
 * @author Alex
 */
@SuppressWarnings({ "serial", "restriction" })
public class NoSuchPortException extends SerialException {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/** @see SerialException#SerialException(Exception) */
	public NoSuchPortException(Exception cause) {
		super(cause);
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
