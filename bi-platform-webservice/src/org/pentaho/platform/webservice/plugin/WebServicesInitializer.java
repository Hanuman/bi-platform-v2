package org.pentaho.platform.webservice.plugin;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;
import org.pentaho.platform.webservice.plugin.messages.Messages;
import org.pentaho.webservice.core.AxisConfig;
import org.pentaho.webservice.core.IWebServiceConfigurator;

public class WebServicesInitializer extends SimpleContentGenerator implements IPentahoInitializer {

  private static final long serialVersionUID = 227084738820361822L;

  private static final Log logger = LogFactory.getLog(WebServicesInitializer.class);

  public WebServicesInitializer() {
    
  }
  
  /**
   * Initializes the webservice system.
   * 1) Gets the object factory for the plugin system
   * 2) Adds PluginServiceSetup to the factory
   * 3) Causes the webservices system to initialize using PluginServiceSetup
   */
  @SuppressWarnings({ "deprecation" })
  public void init(IPentahoSession session) {

    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
      IWebServiceConfigurator iConfig = new PluginServiceSetup();
      AxisConfig.getInstance( iConfig );
    } catch (Exception e) {
      error( Messages.getErrorString("WebServicesInitializer.ERROR_0001_BAD_INIT"), e ); //$NON-NLS-1$
    } finally {
      Thread.currentThread().setContextClassLoader( originalLoader );
    }
  }
  

  /**
   * This class does not generate content.
   */
  @Override
  public void createContent(OutputStream arg0) throws Exception {
  }

  @Override
  public String getMimeType() {
    return null;
  }

  @Override
  public Log getLogger() {
    return logger;
  }


}
