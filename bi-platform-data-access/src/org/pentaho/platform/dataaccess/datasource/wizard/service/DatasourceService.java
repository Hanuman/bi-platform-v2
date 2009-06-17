package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.ui.xul.XulServiceCallback;

public interface DatasourceService {
  void getDatasources(XulServiceCallback<List<IDatasource>> callback);
  void getDatasourceByName(String name, XulServiceCallback<IDatasource> callback);
  void addDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback);
  void deleteDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback);
  void updateDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback);
  void deleteDatasource(String name, XulServiceCallback<Boolean> callback);
  void deleteModel(String domainId, String modelName, XulServiceCallback<Boolean> callback) throws DatasourceServiceException;
  void doPreview(IConnection connection, String query, String previewLimit, XulServiceCallback<SerializedResultSet> callback) throws DatasourceServiceException;
  void doPreview(IDatasource datasource, XulServiceCallback<SerializedResultSet> callback) throws DatasourceServiceException;
  void generateModel(String modelName, IConnection connection, String query, String previewLimit, XulServiceCallback<BusinessData> callback) throws DatasourceServiceException;
  void saveModel(String modelName, IConnection connection, String query, Boolean overwrite, String previewLimit, XulServiceCallback<BusinessData> callback) throws DatasourceServiceException;  
  void saveModel(BusinessData businessData, Boolean overwrite, XulServiceCallback<Boolean> callback) throws DatasourceServiceException ;
  void generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent, String delimeter, String enclosure, XulServiceCallback<BusinessData> callback) throws DatasourceServiceException ;
  void saveInlineEtlModel(Domain modelName, Boolean overwrite,XulServiceCallback<Boolean> callback) throws DatasourceServiceException ;
  void getUploadFilePath(XulServiceCallback<String> callback) throws DatasourceServiceException ; 
  void isAdministrator(XulServiceCallback<Boolean> callback);
}

  