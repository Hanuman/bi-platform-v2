package org.pentaho.platform.webservice.plugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.webservice.plugin.messages.Messages;
import org.pentaho.platform.webservice.services.datasource.DatasourceServiceWrapper;
import org.pentaho.webservice.core.BaseServiceSetup;
import org.pentaho.webservice.core.IWebServiceWrapper;
import org.pentaho.webservice.core.content.WebServiceConst;

/**
 * The web services setup objects for the BI server webservices plugin.
 * This class:
 * 1) Provides an input stream for the axis configuration XML
 * which is located in the root of the plugin folder (getConfigXml)
 * 2) Defines the web services that are available (getWebServiceWrappers)
 * 3) Provides persistence for enabled/disabled status of each web service
 * @author jamesdixon
 *
 */
public class PluginServiceSetup extends BaseServiceSetup {

  private static final long serialVersionUID = -4219285702722007821L;

  protected static PluginServiceSetup instance = null;
  
  private static final Log logger = LogFactory.getLog(PluginServiceSetup.class);

  public PluginServiceSetup() {
    super();
  }
  
  @Override
  public Log getLogger() {
    return logger;
  }

  @Override
  public void init() {
  }

  @Override
  public InputStream getConfigXml( ) {

    // Setup up the base URL for webservices calls
    WebServiceConst.baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    // create the file path to the axis XML
    try {
      IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);

      InputStream in = resLoader.getResourceAsStream(this.getClass(), "axis2_config.xml" ); //$NON-NLS-1$

      return in;
    } catch (Exception e) {
      error( Messages.getErrorString( "PluginServiceSetup.ERROR_0001_BAD_CONFIG_FILE", "axis2_config.xml" ), e ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return null;
  }
  
  @Override
  public boolean setEnabled( String name, boolean enabled ) throws AxisFault {
    return true;
  }
  
  @Override
  protected List<IWebServiceWrapper> getWebServiceWrappers() {

    List<IWebServiceWrapper> wrappers = new ArrayList<IWebServiceWrapper>();
    
    IWebServiceWrapper wrapper = new DatasourceServiceWrapper();
    wrappers.add( wrapper );
    
    return wrappers;
  }
  
  @Override
  protected void addTransports( AxisService axisService ) {
    // the defaults include http so we are good to go
  }

  @Override
  protected void addServiceEndPoints( AxisService axisService ) {
    String endPoint1 = WebServiceConst.getExecuteUrl()+"/"+axisService.getName(); //$NON-NLS-1$
    axisService.setEPRs(new String[] { endPoint1 } );
  }

}
