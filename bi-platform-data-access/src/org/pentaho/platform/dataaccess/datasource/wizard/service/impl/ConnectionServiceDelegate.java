package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.repository.hibernate.HibernateUtil;

public class ConnectionServiceDelegate {
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
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.createDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      return false;  
    }
  }
  public Boolean updateConnection(IConnection connection) {
    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.updateDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      return false;  
    }
  }
  public Boolean deleteConnection(IConnection connection) {
    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.deleteDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      return false;  
    }
  }
  public Boolean deleteConnection(String name) {
    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.deleteDatasource(name);
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      return false;  
    }
  }
  
  public boolean testConnection(IConnection connection) throws ConnectionServiceException {
    IPentahoConnection pentahoConnection = null;
      pentahoConnection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connection.getDriverClass(),
        connection.getUrl(), connection.getUsername(), connection.getPassword(), null,null);
      if (pentahoConnection != null) {
        pentahoConnection.close();
        return true;
      } else {
        return false;
      }
  }

  /**
   * This method converts from IDatasource to IConnection 
   * 
   * @param IDatasource
   * @return IConnection
   */ 
  private IConnection convertTo (IDatasource datasource) {
    IConnection returnDatasource = new org.pentaho.platform.dataaccess.datasource.beans.Connection();
    returnDatasource.setDriverClass(datasource.getDriverClass());
    returnDatasource.setName(datasource.getName());
    returnDatasource.setPassword(datasource.getPassword());
    returnDatasource.setUsername(datasource.getUserName());
    returnDatasource.setUrl(datasource.getUrl());
    return returnDatasource;
  }
  
  /**
   * This method converts from IConnection to IDatasource 
   * 
   * @param IConnection
   * @return IDatasource
   */ 
  private IDatasource convertFrom (IConnection connection) {
    IDatasource returnDatasource = (IDatasource) PentahoSystem.get(IDatasource.class, null);
    returnDatasource.setDriverClass(connection.getDriverClass());
    returnDatasource.setName(connection.getName());
    returnDatasource.setQuery("select count(*) from INFORMATION_SCHEMA.SYSTEM_SEQUENCES"); //$NON-NLS-1$
    returnDatasource.setPassword(connection.getPassword());
    returnDatasource.setUserName(connection.getUsername());
    returnDatasource.setUrl(connection.getUrl());
    return returnDatasource;
  }

}
