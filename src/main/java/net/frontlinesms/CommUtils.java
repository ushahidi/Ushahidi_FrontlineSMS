/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;

import serial.*;

import net.frontlinesms.resources.ResourceUtils;

import org.apache.log4j.Logger;


/**
 * Utilities class for managing bugs in javax.comm classes.
 * 
 * 
 * FIXME the {@link #getPortIdentifiers()} method should be grafted into {@link CommPortIdentifier} where it is needed
 * 
 * @author Alex
 */
public final class CommUtils {
	/** Logging object */
	private static Logger LOG = Utils.getLogger(CommUtils.class);
	/**
	 * Gets a FRESH list of available COM ports.  This method works around a bug with Java COM API
	 * that leads to ports being cached.  For more info on the bug, see http://forum.java.sun.com/thread.jspa?threadID=575580&messageID=2986928
	 * @return an enumeration of {@link CommPortIdentifier}s, as supplied by {@link CommPortIdentifier#getPortIdentifiers()}
	 */
	@SuppressWarnings("restriction")
	public static synchronized Enumeration<CommPortIdentifier> getPortIdentifiers() {
		/* This method is synchronized to prevent strange things happening when reloading the config etc. */
		if(SerialClassFactory.PACKAGE_JAVAXCOMM.equals(SerialClassFactory.getInstance().getSerialPackageName())) {
			try {
				Class<?> commPortIdentifierClass = javax.comm.CommPortIdentifier.class;
				
				Field masterIdList = commPortIdentifierClass.getDeclaredField("masterIdList");
				masterIdList.setAccessible(true);
				masterIdList.set(null, null);
	
				Method loadDriver = commPortIdentifierClass.getDeclaredMethod("loadDriver", String.class);			
				loadDriver.setAccessible(true);
				loadDriver.invoke(null, ResourceUtils.getConfigDirectoryPath() + "properties/javax.comm.properties");
			} catch(Exception ex) {
				LOG.warn("There was an error trying to reset javax.comm ports cache.", ex);
			}
		}
		return CommPortIdentifier.getPortIdentifiers();
	}

}
