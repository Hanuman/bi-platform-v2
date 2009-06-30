package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.dialect.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseConnectionService;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;

public class ConnectionServiceInMemoryDelegate {

  private String locale = Locale.getDefault().toString();
  
  // this should be a singleton
  private DatabaseConnectionService databaseConnectionService = new DatabaseConnectionService();

  private List<IConnection> connectionList = new ArrayList<IConnection>();
  private static final Log logger = LogFactory.getLog(ConnectionServiceInMemoryDelegate.class);
  public ConnectionServiceInMemoryDelegate() {
  }
  
  public List<IConnection> getConnections() throws ConnectionServiceException  {
    return connectionList;
  }
  public IConnection getConnectionByName(String name) throws ConnectionServiceException  {
    for(IConnection connection:connectionList) {
      if(connection.getName().equals(name)) {
        return connection;
      }
    }
    logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0003_UNABLE_TO_GET_CONNECTION",name,null));
    throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0003_UNABLE_TO_GET_CONNECTION",name,null));
  }
  public Boolean addConnection(IConnection connection) throws ConnectionServiceException  {
    if(!isConnectionExist(connection.getName())) {
      connectionList.add(connection);
      return true;
    } else {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0004_UNABLE_TO_ADD_CONNECTION",connection.getName(),null));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0004_UNABLE_TO_ADD_CONNECTION",connection.getName(),null));
    }
  }
  public Boolean updateConnection(IConnection connection) throws ConnectionServiceException  {
    IConnection conn = getConnectionByName(connection.getName());
    if(conn != null) {
      conn.setDriverClass(connection.getDriverClass());
      conn.setPassword(connection.getPassword());
      conn.setUrl(connection.getUrl());
      conn.setUsername(connection.getUsername());
      return true;
    } else {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION",connection.getName(),null));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION",connection.getName(),null));
    }
  }
  public Boolean deleteConnection(IConnection connection) throws ConnectionServiceException  {
    connectionList.remove(connectionList.indexOf(connection));
    return true;
  }
  public Boolean deleteConnection(String name) throws ConnectionServiceException  {
    for(IConnection connection:connectionList) {
      if(connection.getName().equals(name)) {
        return deleteConnection(connection);
      }
    }
    logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0006_UNABLE_TO_DELETE_CONNECTION",name,null));
    throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0006_UNABLE_TO_DELETE_CONNECTION",name,null));
  }
  
  public boolean testConnection(IConnection connection) throws ConnectionServiceException {
    Connection conn = null;
    try {
      conn = getConnection(connection);
    } catch (ConnectionServiceException dme) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName(),dme.getLocalizedMessage()),dme);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName(),dme.getLocalizedMessage()),dme);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName(),null));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName(),null));
      }
    }
    return true;
  }
  
  /**
   * NOTE: caller is responsible for closing connection
   * 
   * @param ds
   * @return
   * @throws DataSourceManagementException
   */
  private static Connection getConnection(IConnection connection) throws ConnectionServiceException {
    Connection conn = null;

    String driverClass = connection.getDriverClass();
    if (StringUtils.isEmpty(driverClass)) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0020_CONNECTION_ATTEMPT_FAILED"));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0020_CONNECTION_ATTEMPT_FAILED")); //$NON-NLS-1$
      
    }
    Class<?> driverC = null;

    try {
      driverC = Class.forName(driverClass);
    } catch (ClassNotFoundException e) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0021_DRIVER_NOT_FOUND_IN_CLASSPATH", driverClass),e);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0021_DRIVER_NOT_FOUND_IN_CLASSPATH"),e); //$NON-NLS-1$

    }
    if (!Driver.class.isAssignableFrom(driverC)) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0021_DRIVER_NOT_FOUND_IN_CLASSPATH", driverClass));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0021_DRIVER_NOT_FOUND_IN_CLASSPATH")); //$NON-NLS-1$

    }
    Driver driver = null;
    
    try {
      driver = driverC.asSubclass(Driver.class).newInstance();
    } catch (InstantiationException e) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0022_UNABLE_TO_INSTANCE_DRIVER", driverClass),e);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0022_UNABLE_TO_INSTANCE_DRIVER"), e); //$NON-NLS-1$
    } catch (IllegalAccessException e) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0022_UNABLE_TO_INSTANCE_DRIVER", driverClass),e);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0022_UNABLE_TO_INSTANCE_DRIVER"), e); //$NON-NLS-1$
    }
    try {
      DriverManager.registerDriver(driver);
      conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword());
      return conn;
    } catch (SQLException e) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0023_UNABLE_TO_CONNECT"), e);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0023_UNABLE_TO_CONNECT"), e); //$NON-NLS-1$
   }
  }
  private boolean isConnectionExist(String connectionName) {
    for(IConnection connection:connectionList) {
      if(connection.getName().equals(connectionName)) {
        return true;
      }
    }
    return false;
  }
  
  public IDatabaseConnection convertFromConnection(IConnection connection) throws ConnectionServiceException {
    IDatabaseConnection conn = databaseConnectionService.createDatabaseConnection(connection.getDriverClass(), connection.getUrl());
    conn.setName(connection.getName());
    conn.setUsername(connection.getUsername());
    conn.setPassword(connection.getPassword());
    return conn;
  }
  
  public IConnection convertToConnection(IDatabaseConnection connection) throws ConnectionServiceException {
    try {
      IDatabaseDialect dialect = databaseConnectionService.getDialectService().getDialect(connection);
      org.pentaho.platform.dataaccess.datasource.beans.Connection conn = new org.pentaho.platform.dataaccess.datasource.beans.Connection();
      String url = dialect.getURL(connection);
      conn.setDriverClass(dialect.getNativeDriver());
      conn.setName(connection.getName());
      conn.setUrl(url);
      conn.setUsername(connection.getUsername());
      conn.setPassword(connection.getPassword());
      return conn;
    } catch (KettleDatabaseException e) {
      throw new ConnectionServiceException(e);
    }
  }

}
