package org.pentaho.mantle.login.server;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.pentaho.mantle.login.client.MantleLoginService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.session.PentahoHttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MantleLoginServlet extends RemoteServiceServlet implements MantleLoginService{

  private static final String ACEGI_URL = "j_acegi_security_check";
  
  public List<String> getAllUsers() {

    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
    
    List<String> users = userDetailsRoleListService.getAllUsers();
    Collections.sort(users);
    return users; 
  }
  

  public boolean isAuthenticated() {
    return getPentahoSession() != null && getPentahoSession().isAuthenticated();
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

  