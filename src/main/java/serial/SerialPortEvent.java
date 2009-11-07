
package serial;

import java.lang.reflect.Constructor;

/**
 * A serial port event.
 * <p>
 * <b>Please note: </b>This is a wrapper around
 * <code>javax.comm.SerialPortEvent</code> (and so
 * <code>gnu.io.SerialPortEvent</code>). The API definition is taken from
 * Sun. So honor them!
 * </p>
 * 
 * @author Jagane Sundar
 */
public class SerialPortEvent extends SerialClassWrapper
{
//> STATIC CONSTANTS // TODO these constants should probably be enclosed in a static final singleton class hidden behind the static methods, as currently this initialisation is very weird and ill-defined
	/** Break interrupt. */
	public static final int BI;
	/** Carrier detect. */
	public static final int CD;
	/** Clear to send. */
	public static final int CTS;
	/** Data available at the serial port. */
	public static final int DATA_AVAILABLE;
	/** Data set ready. */
	public static final int DSR;
	/** Framing error. */
	public static final int FE;
	/** Overrun error. */
	public static final int OE;
	/** Output buffer is empty. */
	public static final int OUTPUT_BUFFER_EMPTY;
	/** Parity error. */
	public static final int PE;
	/** Ring indicator. */
	public static final int RI;
	static
	{
		// SET UP CONSTANTS
		try
		{
			Class<?> classSerialPortEvent = SerialClassFactory.getInstance().forName(SerialPortEvent.class);
			// get the value of constants
			BI = ReflectionHelper.getStaticInt(classSerialPortEvent, "BI");
			CD = ReflectionHelper.getStaticInt(classSerialPortEvent, "CD");
			CTS = ReflectionHelper.getStaticInt(classSerialPortEvent, "CTS");
			DATA_AVAILABLE = ReflectionHelper.getStaticInt(classSerialPortEvent, "DATA_AVAILABLE");
			DSR = ReflectionHelper.getStaticInt(classSerialPortEvent, "DSR");
			FE = ReflectionHelper.getStaticInt(classSerialPortEvent, "FE");
			OE = ReflectionHelper.getStaticInt(classSerialPortEvent, "OE");
			OUTPUT_BUFFER_EMPTY = ReflectionHelper.getStaticInt(classSerialPortEvent, "OUTPUT_BUFFER_EMPTY");
			PE = ReflectionHelper.getStaticInt(classSerialPortEvent, "PE");
			RI = ReflectionHelper.getStaticInt(classSerialPortEvent, "RI");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

//> CONSTRUCTORS
	/**
	 * Constructs a <code>SerialPortEvent</code> with the specified serial
	 * port, event type, old and new values. Application programs should not
	 * directly create <code>SerialPortEvent</code> objects.
	 * 
	 * @param srcport
	 *            source parallel port
	 * @param eventtype
	 *            event type
	 * @param oldvalue
	 *            old value
	 * @param newvalue
	 *            new value
	 */
	public SerialPortEvent(SerialPort srcport, int eventtype, boolean oldvalue, boolean newvalue)
	{
		super(constructRealObject(srcport, eventtype, oldvalue, newvalue));
	}

	SerialPortEvent(Object obj)
	{
		super(obj);
	}
	
	/**
	 * Constructs the real object for wrapping with this class.
	 * @param srcport
	 * @param eventtype
	 * @param oldvalue
	 * @param newvalue
	 * @return
	 */
	private static Object constructRealObject(SerialPort srcport, int eventtype, boolean oldvalue, boolean newvalue) {
		try
		{
			Constructor<?> constr = SerialClassFactory.getInstance().forName(SerialPortEvent.class).getConstructor(SerialPort.class, int.class, boolean.class, boolean.class);
			return constr.newInstance(srcport, eventtype, oldvalue, newvalue);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

//> ACCESSORS
	/**
	 * Gets the type of this event.
	 * 
	 * @return integer that can be equal to one of the following static
	 *         variables:
	 *         <code>BI, CD, CTS, DATA_AVAILABLE, DSR, FE, OE, OUTPUT_BUFFER_EMPTY, PE</code>
	 *         or <code>RI</code>.
	 * @since CommAPI 1.1
	 * @see gnu.io.SerialPortEvent#getEventType()
	 * @see javax.comm.SerialPortEvent#getEventType()
	 */
	@SuppressWarnings("restriction")
	public int getEventType() {
		return invokeWithoutInvocationException(int.class, "getEventType");
	}

	/**
	 * Gets the new value of the state change that caused the SerialPortEvent to
	 * be propagated. For example, when the CD bit changes, newValue reflects
	 * the new value of the CD bit.
	 * @return 
	 * @see gnu.io.SerialPortEvent#getNewValue()
	 * @see javax.comm.SerialPortEvent#getNewValue()
	 */
	@SuppressWarnings("restriction")
	public boolean getNewValue() {
		return invokeWithoutInvocationException(boolean.class, "getNewValue");
	}

	/**
	 * Gets the old value of the state change that caused the SerialPortEvent to
	 * be propagated. For example, when the CD bit changes, oldValue reflects
	 * the old value of the CD bit.
	 * @return 
	 * @see gnu.io.SerialPortEvent#getOldValue()
	 * @see javax.comm.SerialPortEvent#getOldValue()
	 */
	@SuppressWarnings("restriction")
	public boolean getOldValue() {
		return invokeWithoutInvocationException(boolean.class, "getOldValue");
	}
}
