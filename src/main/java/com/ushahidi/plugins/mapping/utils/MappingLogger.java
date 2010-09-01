package com.ushahidi.plugins.mapping.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class MappingLogger extends Logger {

	private static final MappingLoggerFactory LoggerFactory = new MappingLoggerFactory();
	
	public static MappingLogger getLogger(String name) {
		return (MappingLogger) Logger.getLogger(name, LoggerFactory);
	}
	
	public static MappingLogger getLogger(Class<? extends Object> clazz) {
		return (MappingLogger) Logger.getLogger(clazz.getName(), LoggerFactory);
	}
	
	protected MappingLogger(String name) {
		super(name);
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

	@Override
	public void debug(Object text) {
		if (MappingProperties.isDebugMode()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(this.getName());
			stringBuilder.append(" - ");
			stringBuilder.append(text);
			System.out.println(stringBuilder);
		}
		else {
			super.debug(text);
		}
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
	
	@Override
	public void error(Object text) {
		if (MappingProperties.isDebugMode()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(this.getName());
			stringBuilder.append(" - ");
			stringBuilder.append(text);
			System.err.println(stringBuilder);
		}
		else {
			super.error(text);
		}
	}
	
}

class MappingLoggerFactory implements LoggerFactory {
    public MappingLoggerFactory() { 
    }

    public Logger makeNewLoggerInstance(String name) {
        return new MappingLogger(name);
    }
}