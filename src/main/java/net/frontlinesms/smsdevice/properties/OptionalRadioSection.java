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
package net.frontlinesms.smsdevice.properties;

import java.util.*;


/**
 * Encapsulates a a set of radio buttons that enable/disabled a set of
 * fields according to user's selection.
 *
 * @author Carlos Eduardo Endler Genz
 * @date 09/02/2009
 */
public final class OptionalRadioSection <T extends Enum<?>> {
	private T value;
	private Map<T, LinkedHashMap<String, Object>> dependencies;

	public OptionalRadioSection(T value) {
		this.value = value;
		dependencies = new HashMap<T, LinkedHashMap<String,Object>>();
	}

	/** @return the dependencies for a particular option. */
	public Map<String, Object> getDependencies(T value) {
		return dependencies.get(value);
	}
	
	/** Gets all dependencies. */
	public Collection<LinkedHashMap<String, Object>> getAllDependencies() {
		return this.dependencies.values();
	}

	public void addDependency(T value, String property, Object propValue) {
		if (!dependencies.containsKey(value)) {
			dependencies.put(value, new LinkedHashMap<String, Object>());
		}
		dependencies.get(value).put(property, propValue);
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(T value) {
		this.value = value;
	}

}
