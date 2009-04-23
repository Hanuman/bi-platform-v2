package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ConnectionServiceDelegate {

  private String locale = Locale.getDefault().toString();

  private IDatasourceMgmtService datasourceMgmtSvc;
  
  public ConnectionServiceDelegate() {
   }

  public ConnectionServiceDelegate(IDatasourceMgmtService datasourceMgmtSvc) {
    this.datasourceMgmtSvc = datasourceMgmtSvc;
  }
  
  public List<IConnection> getConnections() {
    List<IConnection> connectionList = new ArrayList<IConnection>();
    try  {
      for(IDatasource datasource:datasourceMgmtSvc.getDatasources()) {
        connectionList.add(convertTo(datasource));
      }
    } catch(DatasourceMgmtServiceException dme) {
      return null;
    }
    return connectionList;
  }
  public IConnection getConnectionByName(String name) {
    try {
      return convertTo(datasourceMgmtSvc.getDatasource(name));
    } catch(DatasourceMgmtServiceException dme) {
      return null;  
    }
    
  }
  public Boolean addConnection(IConnection connection) {
    try {
      datasourceMgmtSvc.createDatasource(convertFrom(connection));
      return true;
    } catch(Exception e) {
      return false;  
    }
  }
  public Boolean updateConnection(IConnection connection) {
    try {
      datasourceMgmtSvc.updateDatasource(convertFrom(connection));
      return true;
    } catch(Exception e) {
      return false;  
    }
  }
  public Boolean deleteConnection(IConnection connection) {
    try {
      datasourceMgmtSvc.deleteDatasource(convertFrom(connection));
      return true;
    } catch(Exception e) {
      return false;  
    }
  }
  public Boolean deleteConnection(String name) {
    try {
      datasourceMgmtSvc.deleteDatasource(name);
      return true;
    } catch(Exception e) {
      return false;  
    }
  }
  
  public boolean testConnection(IConnection connection) throws ConnectionServiceException {
    Connection conn = null;
    try {
      conn = getConnection(connection);
    } catch (ConnectionServiceException dme) {
      throw new ConnectionServiceException(dme.getMessage(), dme);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        throw new ConnectionServiceException(e);
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
      throw new ConnectionServiceException("Connection attempt failed"); //$NON-NLS-1$  
    }
    Class<?> driverC = null;

    try {
      driverC = Class.forName(driverClass);
    } catch (ClassNotFoundException e) {
      throw new ConnectionServiceException("Driver not found in the class path. Driver was " + driverClass, e); //$NON-NLS-1$
    }
    if (!Driver.class.isAssignableFrom(driverC)) {
      throw new ConnectionServiceException("Driver not found in the class path. Driver was " + driverClass); //$NON-NLS-1$    }
    }
    Driver driver = null;
    
    try {
      driver = driverC.asSubclass(Driver.class).newInstance();
    } catch (InstantiationException e) {
      throw new ConnectionServiceException("Unable to instance the driver", e); //$NON-NLS-1$
    } catch (IllegalAccessException e) {
      throw new ConnectionServiceException("Unable to instance the driver", e); //$NON-NLS-1$    }
    }
    try {
      DriverManager.registerDriver(driver);
      conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword());
      return conn;
    } catch (SQLException e) {
      throw new ConnectionServiceException("Unable to connect", e); //$NON-NLS-1$
    }
  }

  private IConnection convertTo (IDatasource datasource) {
    IConnection returnDatasource = new org.pentaho.platform.dataaccess.datasource.beans.Connection();
    returnDatasource.setDriverClass(datasource.getDriverClass());
    returnDatasource.setName(datasource.getName());
    returnDatasource.setPassword(datasource.getPassword());
    returnDatasource.setUsername(datasource.getUserName());
    returnDatasource.setUrl(datasource.getUrl());
    return returnDatasource;
  }
  
  private IDatasource convertFrom (IConnection connection) {
    IDatasource returnDatasource = (IDatasource) PentahoSystem.get(IDatasource.class, null);
    //IPasswordService passwordService  = (IPasswordService) PentahoSystem.get(IPasswordService.class, null); 
    returnDatasource.setDriverClass(connection.getDriverClass());
    returnDatasource.setName(connection.getName());
    //try {
    returnDatasource.setPassword(/*passwordService.encrypt((*/connection.getPassword()/*))*/);
    //} catch(Exception e) {
      
    //}
    returnDatasource.setUserName(connection.getUsername());
    returnDatasource.setUrl(connection.getUrl());
    return returnDatasource;
  }

}
