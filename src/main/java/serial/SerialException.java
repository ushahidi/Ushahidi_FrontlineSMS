/**
 * 
 */
package serial;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Alex
 */
@SuppressWarnings("serial")
public abstract class SerialException extends Exception {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/**
	 * Creates a new serial Exception from the supplied {@link Exception}, setting the cause
	 * as the supplied exception and the message as the supplied exception's message.
	 * @param cause The cause of this exception
	 */
	protected SerialException(Exception cause) {
		super(cause.getMessage(), cause);
	}

//> ACCESSORS

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES
	/**
	 * @param clazz 
	 * @param cause 
	 * @param <T> 
	 * @return A new exception of the requested class.
	 */
	private static final <T extends SerialException> T create(Class<T> clazz, Exception cause) {
		if(cause == null) throw new IllegalStateException("Cause must not be null.");
		if(clazz == null) throw new IllegalStateException("Class must not be null.");
		try {
			return clazz.getConstructor(Exception.class).newInstance(cause);
		} catch (Exception ex) {
			throw new IllegalStateException("Exception generation failed from class: " + clazz.getName());
		}
	}

//> STATIC HELPER METHODS
	/**
	 * This method will take an {@link Exception} thrown by javax.comm or gnu.io (RXTXserial) and
	 * attempt to convert them into {@link Exception} types defined in this package.  If the conversion
	 * is possible, then the new exception object will be thrown wrapping the original one and using
	 * it's message. If no wrapping could be done, this method will do nothing.
	 * @param clazz The class of exception to match the cause with
	 * @param <T> A type of {@link SerialException} that will be thrown if the provided cause matches {@link Class#getSimpleName()} with it
	 * @param cause An exception to convert to a common type.
	 * @throws T If the provided exception matched the supplied type
	 */
	private static final <T extends SerialException> void throwIfMatches(Class<T> clazz, Exception cause) throws T {
		String expectedSimpleName = clazz.getSimpleName();
		String actualSimpleName = cause.getClass().getSimpleName();
		if(expectedSimpleName.equals(actualSimpleName)) {
			throw SerialException.create(clazz, cause);
		}
	}
	
	/**
	 * This method will take an {@link InvocationTargetException} and check the cause of it.  If the cause has the
	 * expected simple name, it will be wrapped as the required exception.
	 * @param clazz The class of exception to match the cause with
	 * @param <T> A type of {@link SerialException} that will be thrown if the provided cause matches {@link Class#getSimpleName()} with it
	 * @param invocationException An {@link InvocationTargetException} caused by calling a reflected method
	 * @throws T If the provided exception matched the supplied type
	 */
	static final <T extends SerialException> void throwIfMatches(Class<T> clazz, InvocationTargetException invocationException) throws T {
		throwIfMatches(clazz, (Exception) invocationException.getCause());
	}
}
