package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDelegate;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.session.PentahoHttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConnectionGwtServlet extends RemoteServiceServlet implements ConnectionGwtService {

  ConnectionServiceDelegate SERVICE;

  public ConnectionGwtServlet() {
    SERVICE = new ConnectionServiceDelegate(getPentahoSession());
  }

  public List<IConnection> getConnections() {
    return SERVICE.getConnections();
  }
  public IConnection getConnectionByName(String name) {
    return SERVICE.getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection) {
    return SERVICE.addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection) {
    return SERVICE.updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection) {
    return SERVICE.deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name) {
    return SERVICE.deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection) throws ConnectionServiceException {
    return SERVICE.testConnection(connection);    
  }
  
  private IPentahoSession getPentahoSession() {
    HttpSession session = getThreadLocalRequest().getSession();
    IPentahoSession userSession = (IPentahoSession) session.getAttribute(IPentahoSession.PENTAHO_SESSION_KEY);

    LocaleHelper.setLocale(getThreadLocalRequest().getLocale());
    if (userSession != null) {
      return userSession;
    }
    userSession = new PentahoHttpSession(getThreadLocalRequest().getRemoteUser(), getThreadLocalRequest().getSession(), getThreadLocalRequest().getLocale(),
        null);
    LocaleHelper.setLocale(getThreadLocalRequest().getLocale());
    session.setAttribute(IPentahoSession.PENTAHO_SESSION_KEY, userSession);
    return userSession;
  }
}