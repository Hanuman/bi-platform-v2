package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IConnection;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConnectionGwtServiceAsync {
  void getConnections(AsyncCallback<List<IConnection>> callback);
  void getConnectionByName(String name, AsyncCallback<IConnection> callback);
  void addConnection(IConnection connection, AsyncCallback<Boolean> callback);
  void updateConnection(IConnection connection, AsyncCallback<Boolean> callback);
  void deleteConnection(IConnection connection, AsyncCallback<Boolean> callback);
  void deleteConnection(String name, AsyncCallback<Boolean> callback);
  void testConnection(IConnection connection, AsyncCallback<Boolean> callback);
  void convertToConnection(IDatabaseConnection connection, AsyncCallback<IConnection> callback);
  void convertFromConnection(IConnection connection, AsyncCallback<IDatabaseConnection> callback);
}

  