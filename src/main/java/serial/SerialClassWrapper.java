/**
 * 
 */
package serial;

import java.lang.reflect.Method;

/**
 * Wrapper for a class with more than one implementation but no common interface.
 * @author Alex
 */
abstract class SerialClassWrapper {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES
	/**
	 * The real object that this class wraps.
	 */
	private final Object realObject;

//> CONSTRUCTORS
	/**
	 * Create a new instance of this class, wrapping the supplied object.
	 * @param realObject value for {@link #realObject}
	 */
	protected SerialClassWrapper(Object realObject) {
		this.realObject = realObject;
	}

//> ACCESSORS
	/** @return {@link #realObject} */
	public Object getRealObject() {
		return realObject;
	}
	
	/** @return the class that this wraps. */
	protected Class<?> getWrappedClass() {
		return this.realObject.getClass();
	}
	
	/**
	 * Executes a void method on the superclass.
	 * @param methodName The name of the method to call
	 */
	protected void invokeWithoutInvocationException(String methodName) {
		Method method = ReflectionHelper.getMethod(this.getWrappedClass(), methodName);
		ReflectionHelper.invokeWithoutInvocationException(method, this.realObject);
	}
	
	/**
	 * Executes a void method on the superclass.
	 * @param methodName The name of the method to call
	 * @param parameterTypes Parameters for the method
	 * @param args Arguments to pass to the method
	 */
	protected void invokeWithoutInvocationException(String methodName, Class<?>[] parameterTypes, Object... args) {
		Method method = ReflectionHelper.getMethod(this.getWrappedClass(), methodName, parameterTypes);
		ReflectionHelper.invokeWithoutInvocationException(method, this.realObject, args);
	}
	
	/**
	 * Executes a getter of generic type and returns the response.
	 * @param <T> The return type of the method
	 * @param clazz The class of the return type of the method
	 * @param methodName The name of the method to call
	 * @return The response from the getter.
	 */
	protected <T> T invokeWithoutInvocationException(Class<T> clazz, String methodName) {
		Method method = ReflectionHelper.getMethod(this.getWrappedClass(), methodName);
		return ReflectionHelper.invokeWithoutInvocationException(clazz, method, this.realObject);		
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
