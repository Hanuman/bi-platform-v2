package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.ui.xul.XulServiceCallback;

public class ConnectionServiceDebugImpl implements ConnectionService{

  ConnectionServiceInMemoryDelegate SERVICE;
  public ConnectionServiceDebugImpl(){
    SERVICE = new ConnectionServiceInMemoryDelegate();
  }
 
  public void getConnections(XulServiceCallback<List<IConnection>> callback) {
    callback.success(SERVICE.getConnections());
  }
  public void getConnectionByName(String name, XulServiceCallback<IConnection> callback) {
    callback.success(SERVICE.getConnectionByName(name));
  }
  public void addConnection(IConnection connection, XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.addConnection(connection));
  }
  public void updateConnection(IConnection connection, XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.updateConnection(connection));
  }
  public void deleteConnection(IConnection connection, XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.deleteConnection(connection));
  }
  public void deleteConnection(String name, XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.deleteConnection(name));
  }
  public void testConnection(IConnection connection, XulServiceCallback<Boolean> callback)throws ConnectionServiceException  {
    callback.success(SERVICE.testConnection(connection));
  }  
  
  
}

  