package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.webservices.SessionHandler;
import org.pentaho.platform.repository.hibernate.HibernateUtil;

public class ConnectionServiceDelegate {
  
  private IDataAccessPermissionHandler dataAccessPermHandler;
  private IDatasourceMgmtService datasourceMgmtSvc;
  
  public ConnectionServiceDelegate() {
  }

  protected boolean hasDataAccessPermission() {
    if (dataAccessPermHandler == null) {
      String dataAccessClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessPermHandler = (IDataAccessPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        // TODO: error(Messages.getErrorString("DashboardRenderer.ERROR_0024_SQL_PERMISSIONS_INIT_ERROR", sqlExecClassName), e); //$NON-NLS-1$
        e.printStackTrace();
        
        // TODO: Unhardcode once this is an actual plugin
        dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
      }

      
    }
    return dataAccessPermHandler != null && dataAccessPermHandler.hasDataAccessPermission(SessionHandler.getSession());
  }
  
  public ConnectionServiceDelegate(IDatasourceMgmtService datasourceMgmtSvc) {
    this.datasourceMgmtSvc = datasourceMgmtSvc;
  }
  
  public List<IConnection> getConnections() throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
      // TODO: log
      System.out.println("NO PERMISSION");
      return null;
    }
    
    List<IConnection> connectionList = new ArrayList<IConnection>();
    try  {
      for(IDatasource datasource:datasourceMgmtSvc.getDatasources()) {
        connectionList.add(convertTo(datasource));
      }
    } catch(DatasourceMgmtServiceException dme) {
      throw new ConnectionServiceException("Unable to get connection list " + dme.getLocalizedMessage(), dme); 
    }
    return connectionList;
  }

  public IConnection getConnectionByName(String name) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
      // TODO: log
      System.out.println("NO PERMISSION");
      return null;
    }
    try {
      return convertTo(datasourceMgmtSvc.getDatasource(name));
    } catch(DatasourceMgmtServiceException dme) {
      throw new ConnectionServiceException("Unable to get connection: " + name + " " + dme.getLocalizedMessage(), dme);  
    }
  }
  
  public Boolean addConnection(IConnection connection) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
      // TODO: log
      System.out.println("NO PERMISSION");
      return null;
    }

    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.createDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      throw new ConnectionServiceException("Unable to add connection: " + connection.getName() + " "  + e.getLocalizedMessage(), e);  
    }
  }

  public Boolean updateConnection(IConnection connection) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
      // TODO: log
      System.out.println("NO PERMISSION");
      return null;
    }
    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.updateDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      throw new ConnectionServiceException("Unable to update connection: " + connection.getName() + " "  + e.getLocalizedMessage(),e);  
    }
  }
  
  public Boolean deleteConnection(IConnection connection) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
      // TODO: log
      System.out.println("NO PERMISSION");
      return null;
    }
    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.deleteDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      throw new ConnectionServiceException("Unable to delete connection: " + connection.getName() + " " +  e.getLocalizedMessage(),e);  
    }
  }
  public Boolean deleteConnection(String name) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
      // TODO: log
      System.out.println("NO PERMISSION");
      return null;
    }

    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.deleteDatasource(name);
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      throw new ConnectionServiceException("Unable to delete connection: " + name + " " +  e.getLocalizedMessage(),e);  
    }
  }
  
  public boolean testConnection(IConnection connection) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      // TODO: log
      System.out.println("NO PERMISSION");
      return false;
    }
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
