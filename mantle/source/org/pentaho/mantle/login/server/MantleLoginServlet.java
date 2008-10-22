/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
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

public class MantleLoginServlet extends RemoteServiceServlet implements MantleLoginService {

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

  public boolean isShowUsersList() {
    return "true".equalsIgnoreCase(PentahoSystem.getSystemSetting("login-show-users-list", "true")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
