package org.pentaho.platform.plugin.services.webservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;
import org.pentaho.platform.util.logging.Logger;


/**
 * The central configuration point for the web services system.
 * This object needs to be provided with an implementation-specific object
 * factory that can supply an org.pentaho.webservice.IWebServiceConfigurator 
 * @author jamesdixon
 *
 */
public class AxisConfig  {

  protected IWebServiceConfigurator axisConfigurator;
  
  protected ConfigurationContext configContext;
  
  protected static AxisConfig instance = null;
  
  /**
   * Returns the current instance of this type. This getInstance method cannot 
   * be called until getInstance( IPentahoObjectFactory factory ) has been
   * called
   * @return Current AxisConfig instance
   */
  public static AxisConfig getInstance() {
    return instance;
  }
  
  /**
   * This call to getInstance needs to be called before any calls to the 
   * getInstance() method.
   * @param factory Object factory to be used to obtain an IWebServiceConfigurator
   * @return A new AxisConfig instance
   */
  public static AxisConfig getInstance( IWebServiceConfigurator iConfigurator ) {
    if( AxisConfig.instance == null ) {
      AxisConfig.instance = new AxisConfig( iConfigurator );
      try {
        AxisConfig.instance.init( );
      } catch (AxisFault e) {
        Logger.error( AxisConfig.class.getName(), Messages.getErrorString("AxisConfig.ERROR_0001_OBJECT_FACTORY_EXCEPTION"), e ); //$NON-NLS-1$
        AxisConfig.instance = null;
      }
    }
    return AxisConfig.instance;
  }
  
  /**
   * Resets the webservice system by re-initializing this type and reloading
   * the webservices
   */
  public void reset( ) {
    // throw the current instance away
    IWebServiceConfigurator iConfigurator = instance.getAxisConfigurator();
    instance = null;
    // get another instance
    getInstance( iConfigurator );
    instance.getAxisConfigurator().loadServices();
  }
  
  public AxisConfig( IWebServiceConfigurator axisConfigurator ) {
    this.axisConfigurator = axisConfigurator;
  }
  
  /**
   * Returns the IWebServiceConfigurator object
   * @return the IWebServiceConfigurator object
   */
  public IWebServiceConfigurator getAxisConfigurator() {
    return axisConfigurator;
  }

  /**
   * Returns the current Axis ConfigurationContext object
   * @return ConfigurationContext
   */
  public ConfigurationContext getConfigurationContext() {
    return configContext;
  }
  
  /**
   * Initializes the Axis system:
   * 1) Gets the IWebServiceConfigurator object from the object factory
   * 2) Initializes Axis using the IWebServiceConfigurator implementation
   * @throws AxisFault
   * @throws ObjectFactoryException
   */
  public void init( ) throws AxisFault {
    
    Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

    configContext = ConfigurationContextFactory.createConfigurationContext( axisConfigurator );
    configContext.setProperty(Constants.CONTAINER_MANAGED, Constants.VALUE_TRUE);

  }

}
