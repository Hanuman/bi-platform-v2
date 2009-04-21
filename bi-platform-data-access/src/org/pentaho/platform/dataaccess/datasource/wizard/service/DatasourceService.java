package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetObject;
import org.pentaho.ui.xul.XulServiceCallback;

public interface DatasourceService {
  void getDatasources(XulServiceCallback<List<IDatasource>> callback);
  void getDatasourceByName(String name, XulServiceCallback<IDatasource> callback);
  void addDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback);
  void deleteDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback);
  void updateDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback);
  void deleteDatasource(String name, XulServiceCallback<Boolean> callback);
  void doPreview(IConnection connection, String query, String previewLimit, XulServiceCallback<ResultSetObject> callback) throws DatasourceServiceException;
  void doPreview(IDatasource datasource, XulServiceCallback<ResultSetObject> callback) throws DatasourceServiceException;
  void getBusinessData(IDatasource datasource, XulServiceCallback<BusinessData> callback) throws DatasourceServiceException;
  void getBusinessData(IConnection connection, String query, String previewLimit, XulServiceCallback<BusinessData> callback) throws DatasourceServiceException;
  void createCategory(String categoryName, IConnection connection, String query, BusinessData data,XulServiceCallback<Boolean> callback) throws DatasourceServiceException ;
}

  