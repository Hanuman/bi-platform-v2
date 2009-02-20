package org.pentaho.platform.webservice.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.plugin.services.webservices.AxisConfig;
import org.pentaho.platform.plugin.services.webservices.IWebServiceConfigurator;

/**
 * TODO BISERVER-2803 - this class acts as a plugin initializer.  We need to introduce plugin 
 * lifecycle support to the platform plugin framework and hook this in as an initializer.  This class is
 * currently configured as a content generator as a workaround to not having an initialize event.
 * It just so happens that content generators are test-loaded when the plugin is registered,
 * and this class depends on that behavior, however this is not actually a content generator at all.
 *   -ADP
 */
public class WebServicesInitializer  implements IPluginLifecycleListener {

  private static final long serialVersionUID = 227084738820361822L;

  public void init() throws PluginLifecycleException { }

  /**
   * Initializes the webservice system.
   * 1) Gets the object factory for the plugin system
   * 2) Adds PluginServiceSetup to the factory
   * 3) Causes the webservices system to initialize using PluginServiceSetup
   * 
   * @see IPluginLifecycleListener#loaded()
   */
  public void loaded() throws PluginLifecycleException {
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
      IWebServiceConfigurator iConfig = new PluginServiceSetup();
      AxisConfig.getInstance( iConfig );
    } finally {
      Thread.currentThread().setContextClassLoader( originalLoader );
    }
  }

  public void unLoaded() throws PluginLifecycleException { }
}
