package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDelegate;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.datasource.DatasourceMgmtService;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.session.PentahoHttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConnectionDebugGwtServlet extends RemoteServiceServlet implements ConnectionGwtService {

  ConnectionServiceDelegate SERVICE;
  IDatasourceMgmtService datasourceMgmtSvc;
 
  public ConnectionDebugGwtServlet() {

  }

  private ConnectionServiceDelegate getService(){
    if(SERVICE == null){
      try {
        datasourceMgmtSvc = new DatasourceMgmtService();
        SERVICE = new ConnectionServiceDelegate(datasourceMgmtSvc);
        
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return SERVICE;
  }

  public List<IConnection> getConnections() {
    HibernateUtil.beginTransaction();
    return getService().getConnections();
  }
  public IConnection getConnectionByName(String name) {
    HibernateUtil.beginTransaction();
    return getService().getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection) {
    HibernateUtil.beginTransaction();
    return getService().addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection) {
    HibernateUtil.beginTransaction();
    return getService().updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection) {
    HibernateUtil.beginTransaction();
    return getService().deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name) {
    HibernateUtil.beginTransaction();
    return getService().deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection)  throws ConnectionServiceException{
    return getService().testConnection(connection);
  }
}