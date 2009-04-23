package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDelegate;
import org.pentaho.platform.engine.core.system.PentahoSystem;
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
        IPentahoSession session = PentahoHttpSessionHelper.getPentahoSession(this.getThreadLocalRequest());
        datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class, session); //$NON-NLS-1$
        SERVICE = new ConnectionServiceDelegate(datasourceMgmtSvc);
        
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return SERVICE;
  }

  public List<IConnection> getConnections() {
    return getService().getConnections();
  }
  public IConnection getConnectionByName(String name) {
    return getService().getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection) {
    return getService().addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection) {
    return getService().updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection) {
    return getService().deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name) {
    return getService().deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection)  throws ConnectionServiceException{
    return getService().testConnection(connection);
  }
}