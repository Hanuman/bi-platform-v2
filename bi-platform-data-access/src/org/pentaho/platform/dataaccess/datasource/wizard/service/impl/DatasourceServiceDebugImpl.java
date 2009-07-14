package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;

public class DatasourceServiceDebugImpl implements DatasourceService{

  DatasourceServiceInMemoryDelegate SERVICE;
  public DatasourceServiceDebugImpl(){
    SERVICE = new DatasourceServiceInMemoryDelegate();
  }
 
  public void getDatasources(XulServiceCallback<List<IDatasource>> callback) {
    callback.success(SERVICE.getDatasources());
  }
  public void getDatasourceByName(String name, XulServiceCallback<IDatasource> callback) {
    callback.success(SERVICE.getDatasourceByName(name));
  }
  public void addDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.addDatasource(datasource));
  }
  
  public void updateDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.updateDatasource(datasource));
  }
  public void deleteDatasource(IDatasource datasource, XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.deleteDatasource(datasource));
  }
  public void doPreview(IConnection connection, String query, String previewLimit, XulServiceCallback<SerializedResultSet> callback)
      throws DatasourceServiceException {
    callback.success(SERVICE.doPreview(connection, query, previewLimit));
  }
  public void generateModel(String modelName, IConnection connection, String query, String previewLimit,
      XulServiceCallback<BusinessData> callback) throws DatasourceServiceException {
   callback.success(SERVICE.generateModel(modelName, connection, query, previewLimit));
  }
  public void generateAndSaveModel(String modelName, IConnection connection, String query, Boolean overwrite, String previewLimit,
      XulServiceCallback<BusinessData> callback) throws DatasourceServiceException {
   callback.success(SERVICE.generateAndSaveModel(modelName, connection, query, overwrite, previewLimit));
  }

  public void saveModel(BusinessData businessData, Boolean overwrite, XulServiceCallback<Boolean> callback)
      throws DatasourceServiceException {
    callback.success(SERVICE.saveModel(businessData, overwrite));
  }

  public void generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent,
      String delimeter, String enclosure, XulServiceCallback<BusinessData> callback) throws DatasourceServiceException {
    callback.success(SERVICE.generateInlineEtlModel(modelName, relativeFilePath, headersPresent, delimeter, enclosure));
  }

  public void saveInlineEtlModel(Domain modelName, Boolean overwrite, XulServiceCallback<Boolean> callback)
      throws DatasourceServiceException {
    callback.success(SERVICE.saveInlineEtlModel(modelName, overwrite));
  }
  public void hasPermission(XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.hasPermission());
  }

  public void getUploadFilePath(XulServiceCallback<String> callback) throws DatasourceServiceException {
    callback.success(SERVICE.getUploadFilePath());
  }

  public void deleteModel(String domainId, String modelName, XulServiceCallback<Boolean> callback) {
    try {
      Boolean res = SERVICE.deleteModel(domainId, modelName);
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void getLogicalModels(XulServiceCallback<List<LogicalModelSummary>> callback) {
    try {
      List<LogicalModelSummary> res = SERVICE.getLogicalModels();
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void loadBusinessData(String domainId, String modelId, XulServiceCallback<BusinessData> callback) {
    try {
      BusinessData res = SERVICE.loadBusinessData(domainId, modelId);
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }
}

  