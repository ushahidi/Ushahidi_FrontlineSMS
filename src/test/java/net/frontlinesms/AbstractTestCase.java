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

import java.io.*;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * This class loads all the properties needed by the test cases.
 * <br/>This file is found in <code>src/test/java/TestValues.properties</code>
 *
 * @author Carlos Eduardo Endler Genz
 * @date 19/01/2009
 */
public abstract class AbstractTestCase extends TestCase {
	protected Properties PROPERTIES;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PROPERTIES = new Properties();
		InputStream out = this.getClass().getResourceAsStream("/TestValues.properties");
		PROPERTIES.load(out);
	}
}
