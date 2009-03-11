/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.kettle;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.pentaho.di.core.config.BasicConfigManager;
import org.pentaho.di.core.plugins.PluginLocation;
import org.pentaho.platform.api.engine.ISolutionFile;

/**
 * This configuration manager can be used to load kettle plugins located at ${PENTAHO_SOLUTIONS}/system/kettle/plugins
 * 
 * Do not create separate folders for steps and jobs in the plugins directory.  Simply place jar files or folders containing the 
 * relevant plugin files, including plugin.xml.
 * 
 * @author Alex Silva
 *
 * @param <T> 
 * @see PluginLocation
 */
public class PlatformConfigManager<T extends PluginLocation> extends
		BasicConfigManager<T> {

	private ISolutionFile pluginsFolder;

	PlatformConfigManager(ISolutionFile pluginsFolder) {
		this.pluginsFolder = pluginsFolder;
	}

	@SuppressWarnings("unchecked")
	public Collection<T> load()  {
		Set<T> configObjs = new LinkedHashSet<T>();
		PluginLocation pl = new PluginLocation();
		pl.setId(pluginsFolder.getFullPath());
		pl.setLocation(pluginsFolder.getFullPath());
		configObjs.add((T) pl);
		
		return configObjs;
	}
}
