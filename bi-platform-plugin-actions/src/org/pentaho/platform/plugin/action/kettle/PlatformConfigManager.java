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
