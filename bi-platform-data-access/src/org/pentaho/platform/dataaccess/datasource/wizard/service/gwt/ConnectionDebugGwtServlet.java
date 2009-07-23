package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryConnectionServiceImpl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConnectionDebugGwtServlet extends RemoteServiceServlet implements IGwtConnectionService {

  /**
   * 
   */
  private static final long serialVersionUID = -6800099826721568704L;
  public static InMemoryConnectionServiceImpl SERVICE;

 
  public ConnectionDebugGwtServlet() {

  }

  private InMemoryConnectionServiceImpl getService(){
    if(SERVICE == null){
      try {
        SERVICE = new InMemoryConnectionServiceImpl();
        // add the sample data default connection for testing
        Connection connection = new Connection();
        connection.setDriverClass("org.hsqldb.jdbcDriver"); //$NON-NLS-1$
        connection.setName("SampleData");//$NON-NLS-1$
        connection.setUrl("jdbc:hsqldb:hsql://localhost:9001/sampledata");//$NON-NLS-1$
        connection.setUsername("pentaho_user");//$NON-NLS-1$
        connection.setPassword("password");//$NON-NLS-1$
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
  public boolean addConnection(IConnection connection)  throws ConnectionServiceException{ 
    return getService().addConnection(connection);
  }

  public boolean updateConnection(IConnection connection)  throws ConnectionServiceException {
    return getService().updateConnection(connection);
  }

  public boolean deleteConnection(IConnection connection)  throws ConnectionServiceException {
    return getService().deleteConnection(connection);
  }
    
  public boolean deleteConnection(String name)  throws ConnectionServiceException {
    return getService().deleteConnection(name);    
  }

  public boolean testConnection(IConnection connection)  throws ConnectionServiceException{
    return getService().testConnection(connection);
  }

  public IDatabaseConnection convertFromConnection(IConnection connection) throws ConnectionServiceException {
    return getService().convertFromConnection(connection);
  }

  public IConnection convertToConnection(IDatabaseConnection connection) throws ConnectionServiceException {
    return getService().convertToConnection(connection);
  }
}