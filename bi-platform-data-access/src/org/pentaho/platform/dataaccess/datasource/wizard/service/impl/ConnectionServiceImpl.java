package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.dialect.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseConnectionService;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.repository.hibernate.HibernateUtil;

public class ConnectionServiceImpl implements IConnectionService {
  
  private IDataAccessPermissionHandler dataAccessPermHandler;
  private IDatasourceMgmtService datasourceMgmtSvc;
  private static final Log logger = LogFactory.getLog(ConnectionServiceImpl.class);
  
  public ConnectionServiceImpl() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class, session);
    String dataAccessClassName = null;
    try {
      //FIXME: we should be using an object factory of some kind here
      IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
      dataAccessClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", SimpleDataAccessPermissionHandler.class.getName() );  //$NON-NLS-1$ //$NON-NLS-2$
      Class<?> clazz = Class.forName(dataAccessClassName, true, getClass().getClassLoader());
      Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
      dataAccessPermHandler = (IDataAccessPermissionHandler)defaultConstructor.newInstance(new Object[]{});
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR",e.getLocalizedMessage()),e);        
        // TODO: Unhardcode once this is an actual plugin
        dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
    }
    
  }
  
  protected boolean hasDataAccessPermission() {
    return dataAccessPermHandler != null && dataAccessPermHandler.hasDataAccessPermission(PentahoSessionHolder.getSession());
  }
  
  public List<IConnection> getConnections() throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    
    List<IConnection> connectionList = new ArrayList<IConnection>();
    try  {
      for(IDatasource datasource:datasourceMgmtSvc.getDatasources()) {
        connectionList.add(convertTo(datasource));
      }
    } catch(DatasourceMgmtServiceException dme) {
        logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0002_UNABLE_TO_GET_CONNECTION_LIST",dme.getLocalizedMessage()));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0002_UNABLE_TO_GET_CONNECTION_LIST",dme.getLocalizedMessage()),dme);
    }
    return connectionList;
  }

  public IConnection getConnectionByName(String name) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    try {
      return convertTo(datasourceMgmtSvc.getDatasource(name));
    } catch(DatasourceMgmtServiceException dme) {
        logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0003_UNABLE_TO_GET_CONNECTION",name, dme.getLocalizedMessage()));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0003_UNABLE_TO_GET_CONNECTION",name, dme.getLocalizedMessage()),dme);
    }
  }
  
  public boolean addConnection(IConnection connection) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }

    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.createDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0004_UNABLE_TO_ADD_CONNECTION",connection.getName(), e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0004_UNABLE_TO_ADD_CONNECTION",connection.getName(), e.getLocalizedMessage()),e);      
    }
  }

  public boolean updateConnection(IConnection connection) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.updateDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION",connection.getName(), e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION",connection.getName(), e.getLocalizedMessage()),e);            
    }
  }
  
  public boolean deleteConnection(IConnection connection) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.deleteDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0006_UNABLE_TO_DELETE_CONNECTION",connection.getName(), e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0006_UNABLE_TO_DELETE_CONNECTION",connection.getName(), e.getLocalizedMessage()),e);            
    }
  }
  
  public boolean deleteConnection(String name) throws ConnectionServiceException  {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }

    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.deleteDatasource(name);
      HibernateUtil.commitTransaction();
      return true;
    } catch(Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0007_UNABLE_TO_TEST_CONNECTION",name, e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0007_UNABLE_TO_TEST_CONNECTION",name, e.getLocalizedMessage()),e);            
    }
  }
  
  public boolean testConnection(IConnection connection) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
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
    String driverClass = (connection.getDriverClass() != null) ? connection.getDriverClass().toLowerCase() : null;
    if(driverClass != null) {
      if(driverClass.indexOf("oracle") >=0 || (driverClass.indexOf("postgres") >=0)) { //$NON-NLS-1$
        returnDatasource.setQuery("select 1 from dual");//$NON-NLS-1$
      } else if(driverClass.indexOf("mysql") >=0) {//$NON-NLS-1$
        returnDatasource.setQuery("select 1");//$NON-NLS-1$
      } else if(driverClass.indexOf("hsql") >=0) {//$NON-NLS-1$
        returnDatasource.setQuery("select count(*) from INFORMATION_SCHEMA.SYSTEM_SEQUENCES"); //$NON-NLS-1$
      } 
    } 
    returnDatasource.setPassword(connection.getPassword());
    returnDatasource.setUserName(connection.getUsername());
    returnDatasource.setUrl(connection.getUrl());
    return returnDatasource;
  }
  
  
  public IDatabaseConnection convertFromConnection(IConnection connection) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    
    try {
      IServiceManager manager = PentahoSystem.get(IServiceManager.class);
      DatabaseConnectionService service = (DatabaseConnectionService)manager.getServiceBean("gwt", "databaseConnectionService");
      IDatabaseConnection conn = service.createDatabaseConnection(connection.getDriverClass(), connection.getUrl());
      conn.setName(connection.getName());
      conn.setUsername(connection.getUsername());
      conn.setPassword(connection.getPassword());
      return conn;
    } catch (ServiceException e) {
      throw new ConnectionServiceException(e);
    }
  }

  public IConnection convertToConnection(IDatabaseConnection connection) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    
    try {
      IServiceManager manager = PentahoSystem.get(IServiceManager.class);

      DatabaseConnectionService service = (DatabaseConnectionService)manager.getServiceBean("gwt", "databaseConnectionService");
      IDatabaseDialect dialect = service.getDialectService().getDialect(connection);
      
      Connection conn = new Connection();
      conn.setName(connection.getName());
      conn.setUsername(connection.getUsername());
      conn.setPassword(connection.getPassword());
      String url = dialect.getURL(connection);
      conn.setUrl(url);
      if (connection.getDatabaseType().getShortName().equals("GENERIC")) { //$NON-NLS-1$
        conn.setDriverClass(connection.getAttributes().get(GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS));
      } else {
        conn.setDriverClass(dialect.getNativeDriver());
      }
      return conn;
    } catch (KettleDatabaseException e) {
      throw new ConnectionServiceException(e);
    } catch (ServiceException e) {
      throw new ConnectionServiceException(e);
    }
  }
}
