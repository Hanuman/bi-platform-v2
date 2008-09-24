package org.pentaho.platform.plugin.action.kettle;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.pentaho.di.core.config.BasicConfigManager;
import org.pentaho.di.core.plugins.PluginLocation;

public class PlatformConfigManager<T extends PluginLocation> extends
		BasicConfigManager<T> {

	private File pluginsFolder;

	PlatformConfigManager(File pluginsFolder) {
		this.pluginsFolder = pluginsFolder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<T> load()  {
		Set<T> configObjs = new LinkedHashSet<T>();
		PluginLocation pl = new PluginLocation();
		pl.setId(pluginsFolder.getAbsolutePath());
		pl.setLocation(pluginsFolder.getAbsolutePath());
		configObjs.add((T) pl);
		
		return configObjs;
	}
}
