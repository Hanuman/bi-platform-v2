package org.pentaho.platform.plugin.services.webservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IPentahoSession;

public interface IWebServiceConfigurator extends AxisConfigurator {

  /** 
   * Sets the session used for initialization
   */
  public void setSession( IPentahoSession session );

  /**
   * Unloads and then re-loads the web services
   */
  public void reloadServices() throws AxisFault;

  /**
   * Sets the enabled state of a named web service and returns a boolean
   * indicating whether any change of state was successful
   * @param name The name of the web service
   * @param enabled The new state of the web service
   * @return Success of state change attempt
   * @throws AxisFault
   */
  public boolean setEnabled( String name, boolean enabled ) throws AxisFault;
  
  /**
   * Returns a web service wrapper using the service name as the key
   * @param name Web service name
   * @return Web service wrapper for the specific service
   */
  public IWebServiceWrapper getServiceWrapper( String name );
  
  /**
   * Unloads the web services.
   */
  public void unloadServices() throws AxisFault;

  /**
   * Returns the logger for this class
   * @return Log instance
   */
  public Log getLogger();
}
