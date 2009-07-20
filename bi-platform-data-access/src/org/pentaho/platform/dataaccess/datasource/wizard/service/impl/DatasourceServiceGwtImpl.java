package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtDatasourceServiceAsync;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class DatasourceServiceGwtImpl implements IXulAsyncDatasourceService {
  final static String ERROR = "ERROR:";
  static IGwtDatasourceServiceAsync SERVICE;

  static {

    SERVICE = (org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtDatasourceServiceAsync) GWT
        .create(org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtDatasourceService.class);
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
  public void doPreview(String connectionName, String query, String previewLimit,
      final XulServiceCallback<SerializedResultSet> callback) {
    SERVICE.doPreview(connectionName, query, previewLimit, new AsyncCallback<SerializedResultSet>() {

      public void onFailure(Throwable arg0) {
        callback.error("error doing preview: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(SerializedResultSet arg0) {
        callback.success(arg0);
      }

    });

  }

  public void generateLogicalModel(String modelName, String connectionName, String query, String previewLimit,
      final XulServiceCallback<BusinessData> callback) {
    SERVICE.generateLogicalModel(modelName, connectionName, query, previewLimit, new AsyncCallback<BusinessData>() {

      public void onFailure(Throwable arg0) {
        callback.error("error generating the mode: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(BusinessData arg0) {
        callback.success(arg0);
      }

    });
  }

  public void generateAndSaveLogicalModel(String modelName, String connectionName, String query, boolean overwrite, String previewLimit,
      final XulServiceCallback<BusinessData> callback) {
    SERVICE.generateAndSaveLogicalModel(modelName, connectionName, query, overwrite, previewLimit, new AsyncCallback<BusinessData>() {

      public void onFailure(Throwable arg0) {
        callback.error("error saving the mode: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(BusinessData arg0) {
        callback.success(arg0);
      }

    });
  }

  public void saveLogicalModel(Domain domain, boolean overwrite, final XulServiceCallback<Boolean> callback) {
    SERVICE.saveLogicalModel(domain, overwrite, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error saving the mode: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent,
      String delimeter, String enclosure, final XulServiceCallback<BusinessData> callback) {
    SERVICE.generateInlineEtlLogicalModel(modelName, relativeFilePath, headersPresent, delimeter, enclosure, new AsyncCallback<BusinessData>() {

      public void onFailure(Throwable arg0) {
        callback.error("error generating the inline etl model: ", arg0);//$NON-NLS-1$
      }

      public void onSuccess(BusinessData arg0) {
        callback.success(arg0);
      }

    });
    
  }


  public void hasPermission(final XulServiceCallback<Boolean> callback) {
    SERVICE.hasPermission(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error checking if the user is the administrator: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void deleteLogicalModel(String domainId, String modelName, final XulServiceCallback<Boolean> callback) {
    SERVICE.deleteLogicalModel(domainId, modelName, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error("error deleting the model: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    });
  }

  public void getLogicalModels(final XulServiceCallback<List<LogicalModelSummary>> callback) {
    SERVICE.getLogicalModels(new AsyncCallback<List<LogicalModelSummary>>() {

      public void onFailure(Throwable arg0) {
        callback.error("error getting logical models: ", arg0); //$NON-NLS-1$
      }

      public void onSuccess(List<LogicalModelSummary> arg0) {
        callback.success(arg0);
      }

    });
  }

  public void loadBusinessData(String domainId, String modelId, final XulServiceCallback<BusinessData> callback) {
    SERVICE.loadBusinessData(domainId, modelId, new AsyncCallback<BusinessData>() {
      public void onFailure(Throwable arg0) {
        callback.error("error getting logical models: ", arg0); //$NON-NLS-1$
      }
      public void onSuccess(BusinessData arg0) {
        callback.success(arg0);
      }
    });
  }
}
