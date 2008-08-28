package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PluginManager implements IPentahoSystemListener, IPentahoPublisher {
	
	public PluginManager() {
		
	}

	public boolean startup(IPentahoSession session) {
		  
		// from IPentahoSystemListener
		StringBuilder comments = new StringBuilder();
		PluginSettings pluginSettings = (PluginSettings) PentahoSystem.getObject( session, "IPluginSettings" );
		pluginSettings.updatePluginSettings( session, comments );
		return true;
	}

	public void shutdown() {
		// nothing to do here
	}
	
	public String getDescription() {
		// from IPentahoPublisher
		return "Refresh all of the plugin settings";
	}

	public String getName() {
		// from IPentahoPublisher
		return "Plugin Manager";
	}

	public String publish(IPentahoSession session, int loggingLevel) {
		// from IPentahoPublisher
		StringBuilder comments = new StringBuilder();
		PluginSettings pluginSettings = (PluginSettings) PentahoSystem.getObject( session, "IPluginSettings" );
		pluginSettings.updatePluginSettings( session, comments );
		return comments.toString();
	}
	
}
