package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDelegate;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ConnectionServiceBeanImpl implements ConnectionGwtService {

  ConnectionServiceDelegate SERVICE;
  IDatasourceMgmtService datasourceMgmtSvc;
  
  private ConnectionServiceDelegate getService(){
    if(SERVICE == null){
      try {
        IPentahoSession session = PentahoSessionHolder.getSession();
        
        datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class, session);
        SERVICE = new ConnectionServiceDelegate(datasourceMgmtSvc);
        
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return SERVICE;
  }

  public List<IConnection> getConnections() throws ConnectionServiceException {
    return getService().getConnections();
  }
  public IConnection getConnectionByName(String name) throws ConnectionServiceException {
    return getService().getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection) throws ConnectionServiceException {
    return getService().addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection) throws ConnectionServiceException {
    return getService().updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection) throws ConnectionServiceException {
    return getService().deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name) throws ConnectionServiceException {
    return getService().deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection) throws ConnectionServiceException {
    return getService().testConnection(connection);    
  }
}