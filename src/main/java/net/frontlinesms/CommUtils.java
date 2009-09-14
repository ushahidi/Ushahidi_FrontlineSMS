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

//#ifdef COMM_JAVAX
import javax.comm.*;
//#else
//# import gnu.io.*;
//#endif

import net.frontlinesms.resources.ResourceUtils;

import org.apache.log4j.Logger;


/**
 * Utilities class for managing bugs in javax.comm classes.
 * @author Alex
 */
public class CommUtils {
	/** Logging object */
	private static Logger LOG = Utils.getLogger(CommUtils.class);
	/**
	 * Gets a FRESH list of available COM ports.  This method works around a bug with Java COM API
	 * that leads to ports being cached.  For more info on the bug, see http://forum.java.sun.com/thread.jspa?threadID=575580&messageID=2986928
	 * @return an enumeration of {@link CommPortIdentifier}s, as supplied by {@link CommPortIdentifier#getPortIdentifiers()}
	 */
	@SuppressWarnings("unchecked")
	public static Enumeration<CommPortIdentifier> getPortIdentifiers() {
		//#if COMM_JAVAX
		try {
			Field masterIdList = CommPortIdentifier.class.getDeclaredField("masterIdList");
			masterIdList.setAccessible(true);
			masterIdList.set(null, null);

			Method loadDriver = CommPortIdentifier.class.getDeclaredMethod("loadDriver", new Class[] {String.class});			
			loadDriver.setAccessible(true);
			loadDriver.invoke(null, new Object[] {ResourceUtils.getConfigDirectoryPath() + "properties/javax.comm.properties"});
		} catch(Exception ex) {
			LOG.warn("There was an error trying to reset javax.comm ports cache.", ex);
		}
		//#endif
		return CommPortIdentifier.getPortIdentifiers();
	}

}
