package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDelegate;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConnectionDebugGwtServlet extends RemoteServiceServlet implements ConnectionGwtService {

  ConnectionServiceDelegate SERVICE;

  public ConnectionDebugGwtServlet() {
    SERVICE = new ConnectionServiceDelegate();
  }

  public List<IConnection> getConnections() {
    return SERVICE.getConnections();
  }
  public IConnection getConnectionByName(String name) {
    return SERVICE.getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection) {
    return SERVICE.addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection) {
    return SERVICE.updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection) {
    return SERVICE.deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name) {
    return SERVICE.deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection)  throws ConnectionServiceException{
    return SERVICE.testConnection(connection);
  }
}