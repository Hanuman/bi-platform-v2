package org.pentaho.platform.webservice.services.datasource;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.webservice.plugin.messages.Messages;

/**
 * Datasource webservice bean. Provides the ability to list current datasources.
 * @author jamesdixon
 *
 */
public class DatasourceService {

  public DatasourceService() {
    
  }
  
  /**
   * Returns a list of the data sources defined currently in the BI server
   * @return List of data sources
   */
  
  public WSDataSource[] getDataSources() {
    // get the datasource management service
    IDatasourceMgmtService datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class,null);
    try {
      // get the list of data sources (IDatasource) from the management service
      List<IDatasource> sources = datasourceMgmtSvc.getDatasources();
      // create a list of WSDataSource objects to return
      List<WSDataSource> wsSources = new ArrayList<WSDataSource>();
      for( IDatasource source : sources ) {
        WSDataSource wsDataSource = new WSDataSource();
        wsDataSource.setName( source.getName() );
        wsSources.add( wsDataSource );
      }
      // return the list to the caller
      return wsSources.toArray( new WSDataSource[wsSources.size()]);
    } catch (DatasourceMgmtServiceException e) {
      Logger.error( DatasourceService.class.getName(), Messages.getErrorString( "DatasourceService.ERROR_0001_FAILED_TO_LIST" ), e ); //$NON-NLS-1$
    }
    return null;
  }
  
}
