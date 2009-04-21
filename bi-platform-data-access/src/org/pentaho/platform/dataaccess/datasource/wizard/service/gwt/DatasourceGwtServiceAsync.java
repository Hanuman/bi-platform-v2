package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DatasourceGwtServiceAsync {
  void getDatasources(AsyncCallback<List<IDatasource>> callback);
  void getDatasourceByName(String name, AsyncCallback<IDatasource> callback);
  void addDatasource(IDatasource datasource, AsyncCallback<Boolean> callback);
  void deleteDatasource(IDatasource datasource, AsyncCallback<Boolean> callback);
  void updateDatasource(IDatasource datasource, AsyncCallback<Boolean> callback);
  void deleteDatasource(String name, AsyncCallback<Boolean> callback);
  void doPreview(IConnection connection, String query, String previewLimit, AsyncCallback<ResultSetObject> callback);
  void doPreview(IDatasource datasource, AsyncCallback<ResultSetObject> callback);
  void getBusinessData(IConnection connection, String query, String previewLimit, AsyncCallback<BusinessData> callback);
  void getBusinessData(IDatasource datasource, AsyncCallback<BusinessData> callback);
  void createCategory(String categoryName, IConnection connection, String query, BusinessData businessData,AsyncCallback<Boolean> callback);
}

  