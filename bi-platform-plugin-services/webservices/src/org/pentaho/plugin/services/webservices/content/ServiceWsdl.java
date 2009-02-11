package org.pentaho.plugin.services.webservices.content;

import java.io.OutputStream;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Writes the WSDL for a Axis web service to the output stream provided
 * @author jamesdixon
 *
 */
public class ServiceWsdl extends ServiceContentGenerator {
  
  private static final long serialVersionUID = -163750511475038584L;

  /**
   * Writes the WSDL to the output stream provided. The WSDL is prepared ahead of time 
   * when the web service is created.
   */
  @Override
  public void createServiceContent( AxisService axisService, String operationName, AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out ) throws Exception {

    axisService.printWSDL(out, WebServiceConst.getExecuteUrl() );
    
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(ServiceWsdl.class);
  }

  @Override
  public String getMimeType() {
    return "text/xml"; //$NON-NLS-1$
  }
  
}
