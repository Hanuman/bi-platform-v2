package org.pentaho.platform.plugin.services.pluginmgr;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class PluginAdapter implements IPentahoSystemListener, IPentahoPublisher {
	
	public PluginAdapter() {
		
	}

	public boolean startup(IPentahoSession session) {
		  
		// from IPentahoSystemListener
		List<String> comments = new ArrayList<String>();
		IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, session);
		if( pluginManager == null ) {
			// we cannot continue without the PluginSettings
			Logger.error( getClass().toString(), Messages.getErrorString("PluginAdapter.ERROR_0001_PLUGIN_MANAGER_NOT_CONFIGURED")); //$NON-NLS-1$
			return false;
		}
		pluginManager.updatePluginSettings( session, comments );
		return true;
	}

	public void shutdown() {
		// nothing to do here
	}
	
	public String getDescription() {
		// from IPentahoPublisher
		return Messages.getString("PluginAdapter.USER_REFRESH_PLUGINS"); //$NON-NLS-1$
	}

	public String getName() {
		// from IPentahoPublisher
		return Messages.getString("PluginAdapter.USER_PLUGIN_MANAGER"); //$NON-NLS-1$
	}

	public String publish(IPentahoSession session, int loggingLevel) {
		// from IPentahoPublisher
		List<String> comments = new ArrayList<String>();
		IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, session);
		if( pluginManager == null ) {
			// we cannot continue without the PluginSettings
			Logger.error( getClass().toString(), Messages.getErrorString("PluginAdapter.ERROR_0001_PLUGIN_MANAGER_NOT_CONFIGURED")); //$NON-NLS-1$
			return Messages.getString("PluginAdapter.ERROR_0001_PLUGIN_MANAGER_NOT_CONFIGURED"); //$NON-NLS-1$
		}
		pluginManager.updatePluginSettings( session, comments );
		return comments.toString();
	}
	
}
