package org.pentaho.platform.plugin.services.pluginmgr;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class PluginManager implements IPentahoSystemListener, IPentahoPublisher {
	
	public PluginManager() {
		
	}

	public boolean startup(IPentahoSession session) {
		  
		// from IPentahoSystemListener
		List<String> comments = new ArrayList<String>();
		IPluginSettings pluginSettings = PentahoSystem.get(IPluginSettings.class, session);
		if( pluginSettings == null ) {
			// we cannot continue without the PluginSettings
			Logger.error( getClass().toString(), Messages.getErrorString("PluginManager.ERROR_0001_PLUGIN_SETTINGS_NOT_CONFIGURED")); //$NON-NLS-1$
			return false;
		}
		pluginSettings.updatePluginSettings( session, comments );
		return true;
	}

	public void shutdown() {
		// nothing to do here
	}
	
	public String getDescription() {
		// from IPentahoPublisher
		return Messages.getString("PluginManager.USER_REFRESH_PLUGINS"); //$NON-NLS-1$
	}

	public String getName() {
		// from IPentahoPublisher
		return Messages.getString("PluginManager.USER_PLUGIN_MANAGER"); //$NON-NLS-1$
	}

	public String publish(IPentahoSession session, int loggingLevel) {
		// from IPentahoPublisher
		List<String> comments = new ArrayList<String>();
		IPluginSettings pluginSettings = PentahoSystem.get(IPluginSettings.class, session);
		if( pluginSettings == null ) {
			// we cannot continue without the PluginSettings
			Logger.error( getClass().toString(), Messages.getErrorString("PluginManager.ERROR_0001_PLUGIN_SETTINGS_NOT_CONFIGURED")); //$NON-NLS-1$
			return Messages.getString("PluginManager.ERROR_0001_PLUGIN_SETTINGS_NOT_CONFIGURED"); //$NON-NLS-1$
		}
		pluginSettings.updatePluginSettings( session, comments );
		return comments.toString();
	}
	
}
