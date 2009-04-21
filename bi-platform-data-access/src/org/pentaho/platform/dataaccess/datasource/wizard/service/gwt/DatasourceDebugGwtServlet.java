package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetObject;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceDelegate;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DatasourceDebugGwtServlet extends RemoteServiceServlet implements DatasourceGwtService {

  DatasourceServiceDelegate SERVICE;

  public DatasourceDebugGwtServlet() {
    SERVICE = new DatasourceServiceDelegate();
  }

  public List<IDatasource> getDatasources() {
    return SERVICE.getDatasources();
  }
  public IDatasource getDatasourceByName(String name) {
    return SERVICE.getDatasourceByName(name);
  }
  public Boolean addDatasource(IDatasource datasource) {
    return SERVICE.addDatasource(datasource);
  }

  public Boolean updateDatasource(IDatasource datasource) {
    return SERVICE.updateDatasource(datasource);
  }

  public Boolean deleteDatasource(IDatasource datasource) {
    return SERVICE.deleteDatasource(datasource);
  }
    
  public Boolean deleteDatasource(String name) {
    return SERVICE.deleteDatasource(name);    
  }

  public ResultSetObject doPreview(IConnection connection, String query, String previewLimit) throws DatasourceServiceException{
    return SERVICE.doPreview(connection, query, previewLimit);
  }

  public ResultSetObject doPreview(IDatasource datasource) throws DatasourceServiceException{
    return SERVICE.doPreview(datasource);
  }

  public Boolean createCategory(String categoryName, IConnection connection, String query, BusinessData businessData) throws DatasourceServiceException {
    return SERVICE.createCategory(categoryName,connection, query, businessData);  }

  public BusinessData getBusinessData(IConnection connection, String query, String previewLimit)   throws DatasourceServiceException {
    return SERVICE.getBusinessData(connection, query, previewLimit);
  }

  public BusinessData getBusinessData(IDatasource datasource) throws DatasourceServiceException {
    return SERVICE.getBusinessData(datasource);    
  }

  

}