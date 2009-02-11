package org.pentaho.plugin.services.webservices.content;

import java.io.OutputStream;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.plugin.services.webservices.messages.Messages;


/**
 * Base class for content generators that operate on a specific web service, 
 * e.g. ServiceWsdl or RunService
 * @author jamesdixon
 *
 */
public abstract class ServiceContentGenerator extends WebServiceContentGenerator {
  
  /**
   * Parses the path parameter to find the web service name, makes sure it is 
   * valid, and the calls the current subclass to create the required content
   * for the specified web service
   */
  @Override
  public void createContent( AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out ) throws Exception {

    // make sure we have a 'path' parameters provider
    IParameterProvider pathParams = parameterProviders.get( "path" ); //$NON-NLS-1$
    if( pathParams == null ) {
      // return an error
      String message = Messages.getErrorString("WebServiceContentGenerator.ERROR_0004_PATH_PARAMS_IS_MISSING"); //$NON-NLS-1$
      getLogger().error( message );
      out.write( message.getBytes() );
      return;
    }
    
    // make sure we have a service name on the URL
    String serviceName = pathParams.getStringParameter( "path", null); //$NON-NLS-1$
    if( serviceName == null ) {
      // return an error
      String message = Messages.getErrorString("WebServiceContentGenerator.ERROR_0005_SERVICE_NAME_IS_MISSING"); //$NON-NLS-1$
      getLogger().error( message );
      out.write( message.getBytes() );
      return;
    }

    // remove the leading '/'
    serviceName = serviceName.substring( 1 );

    // pull the service name off the URL
    String query = serviceName;
    String operationName = null;
    int idx = serviceName.indexOf( "/" ); //$NON-NLS-1$
    if( idx != -1 ) {
      serviceName = serviceName.substring( 0, idx );
      query = query.substring( idx + 1 );
      idx = query.indexOf( "?" ); //$NON-NLS-1$
      if( idx != -1 ) {
        operationName = query.substring( 0, idx );
      } else {
        operationName = query;
      }
    }

    // try to get the service using the name
    AxisService axisService = axisConfiguration.getService( serviceName );
    if( axisService == null ) {
      // return an error
      String message = Messages.getErrorString("WebServiceContentGenerator.ERROR_0006_SERVICE_IS_INVALID", serviceName ); //$NON-NLS-1$
      getLogger().error( message );
      out.write( message.getBytes() );
      return;
    }
    
    // hand over to the subclass
    createServiceContent( axisService, operationName, axisConfiguration, context, out );
    
  }
  
  /**
   * Processes the current request for the provided Axis service
   * @param axisService The Axis web service
   * @param operationName The name of the operation to perform, if known
   * @param axisConfiguration The current configuration
   * @param context The current context
   * @param out The output stream for content to be written to
   * @throws Exception
   */
  protected abstract void createServiceContent( AxisService axisService, String operationName, AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out ) throws Exception;
  
}
