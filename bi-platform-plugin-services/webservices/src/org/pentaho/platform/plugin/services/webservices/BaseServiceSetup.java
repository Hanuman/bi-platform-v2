/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.services.webservices;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.AxisConfigBuilder;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;


/**
 * This is the base IWebServiceConfigurator class. Specific implementations can
 * subclass this.
 * @author jamesdixon
 *
 */
public abstract class BaseServiceSetup extends PentahoBase implements IWebServiceConfigurator {

  protected AxisConfiguration axisConfig = null;

  protected IPentahoSession session; // Session to use during initialization
  
  //map of the web service wrappers
  private Map<String, IWebServiceWrapper> services = new HashMap<String,IWebServiceWrapper>();  

  public abstract Log getLogger();
  
  public BaseServiceSetup() {
    init();
  }
  
  public void reloadServices() throws AxisFault {
    unloadServices();
    loadServices();
  }
  
  public void unloadServices() throws AxisFault {
    Set<String> keys = services.keySet();
    List<String> removed = new ArrayList<String>();
    // iterate through the list of web service wrappers
    for( String key : keys ) {
      IWebServiceWrapper wrapper = services.get( key );
      String serviceName = wrapper.getServiceClass().getSimpleName();
      // use the service name to remove them from the Axis system
      axisConfig.removeService( serviceName );
      // build a list of the ones removed
      removed.add( serviceName );
    }
    // now remove the wrappers from the services list
    for( String serviceName : removed ) {
      services.remove( serviceName );
    }

  }
  
  public IWebServiceWrapper getServiceWrapper( String name ) {
     return services.get( name );
  }

  /**
   * Creates the AxisConfiguration object using an XML document.
   * Subclasses of this class must provide the XML via an input stream.
   * The concrete implementation can store the XML file wherever it
   * wants as we only need an InputStream
   */
  public AxisConfiguration getAxisConfiguration() throws AxisFault {
    if( axisConfig != null ) {
      // we have already initialized
      return axisConfig;
    }
    try {

      // create a new AxisConfiguration
      axisConfig = new AxisConfiguration();
      
      // get the config XML input stream
      InputStream in = getConfigXml( );
      // build the configuration
        AxisConfigBuilder builder = new AxisConfigBuilder(in, axisConfig, null);
        builder.populateConfig();
    } catch (Exception e) {
      e.printStackTrace();
      throw AxisFault.makeFault(e);
    }
    // set this object as the Axis configurator. Axis will call loadServices().
    axisConfig.setConfigurator(this);
    return axisConfig;
  }

  /*
   * These are the abstract methods
   */
  public abstract InputStream getConfigXml( );
  
  public abstract boolean setEnabled( String name, boolean enabled ) throws AxisFault;
  
  public abstract void init();

  /**
   * Adds any implementation-specific web service endpoints for a Axis service
   * @param axisService The Axis web service to add end points to
   */
  protected abstract void addServiceEndPoints( AxisService axisService ); 
  
  /**
   * Adds any implementation-specific transports for a Axis service
   * @param axisService The Axis web service to add end points to
   */
  protected abstract void addTransports( AxisService axisService );
  
  /**
   * Returns a list of the web service wrappers for this implmentation
   * @return
   */
  protected abstract List<IWebServiceWrapper> getWebServiceWrappers();

  /**
   * Load the web services from the list of web service wrappers
   */
  public void loadServices() {
    
    List<IWebServiceWrapper> wrappers = getWebServiceWrappers();
    
    for( IWebServiceWrapper wrapper : wrappers ) {
      try {
        loadService( wrapper );
      } catch (Exception e) {
        error( Messages.getErrorString( "BaseServiceSetup.ERROR_0001_COULD_NOT_LOAD_SERVICE", wrapper.getName() ), e ); //$NON-NLS-1$
      }
    }
      
  }

  /**
   * Loads a web service from a web service wrapper
   * @param wrapper Web service wrapper
   * @throws Exception
   */
  protected void loadService( IWebServiceWrapper wrapper) throws Exception {

    // first create the service
    String serviceName = wrapper.getName();
    AxisService axisService = createService( wrapper );

    // add any additional transports
    addTransports( axisService );
    
    // add any end points
    addServiceEndPoints( axisService );
    
    // create the WSDL for the service
    createServiceWsdl( axisService, wrapper );
    
    // add the wrapper to the service list
    services.put( serviceName, wrapper );
    
    // start the service
    axisConfig.addService(axisService);
    axisConfig.startService(axisService.getName());
    
    // enable or disable the service as the wrapper dictates
    axisService.setActive( wrapper.isEnabled() );

  }  
  
  /**
   * Create a web service from a web service wrapper. The concrete subclass
   * providers the wrappers via getWebServiceWrappers()
   * @param wrapper The wrapper
   * @return
   * @throws AxisFault
   */
  protected AxisService createService( IWebServiceWrapper wrapper ) throws AxisFault {
    
    Class serviceClass = wrapper.getServiceClass();
    String serviceName = wrapper.getName();
    String className = serviceClass.getName();
    
    if( axisConfig.getService( serviceName ) != null ) {
      axisConfig.removeService( serviceName );
    }
  
    AxisService axisService = AxisService.createService( className,  axisConfig );

    axisService.setName( serviceName );
    axisService.setDocumentation( wrapper.getDescription() );
    
    wrapper.setService( axisService );
    
    return axisService;
  }
  
  /**
   * Creates the WSDL for an Axis service
   * @param axisService
   * @param wrapper
   * @throws Exception
   */
  protected void createServiceWsdl( AxisService axisService, IWebServiceWrapper wrapper ) throws Exception {
    // specific that we are generating the WSDL
    Parameter useOriginalwsdl = new Parameter();
    useOriginalwsdl.setName("useOriginalwsdl"); //$NON-NLS-1$
    useOriginalwsdl.setValue("true"); //$NON-NLS-1$
    axisService.addParameter(useOriginalwsdl);
    
    // get the WSDL generation and make it a parameter
    Definition wsdlDefn = wrapper.getDefinition( axisConfig );
    Parameter wsdl = new Parameter();
    wsdl.setName( WSDLConstants.WSDL_4_J_DEFINITION );
    wsdl.setValue( wsdlDefn);
    
    // add the WSDL parameter to the service
    axisService.addParameter(wsdl);
  }

  /**
   * An AxisConfigurator method that we don't need
   */
  public void engageGlobalModules() throws AxisFault {
    
  }

  /**
   * An AxisConfigurator method that we don't need
   */
  public void cleanup() {
    
  }

  public void setSession(IPentahoSession session) {
    this.session = session;
  }

}
