/**
 * 
 */
package serial;

/**
 * Factory class for getting the serial classes.
 * @author Alex
 */
public class SerialClassFactory {
//> STATIC CONSTANTS
	/** Package name for javax.comm */
	public static final String PACKAGE_JAVAXCOMM = "javax.comm";
	/** Package name for RXTXserial */
	private static final String PACKAGE_RXTX = "gnu.io";
	/** Singleton instance of this class */
	private static SerialClassFactory INSTANCE; 

//> INSTANCE PROPERTIES
	/** The name of the package of the serial implementation to use, either {@link #PACKAGE_JAVAXCOMM} or {@value #PACKAGE_RXTX} */
	private final String serialPackageName;

//> CONSTRUCTORS
	/**
	 * Constructs a {@link SerialClassFactory}.
	 * TODO this currently tests RXTX first, but as javax.comm provides better device support, we should really test that first.  Need to isolate how to tell if it works.
	 */
	private SerialClassFactory() {
		String serialPackageName;
		try {
			// TODO log that we're trying RXTX
			/*
			 * RXTX will throw: class java.lang.UnsatisfiedLinkError :: no rxtxSerial in java.library.path
			 * if it cannot load.  Hopefully javax.comm will do similar.
			 */
			Class.forName(PACKAGE_RXTX + "." + CommPortIdentifier.class.getSimpleName());
			serialPackageName = PACKAGE_RXTX;
		} catch(Throwable t) {
			t.printStackTrace(); // TODO log this instead of printing it
			// TODO test this package works - it's possible neither does
			serialPackageName = PACKAGE_JAVAXCOMM;
		}
		// TODO log what we have ended up with
		this.serialPackageName = serialPackageName;
	}

//> ACCESSORS
	/**
	 * @return {@link #serialPackageName}.
	 */
	public String getSerialPackageName() {
		return serialPackageName;
	}

//> INSTANCE HELPER METHODS

//> STATIC FACTORIES

//> STATIC HELPER METHODS
	/**
	 * Get the singleton instance of this class.  If the singleton is not yet
	 * initialized, this method will do that too.
	 * @return the singleton instance of this class
	 * @throws IllegalStateException If there was a problem initialising the class
	 */
	public static final synchronized SerialClassFactory getInstance() throws IllegalStateException {
		if(INSTANCE == null) {
			INSTANCE = new SerialClassFactory();
		}
		return INSTANCE;
	}
	
	/**
	 * Attempt to get a class by name, first from the {@link #PACKAGE_JAVAXCOMM}, and then from {@link #PACKAGE_RXTX}
	 * TODO once we have decided which package we are using, we should probably try exclusively to get classes from that package.  E.g. it might be possible to get javax.comm classes even though the library is broken.  If we can detect that, do it here the first time this method is called.
	 * @param clazz The class whose namesake we should fetch
	 * @return An implementation of the desired class
	 */
	public Class<?> forName(Class<?> clazz) {
		try {
			return Class.forName(this.serialPackageName + "." + clazz.getSimpleName());
		} catch (ClassNotFoundException ex) {
			throw new IllegalStateException(clazz.getSimpleName() + " class not found in package " + this.serialPackageName);
		}
	}
}
