package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.DatasourceGwtServiceAsync;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;

public class DatasourceServiceGwtImpl implements DatasourceService {
  final static String ERROR = "ERROR:";
  static DatasourceGwtServiceAsync SERVICE;

  static {

    SERVICE = (org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.DatasourceGwtServiceAsync) GWT
        .create(org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.DatasourceGwtService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    endpoint.setServiceEntryPoint(getBaseUrl());

  }
  
  /** 
   * Returns the context-aware URL to the rpc service
   */
  private static String getBaseUrl() {
    String moduleUrl = GWT.getModuleBaseURL();
    
    //
    //Set the base url appropriately based on the context in which we are running this client
    //
    if(moduleUrl.indexOf("content") > -1) {
      //we are running the client in the context of a BI Server plugin, so 
      //point the request to the GWT rpc proxy servlet
      String baseUrl = moduleUrl.substring(0, moduleUrl.indexOf("content"));
      //NOTE: the dispatch URL ("connectionService") must match the bean id for 
      //this service object in your plugin.xml.  "gwtrpc" is the servlet 
      //that handles plugin gwt rpc requests in the BI Server.
      return  baseUrl + "gwtrpc/datasourceService";
    }
    //we are running this client in hosted mode, so point to the servlet 
    //defined in war/WEB-INF/web.xml
    return moduleUrl + "DatasourceService";
  }

  public DatasourceServiceGwtImpl() {

  }

  public void getDatasources(final XulServiceCallback<List<IDatasource>> callback) {
    SERVICE.getDatasources(new AsyncCallback<List<IDatasource>>() {

      public void onFailure(Throwable arg0) {
        callback.error("error getting connections: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(List<IDatasource> arg0) {
        callback.success(arg0);
      }

    });
  }

  public void getDatasourceByName(String name, final XulServiceCallback<IDatasource> callback) {
    SERVICE.getDatasourceByName(name, new AsyncCallback<IDatasource>() {

      public void onFailure(Throwable arg0) {
        callback.error("error getting connections: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(IDatasource arg0) {
        callback.success(arg0);
      }

    });
  }

  public void addDatasource(IDatasource datasource, final XulServiceCallback<Boolean> callback) {
    SERVICE.addDatasource(datasource, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error adding connection: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void updateDatasource(IDatasource datasource, final XulServiceCallback<Boolean> callback) {
    SERVICE.updateDatasource(datasource, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error updating connection: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void deleteDatasource(IDatasource datasource, final XulServiceCallback<Boolean> callback) {
    SERVICE.deleteDatasource(datasource, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error deleting connection: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void deleteDatasource(String name, final XulServiceCallback<Boolean> callback) {
    SERVICE.deleteDatasource(name, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error deleting connection: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void doPreview(IConnection connection, String query, String previewLimit,
      final XulServiceCallback<SerializedResultSet> callback) throws DatasourceServiceException {
    SERVICE.doPreview(connection, query, previewLimit, new AsyncCallback<SerializedResultSet>() {

      public void onFailure(Throwable arg0) {
        callback.error("error doing preview: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(SerializedResultSet arg0) {
        callback.success(arg0);
      }

    });

  }

  public void doPreview(IDatasource datasource, final XulServiceCallback<SerializedResultSet> callback)
      throws DatasourceServiceException {
    SERVICE.doPreview(datasource, new AsyncCallback<SerializedResultSet>() {

      public void onFailure(Throwable arg0) {
        callback.error("error doing preview: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(SerializedResultSet arg0) {
        callback.success(arg0);
      }

    });

  }

  public void generateModel(String modelName, IConnection connection, String query, String previewLimit,
      final XulServiceCallback<BusinessData> callback) throws DatasourceServiceException {
    SERVICE.generateModel(modelName, connection, query, previewLimit, new AsyncCallback<BusinessData>() {

      public void onFailure(Throwable arg0) {
        callback.error("error generating the mode: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(BusinessData arg0) {
        callback.success(arg0);
      }

    });
  }

  public void saveModel(String modelName, IConnection connection, String query, Boolean overwrite, String previewLimit,
      final XulServiceCallback<BusinessData> callback) throws DatasourceServiceException {
    SERVICE.saveModel(modelName, connection, query, overwrite, previewLimit, new AsyncCallback<BusinessData>() {

      public void onFailure(Throwable arg0) {
        callback.error("error saving the mode: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(BusinessData arg0) {
        callback.success(arg0);
      }

    });
  }

  public void saveModel(BusinessData businessData, Boolean overwrite, final XulServiceCallback<Boolean> callback)
      throws DatasourceServiceException {
    SERVICE.saveModel(businessData, overwrite, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error saving the mode: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent,
      String delimeter, String enclosure, final XulServiceCallback<BusinessData> callback) throws DatasourceServiceException {
    SERVICE.generateInlineEtlModel(modelName, relativeFilePath, headersPresent, delimeter, enclosure, new AsyncCallback<BusinessData>() {

      public void onFailure(Throwable arg0) {
        callback.error("error generating the inline etl model: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(BusinessData arg0) {
        callback.success(arg0);
      }

    });
    
  }

  public void saveInlineEtlModel(Domain modelName, Boolean overwrite, final XulServiceCallback<Boolean> callback)
      throws DatasourceServiceException {
    SERVICE.saveInlineEtlModel(modelName, overwrite, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error generating the inline etl model: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void isAdministrator(final XulServiceCallback<Boolean> callback) {
    SERVICE.isAdministrator(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error checking if the user is the administrator: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void getUploadFilePath(final XulServiceCallback<String> callback) throws DatasourceServiceException {
    SERVICE.getUploadFilePath(new AsyncCallback<String>() {

      public void onFailure(Throwable arg0) {
        callback.error("error getting the upload file path: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(String arg0) {
        callback.success(arg0);
      }

    });
  }
}
