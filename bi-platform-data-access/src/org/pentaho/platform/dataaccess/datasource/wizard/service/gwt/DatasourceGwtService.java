
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
     
public interface DatasourceGwtService extends RemoteService{
  public List<IDatasource> getDatasources();
  public IDatasource getDatasourceByName(String name);
  public Boolean addDatasource(IDatasource datasource);
  public Boolean deleteDatasource(IDatasource datasource);
  public Boolean updateDatasource(IDatasource datasource);
  public Boolean deleteDatasource(String name);
  public SerializedResultSet doPreview(IConnection connection, String query, String previewLimit) throws DatasourceServiceException;
  public SerializedResultSet doPreview(IDatasource datasource) throws DatasourceServiceException;
  public BusinessData generateModel(String modelName, IConnection connection, String query, String previewLimit) throws DatasourceServiceException;
  public Boolean saveModel(String modelName, IConnection connection, String query, Boolean overwrite) throws DatasourceServiceException;  
  public Boolean saveModel(BusinessData businessData, Boolean overwrite)throws DatasourceServiceException;
  public BogoPojo gwtWorkaround(BogoPojo pojo);
}

  