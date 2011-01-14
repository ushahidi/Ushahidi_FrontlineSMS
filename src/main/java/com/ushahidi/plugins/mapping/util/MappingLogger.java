package com.ushahidi.plugins.mapping.util;

import net.frontlinesms.FrontlineUtils;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.lang.reflect.Method;

/**
 * @author bmuita
 * Borrowed from The complete log4j manual  
 */
public class MappingLogger {
	// Our fully qualified class name.
	static String FQCN = MappingLogger.class.getName();

	static boolean JDK14 = false;
	static {
		String version = System.getProperty("java.version");
		if (version != null) {
			JDK14 = version.startsWith("1.4");
		}
	}

	private final Logger logger;

	public MappingLogger(Class<? extends Object> clazz) {
		this.logger = FrontlineUtils.getLogger(clazz);
	}

	public void trace(Object msg) {
		logger.log(FQCN, Level.TRACE, msg, null);
	}

	public void trace(Object msg, Throwable t) {
		logger.log(FQCN, Level.TRACE, msg, t);
		logNestedException(Level.TRACE, msg, t);
	}

	public boolean isTraceEnabled() {
		return logger.isEnabledFor(Level.TRACE);
	}
	
	public void debug(String format, Object ... args) {
		debug(String.format(format, args));
	}
	
	public void debug(String format, String [] args) {
		StringBuffer sb = new StringBuffer();
		for (String arg : args) {
			if (sb.length() > 0) {
				 sb.append(",");
			}
			else {
				 sb.append("[");
			}
			sb.append(arg);
		}
		sb.append("]");
		debug(String.format(format, sb));
	}	

	public void debug(Object msg) {
		logger.log(FQCN, Level.DEBUG, msg, null);
	}

	public void debug(Object msg, Throwable t) {
		logger.log(FQCN, Level.DEBUG, msg, t);
		logNestedException(Level.DEBUG, msg, t);
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public void info(Object msg) {
		logger.log(FQCN, Level.INFO, msg, null);
	}

	public void info(Object msg, Throwable t) {
		logger.log(FQCN, Level.INFO, msg, t);
		logNestedException(Level.INFO, msg, t);
	}

	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();

	}

	public void warn(Object msg) {
		logger.log(FQCN, Level.WARN, msg, null);
	}

	public void warn(Object msg, Throwable t) {
		logger.log(FQCN, Level.WARN, msg, t);
		logNestedException(Level.WARN, msg, t);
	}
	
	public void error(String format, Object ... args) {
		error(String.format(format, args));
	}
	
	public void error(String format, String [] args) {
		StringBuffer sb = new StringBuffer();
		for (String arg : args) {
			if (sb.length() > 0) {
				 sb.append(",");
			}
			else {
				 sb.append("[");
			}
			sb.append(arg);
		}
		sb.append("]");
	    error(String.format(format, sb));
	}	

	public void error(Object msg) {
		logger.log(FQCN, Level.ERROR, msg, null);
	}

	public void error(Object msg, Throwable t) {
		logger.log(FQCN, Level.ERROR, msg, t);
		logNestedException(Level.ERROR, msg, t);
	}

	public void fatal(Object msg) {
		logger.log(FQCN, Level.FATAL, msg, null);
	}

	public void fatal(Object msg, Throwable t) {
		logger.log(FQCN, Level.FATAL, msg, t);
		logNestedException(Level.FATAL, msg, t);
	}

	void logNestedException(Level level, Object msg, Throwable t) {
		if (t == null)
			return;
		try {
			Class<? extends Object> tC = t.getClass();
			Method mA[] = tC.getMethods();
			Method nextThrowableMethod = null;
			for (int i = 0; i < mA.length; i++) {
				if (("getCause".equals(mA[i].getName()) && !JDK14)
						|| "getRootCause".equals(mA[i].getName())
						|| "getNextException".equals(mA[i].getName())
						|| "getException".equals(mA[i].getName())) {
					// check param types
					Class<? extends Object> params[] = mA[i].getParameterTypes();
					if (params == null || params.length == 0) {
						// just found the getter for the nested throwable
						nextThrowableMethod = mA[i];
						break; // no need to search further
					}
				}
			}
			if (nextThrowableMethod != null) { // get the nested throwable and
												// log it
				Throwable nextT = (Throwable) nextThrowableMethod.invoke(t,
						new Object[0]);
				if (nextT != null) {
					this.logger.log(FQCN, level, "Previous log CONTINUED",
							nextT);
				}
			}
		} catch (Exception e) {
			// do nothing
		}
	}
}