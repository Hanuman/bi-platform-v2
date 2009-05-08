package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceInMemoryDelegate;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConnectionDebugGwtServlet extends RemoteServiceServlet implements ConnectionGwtService {

  ConnectionServiceInMemoryDelegate SERVICE;

 
  public ConnectionDebugGwtServlet() {

  }

  private ConnectionServiceInMemoryDelegate getService(){
    if(SERVICE == null){
      try {
        SERVICE = new ConnectionServiceInMemoryDelegate();
        // add the sample data default connection for testing
        Connection connection = new Connection();
        connection.setDriverClass("org.hsqldb.jdbcDriver");
        connection.setName("SampleData");
        connection.setUrl("jdbc:hsqldb:hsql://localhost:9001/sampledata");
        connection.setUsername("pentaho_user");
        connection.setPassword("password");
        SERVICE.addConnection(connection);
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return SERVICE;
  }

  public List<IConnection> getConnections()  throws ConnectionServiceException {
    return getService().getConnections();
  }
  public IConnection getConnectionByName(String name)  throws ConnectionServiceException {
    return getService().getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection)  throws ConnectionServiceException{ 
    return getService().addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection)  throws ConnectionServiceException {
    return getService().updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection)  throws ConnectionServiceException {
    return getService().deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name)  throws ConnectionServiceException {
    return getService().deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection)  throws ConnectionServiceException{
    return getService().testConnection(connection);
  }
}