// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2009, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package serial;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Some methods to use generics with reflection.
 * 
 * In this class, {@link Field#setAccessible(boolean)} and {@link Method#setAccessible(boolean)} are always called and
 * set to true.  This is because some class implementations, e.g. com.sun.comm.Win32SerialPort, will throw Exceptions like this:
 * <code>java.lang.IllegalAccessException: Class serial.ReflectionHelper can not access a member of class com.sun.comm.Win32SerialPort with modifiers "public"
	at sun.reflect.Reflection.ensureMemberAccess(Reflection.java:65)</code>
 */
public class ReflectionHelper
{
	/**
	 * Invokes the given method on the given object with the given arguments.
	 * The result is cast to T and every kind of exception is wrapped as
	 * RuntimeException
	 * @param returnType 
	 * @param m 
	 * @param obj 
	 * @param args 
	 * @param <T> 
	 * @return The result of the method invocation
	 * @throws InvocationTargetException 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invoke(Class<T> returnType, Method m, Object obj, Object... args) throws InvocationTargetException {
		try {
			return (T) m.invoke(obj, args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invokes the given method on the given object with the given arguments.
	 * The result is cast to T and every kind of exception is wrapped as
	 * RuntimeException
	 * @param returnType 
	 * @param m 
	 * @param obj 
	 * @param args 
	 * @param <T> 
	 * @return The result of the method invocation
	 */
	public static <T> T invokeWithoutInvocationException(Class<T> returnType, Method m, Object obj, Object... args) {
		try {
			return invoke(returnType, m, obj, args);
		} catch (InvocationTargetException e) {
			// This exception is wrapped in a strange way.  The reasoning was not documented in SMS Lib code, but best not to fiddle.
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
	}

	/**
	 * Invokes the given void method on the given object with the given arguments.
	 * @param m 
	 * @param obj 
	 * @param args 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public static void invoke(Method m, Object obj, Object... args) throws InvocationTargetException {
		try {
			m.invoke(obj, args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invokes the given void method on the given object with the given arguments.
	 * @param m 
	 * @param obj 
	 * @param args 
	 */
	public static void invokeWithoutInvocationException(Method m, Object obj, Object... args) {
		try {
			invoke(m, obj, args);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
	}
	
	/**
	 * Get a method by reflection, and wrap all expected {@link Exception} as {@link RuntimeException}.
	 * @param clazz
	 * @param methodName
	 * @param parameterTypes
	 * @return the requested {@link Method}
	 */
	public static final Method getMethod(Class<?> clazz, String methodName, Class<?> ... parameterTypes) {
		try {
			Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Gets the value of a static final <code>int</code> from the supplied class by reflection.
	 * @param clazz The class to get the value from
	 * @param fieldName The name of a public static int field of the supplied class.
	 * @return The value of the constant.
	 * @throws IllegalAccessException if this is thrown by the reflection
	 * @throws IllegalArgumentException if this is thrown by the reflection
	 * @throws NoSuchFieldException if this is thrown by the reflection
	 * @throws SecurityException if this is thrown by the reflection
	 */
	public static int getStaticInt(Class<?> clazz, String fieldName) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		Field f = clazz.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.getInt(null);
	}
}
