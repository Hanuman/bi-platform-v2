package org.pentaho.platform.webservice.content;

import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.PentahoAxis;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.plugin.services.webservices.SessionHandler;
import org.pentaho.platform.plugin.services.webservices.content.RunService;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;

public class RunJsonService extends RunService {

  private static final long serialVersionUID = 2486856447489385553L;

  @Override
  public void createServiceContent( AxisService axisService, String operationName, AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out ) throws Exception {

    IParameterProvider pathParams = parameterProviders.get( "path" ); //$NON-NLS-1$
    
    // get the HTTP objects from the 'path' parameter provider
    HttpServletRequest request = (HttpServletRequest) pathParams.getParameter("httprequest"); //$NON-NLS-1$
    HttpServletResponse response = (HttpServletResponse) pathParams.getParameter("httpresponse"); //$NON-NLS-1$
    ServletConfig servletConfig  = (ServletConfig) pathParams.getParameter("servletconfig"); //$NON-NLS-1$
    
    // create a service group and group context for this service
    AxisServiceGroup axisServiceGroup = new AxisServiceGroup( context.getAxisConfiguration() );
    axisServiceGroup.addService( axisService );
    ServiceGroupContext serviceGroupContext = new ServiceGroupContext( context, axisServiceGroup );
    // create a service context
    ServiceContext serviceContext = serviceGroupContext.getServiceContext( axisService );
    // get an operation by name, if possible
    AxisOperation axisOperation = axisService.getOperationByAction( operationName );
    OperationContext operationContext = serviceContext.createOperationContext( axisOperation );
    
    // create an object to hook into Axis and give it everything we have
    PentahoAxis hooks = new PentahoAxis();
    hooks.setContext(context);
    servletConfig.getServletContext().setAttribute(PentahoAxis.CONFIGURATION_CONTEXT, context);
    hooks.setServletConfig(servletConfig);
    hooks.setConfiguration( axisConfiguration );
    hooks.initContextRoot( request );
    hooks.setAxisService(axisService);
    hooks.setAxisOperation(axisOperation);
    hooks.setOperationContext(operationContext);
    hooks.setServiceContext( serviceContext );
    hooks.setAxisOperation(axisOperation);
    hooks.setOperationContext(operationContext);
    String pathInfo = request.getPathInfo();
    if( pathInfo.startsWith( "/ws-json1" ) ) { //$NON-NLS-1$
      hooks.setMessageType( "application/json" ); //$NON-NLS-1$
    } 
    else if( pathInfo.startsWith( "/ws-json2" ) ) { //$NON-NLS-1$
      hooks.setMessageType( "text/javascript" ); //$NON-NLS-1$
    }
    // now execute the operation
    if( request != null && response != null ) {
      try {
        SessionHandler.setSession( userSession );
        String method = request.getMethod();
        if( "GET".equalsIgnoreCase( method ) ) { //$NON-NLS-1$
          hooks.handleGet( method, request, response);
        }
        else if( "POST".equalsIgnoreCase( request.getMethod() ) ) { //$NON-NLS-1$
          hooks.handlePost(method, request, response);
        } else if( "PUT".equalsIgnoreCase( request.getMethod() ) ) { //$NON-NLS-1$
          hooks.handlePut(method, request, response);
        }
      } catch (Exception e) {
        processAxisFault(hooks.getMessageContext(), out, e);
        error( Messages.getErrorString( "RunService.ERROR_0001_ERROR_DURING_EXECUTION" ), e ); //$NON-NLS-1$
      }
    }
    
  }
  
}
