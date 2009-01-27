package org.pentaho.webservices.test;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.repository.datasource.Datasource;

public class TestDatasourceMgmtService implements IDatasourceMgmtService {

  public void createDatasource(IDatasource newDataSource) throws DuplicateDatasourceException,
      DatasourceMgmtServiceException {
    // TODO Auto-generated method stub

  }

  public void deleteDatasource(String jndiName) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    // TODO Auto-generated method stub

  }

  public void deleteDatasource(IDatasource dataSource) throws NonExistingDatasourceException,
      DatasourceMgmtServiceException {
    // TODO Auto-generated method stub

  }

  public IDatasource getDatasource(String JndiName) throws DatasourceMgmtServiceException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<IDatasource> getDatasources() throws DatasourceMgmtServiceException {
    
    // return a hardcoded list
    List<IDatasource> sources = new ArrayList<IDatasource>();
    
    IDatasource datasource = new Datasource();
    datasource.setName( "testdatasource1" ); //$NON-NLS-1$
    sources.add( datasource );

    datasource = new Datasource();
    datasource.setName( "testdatasource2" ); //$NON-NLS-1$
    sources.add( datasource );
    
    return sources;
  }

  public void updateDatasource(IDatasource datasource) throws NonExistingDatasourceException,
      DatasourceMgmtServiceException {
    // TODO Auto-generated method stub

  }

  public void init(IPentahoSession session) {
    // TODO Auto-generated method stub

  }

}
