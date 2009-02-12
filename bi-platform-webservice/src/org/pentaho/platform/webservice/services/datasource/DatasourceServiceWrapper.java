package org.pentaho.platform.webservice.services.datasource;

import java.util.ArrayList;

import org.pentaho.platform.plugin.services.webservices.BaseWebServiceWrapper;
import org.pentaho.platform.webservice.plugin.messages.Messages;

/**
 * A wrapper for the datasource web service. Defines the datasource bean, the
 * additional classes needed in the WSDL (WSDataSource) and provides the
 * localized title and description of the web service
 * @author jamesdixon
 *
 */
public class DatasourceServiceWrapper extends BaseWebServiceWrapper {

  public Class getServiceClass() {
    return DatasourceService.class;
  }

  protected ArrayList<Class> getExtraClasses() {
    ArrayList<Class> extraClasses = new ArrayList<Class>();
    extraClasses.add( WSDataSource.class );
    return extraClasses;
  }
    
  public String getTitle() {
    return Messages.getString( "DatasourceServiceWrapper.USER_DATASOURCE_SERVICE_TITLE" ); //$NON-NLS-1$
  }
    
  public String getDescription() {
    return Messages.getString( "DatasourceServiceWrapper.USER_DATASOURCE_SERVICE_DESC" ); //$NON-NLS-1$
  }
}
