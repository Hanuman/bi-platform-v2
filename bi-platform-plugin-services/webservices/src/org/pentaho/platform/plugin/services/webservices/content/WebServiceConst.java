package org.pentaho.platform.plugin.services.webservices.content;

/**
 * A class that holds global constants for the webservice system
 * @author jamesdixon
 *
 */
public class WebServiceConst {

  public static final String PLUGIN_NAME = "webservices"; //$NON-NLS-1$
  
  public static final String AXIS_CONFIG_FILE = "axis2_config.xml"; //$NON-NLS-1$
  
  public static String baseUrl = null;
  
  /**
   * Returns the URL that can be used to get a list of the web services
   * @return Services list URL
   */
  public static String getDiscoveryUrl() {
    return WebServiceConst.baseUrl + "content/ws-services"; //$NON-NLS-1$
  }

  /**
   * Returns the URL to the used as the base for the WSDL content generator URLs
   * @return WSDL URL base
   */
  public static String getWsdlUrl() {
    return baseUrl + "content/ws-wsdl"; //$NON-NLS-1$
  }

  /**
   * Returns the URL to the used as the base for the service execution URLs
   * @return Execution URL base
   */
  public static String getExecuteUrl() {
    return baseUrl + "content/ws-run"; //$NON-NLS-1$
  }

}
