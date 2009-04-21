
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetObject;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
     
public interface DatasourceGwtService extends RemoteService{
  public List<IDatasource> getDatasources();
  public IDatasource getDatasourceByName(String name);
  public Boolean addDatasource(IDatasource datasource);
  public Boolean deleteDatasource(IDatasource datasource);
  public Boolean updateDatasource(IDatasource datasource);
  public Boolean deleteDatasource(String name);
  public ResultSetObject doPreview(IConnection connection, String query, String previewLimit) throws DatasourceServiceException;
  public ResultSetObject doPreview(IDatasource datasource) throws DatasourceServiceException;
  public BusinessData getBusinessData(IConnection connection, String query, String previewLimit)throws DatasourceServiceException;
  public BusinessData getBusinessData(IDatasource datasource)throws DatasourceServiceException;
  public Boolean createCategory(String categoryName, IConnection connection, String query, BusinessData businessData)throws DatasourceServiceException;
}

  