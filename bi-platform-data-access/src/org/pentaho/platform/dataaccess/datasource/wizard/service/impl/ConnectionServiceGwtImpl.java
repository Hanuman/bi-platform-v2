package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtConnectionServiceAsync;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ConnectionServiceGwtImpl implements IXulAsyncConnectionService {

  static IGwtConnectionServiceAsync SERVICE;

  static {

    SERVICE = (org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtConnectionServiceAsync) GWT.create(org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtConnectionService.class);
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
      return  baseUrl + "gwtrpc/connectionService";
    }
    //we are running this client in hosted mode, so point to the servlet 
    //defined in war/WEB-INF/web.xml
    return moduleUrl + "ConnectionService";
  }

  public ConnectionServiceGwtImpl() {

  }

  
  public void getConnections(final XulServiceCallback<List<IConnection>> callback) {
    SERVICE.getConnections(new AsyncCallback<List<IConnection>>() {

      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(List<IConnection> arg0) {
        callback.success(arg0);
      }

    });
  }
  public void getConnectionByName(String name, final XulServiceCallback<IConnection> callback) {
    SERVICE.getConnectionByName(name, new AsyncCallback<IConnection>() {

      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(IConnection arg0) {
        callback.success(arg0);
      }

    });
  }
  public void addConnection(IConnection connection, final XulServiceCallback<Boolean> callback) {
    SERVICE.addConnection(connection, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    }); 
  }
  
  public void updateConnection(IConnection connection, final XulServiceCallback<Boolean> callback) {
    SERVICE.updateConnection(connection, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    }); 
  }
  public void deleteConnection(IConnection connection, final XulServiceCallback<Boolean> callback) {
    SERVICE.deleteConnection(connection, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    }); 
  }
  public void deleteConnection(String name, final XulServiceCallback<Boolean> callback) {
    SERVICE.deleteConnection(name, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    }); 
  }
  
  public void testConnection(IConnection connection,  final XulServiceCallback<Boolean> callback) {
    SERVICE.testConnection(connection, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(Boolean arg0) {
        callback.success(arg0);
      }

    }); 
  }

  public void convertFromConnection(final IConnection databaseConnection, final XulServiceCallback<IDatabaseConnection> callback) {
    SERVICE.convertFromConnection(databaseConnection, new AsyncCallback<IDatabaseConnection>() {
      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }
      public void onSuccess(IDatabaseConnection arg0) {
        callback.success(arg0);
      }
    }); 
    
  }

  public void convertToConnection(final IDatabaseConnection databaseConnection, final XulServiceCallback<IConnection> callback) {
    SERVICE.convertToConnection(databaseConnection, new AsyncCallback<IConnection>() {
      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0);
      }
      public void onSuccess(IConnection arg0) {
        callback.success(arg0);
      }
    }); 
  }
}
