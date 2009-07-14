
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
     
public interface DatasourceGwtService extends RemoteService{
  public List<LogicalModelSummary> getLogicalModels() throws DatasourceServiceException;
  public List<IDatasource> getDatasources();
  public IDatasource getDatasourceByName(String name);
  public Boolean addDatasource(IDatasource datasource);
  public Boolean deleteDatasource(IDatasource datasource);
  public Boolean updateDatasource(IDatasource datasource);
  public Boolean deleteModel(String domainId, String modelName) throws DatasourceServiceException;
  public SerializedResultSet doPreview(IConnection connection, String query, String previewLimit) throws DatasourceServiceException;
  public BusinessData generateModel(String modelName, IConnection connection, String query, String previewLimit) throws DatasourceServiceException;
  public BusinessData generateAndSaveModel(String modelName, IConnection connection, String query, Boolean overwrite, String previewLimit) throws DatasourceServiceException;  
  public Boolean saveModel(BusinessData businessData, Boolean overwrite)throws DatasourceServiceException;
  public BusinessData generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent, String delimeter, String enclosure) throws DatasourceServiceException;
  public Boolean saveInlineEtlModel(Domain modelName, Boolean overwrite) throws DatasourceServiceException ;
  public String getUploadFilePath() throws DatasourceServiceException ;
  public Boolean hasPermission();
  public BogoPojo gwtWorkaround(BogoPojo pojo);
  
  public BusinessData loadBusinessData(String domainId, String modelId) throws DatasourceServiceException;
}

  