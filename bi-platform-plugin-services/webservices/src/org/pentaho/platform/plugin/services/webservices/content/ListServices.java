package org.pentaho.platform.plugin.services.webservices.content;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.webservices.IWebServiceConfigurator;
import org.pentaho.platform.plugin.services.webservices.IWebServiceWrapper;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * A content generator for listing web services and providing links to their WSDL URLs
 * @author jamesdixon
 *
 */
public class ListServices  extends WebServiceContentGenerator {

  private static final long serialVersionUID = -1772210710764038165L;

  @Override
  public void createContent( AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out ) throws Exception {
    
    HashMap serviceMap = axisConfiguration.getServices();
    
    StringBuilder sb = new StringBuilder();
    
    getPageTitle( serviceMap, sb );

        Collection servicecol = serviceMap.values();
        // list each web service
        for (Iterator iterator = servicecol.iterator(); iterator.hasNext();) {
          AxisService axisService = (AxisService) iterator.next();

            getTitleSection( axisService, axisConfiguration, sb );
            
            getWsdlSection( axisService, sb );

            getRunSection( axisService, sb );

            getOperationsSection( axisService, sb );
            
        }
        
        getPageFooter( serviceMap, sb );

    out.write( sb.toString().getBytes(LocaleHelper.getSystemEncoding()) );
  }

  /**
   * Writes the HTML page title area
   * @param serviceMap Map of current web services
   * @param sb StringBuilder to write content to
   */
  protected void getPageTitle( HashMap serviceMap, StringBuilder sb ) {
    // write out the page title
    sb.append( "<div id=\"webservicediv\">" ); //$NON-NLS-1$
    sb.append( "<h1>" ).append( Messages.getString("ListServices.USER_WEB_SERVICES") ).append( "</h1>\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    if (serviceMap.isEmpty()) {
      // there are no services defined
      sb.append( Messages.getString("ListServices.USER_NO_SERVICES") ); //$NON-NLS-1$
    }
  }

  /**
   * Writes the title section for a service to the HTML page
   * @param axisService the Axis service
   * @param axisConfiguration the Axis configuration
   * @param sb StringBuilder to write content to
   */
  protected void getTitleSection( AxisService axisService, AxisConfiguration axisConfiguration, StringBuilder sb ) {

    IWebServiceConfigurator getAxisConfigurator = (IWebServiceConfigurator) axisConfiguration.getConfigurator();
    
    String serviceName = axisService.getName();
    // get the wrapper for the web service so we can get the localized title and description
    IWebServiceWrapper wrapper = getAxisConfigurator.getServiceWrapper( serviceName );

    sb.append( "<table>\n<tr>\n<td colspan=\"2\"><h2>" ).append( wrapper.getTitle() ).append( "</h2></td></tr>\n<tr><td>" ); //$NON-NLS-1$ //$NON-NLS-2$

    String serviceDescription = axisService.getDocumentation();
    if (serviceDescription == null || "".equals(serviceDescription)) { //$NON-NLS-1$
        serviceDescription = Messages.getString( "WebServicePlugin.USER_NO_DESCRIPTION" ); //$NON-NLS-1$
    }
    
    // write out the description
    sb.append( Messages.getString( "WebServicePlugin.USER_SERVICE_DESCRIPTION" ) ) //$NON-NLS-1$
    .append( "</td><td>" ) //$NON-NLS-1$
    .append( serviceDescription )
      .append( "</td></tr>\n" ); //$NON-NLS-1$
      
    // write out the enable/disable controls
    sb.append( "<tr><td>" ).append( Messages.getString( "WebServicePlugin.USER_SERVICE_STATUS" ) ) //$NON-NLS-1$ //$NON-NLS-2$
    
    .append( "</td><td>" ); //$NON-NLS-1$
    if( axisService.isActive() ) {
      sb.append( Messages.getString( "WebServicePlugin.USER_ENABLED" ) ); //$NON-NLS-1$
    } else {
      sb.append( Messages.getString( "WebServicePlugin.USER_DISABLED" ) ); //$NON-NLS-1$
    }  }
  
  /**
   * Writes the WSDL section for a service to the HTML page
   * @param axisService the Axis service
   * @param sb StringBuilder to write content to
   */
  protected void getWsdlSection( AxisService axisService, StringBuilder sb ) {
    // write out the WSDL URL
    String wsdlUrl = WebServiceConst.getWsdlUrl()+"/"; //$NON-NLS-1$
    sb.append( "<tr><td>" ).append( Messages.getString( "WebServicePlugin.USER_SERVICE_WSDL" ) ) //$NON-NLS-1$ //$NON-NLS-2$
    .append( "</td><td><a href=\"" ).append( wsdlUrl+axisService.getName() ) //$NON-NLS-1$
    .append( "\">" ).append( wsdlUrl+axisService.getName() ) //$NON-NLS-1$
    .append( "</a></td></tr>\n" ); //$NON-NLS-1$
  }
  
  /**
   * Writes the execute URL section for a service to the HTML page
   * @param axisService the Axis service
   * @param sb StringBuilder to write content to
   */
  protected void getRunSection( AxisService axisService, StringBuilder sb ) {
    // write out the execution URL
    String serviceUrl = WebServiceConst.getExecuteUrl()+"/"; //$NON-NLS-1$
    sb.append( "<tr><td>" ).append( Messages.getString( "WebServicePlugin.USER_SERVICE_URL" ) ) //$NON-NLS-1$ //$NON-NLS-2$
    .append( "</td><td><a href=\"" ).append( serviceUrl+axisService.getName() ) //$NON-NLS-1$
    .append( "\">" ).append( serviceUrl+axisService.getName() ) //$NON-NLS-1$
    .append( "</a></td></tr>\n" ); //$NON-NLS-1$

  }

  /**
   * Writes the list of operations for a service to the HTML page
   * @param axisService the Axis service
   * @param sb StringBuilder to write content to
   */
  protected void getOperationsSection( AxisService axisService, StringBuilder sb ) {
    
    // write out the operations
    Iterator it = axisService.getOperations();

    sb.append( "<tr><td valign=\"top\">" ) //$NON-NLS-1$
    .append( Messages.getString( "WebServicePlugin.USER_OPERATIONS" ) ) //$NON-NLS-1$
    .append( "</td><td>" ); //$NON-NLS-1$
    // now do the operations
    if( !it.hasNext() ) {
      sb.append( Messages.getString( "WebServicePlugin.USER_NO_OPERATIONS" ) ); //$NON-NLS-1$
    } else {

      // write out the names of the operations
      // TODO localize these?
      while ( it.hasNext() ) {
        AxisOperation axisOperation = (AxisOperation) it.next();
        sb.append( axisOperation.getName().getLocalPart() );
        if( it.hasNext() ) {
          sb.append( "<br/>" ); //$NON-NLS-1$
        }
      }
      sb.append( "</td></tr>\n</table>\n" ); //$NON-NLS-1$
    }
  }
  
  /**
   * Writes the HTML page footer
   * @param serviceMap Map of current web services
   * @param sb StringBuilder to write content to
   */
  protected void getPageFooter( HashMap serviceMap, StringBuilder sb ) {
    // write out the page footer
    sb.append( "</div" ); //$NON-NLS-1$
  }

  
  @Override
  public String getMimeType() {
    return "text/html"; //$NON-NLS-1$
  }
  
  @Override
  public Log getLogger() {
    return LogFactory.getLog(ListServices.class);
  }

}
