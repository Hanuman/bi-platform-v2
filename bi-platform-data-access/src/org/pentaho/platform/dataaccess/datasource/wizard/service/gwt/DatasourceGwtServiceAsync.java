package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DatasourceGwtServiceAsync {
  void getDatasources(AsyncCallback<List<IDatasource>> callback);
  void getDatasourceByName(String name, AsyncCallback<IDatasource> callback);
  void addDatasource(IDatasource datasource, AsyncCallback<Boolean> callback);
  void deleteDatasource(IDatasource datasource, AsyncCallback<Boolean> callback);
  void updateDatasource(IDatasource datasource, AsyncCallback<Boolean> callback);
  void deleteDatasource(String name, AsyncCallback<Boolean> callback);
  void doPreview(IConnection connection, String query, String previewLimit, AsyncCallback<SerializedResultSet> callback);
  void doPreview(IDatasource datasource, AsyncCallback<SerializedResultSet> callback);
  void generateModel(String modelName, IConnection connection, String query, String previewLimit, AsyncCallback<BusinessData> callback) throws DatasourceServiceException;
  void saveModel(String modelName, IConnection connection, String query, Boolean overwrite, String previewLimit, AsyncCallback<BusinessData> callback) throws DatasourceServiceException;
  void saveModel(BusinessData businessData, Boolean overwrite, AsyncCallback<Boolean> callback) throws DatasourceServiceException ;
  void generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent, String delimeter, String enclosure, AsyncCallback<Domain> callback) throws DatasourceServiceException ;
  void saveInlineEtlModel(Domain modelName, Boolean overwrite,AsyncCallback<Boolean> callback) throws DatasourceServiceException ;    
  void gwtWorkaround (BogoPojo pojo, AsyncCallback<BogoPojo> callback);

}

  