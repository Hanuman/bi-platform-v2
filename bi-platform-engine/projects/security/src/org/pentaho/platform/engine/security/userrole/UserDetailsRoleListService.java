/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.security.userrole;

import java.util.ArrayList;
import java.util.List;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.messages.Messages;
import org.springframework.beans.factory.InitializingBean;

public class UserDetailsRoleListService implements InitializingBean, IUserDetailsRoleListService {

  private IUserRoleListService userRoleListService;

  public UserDetailsRoleListService() {
    super();
  }

  public void setUserRoleListService(final IUserRoleListService value) {
    this.userRoleListService = value;
  }

  public IUserRoleListService getUserRoleListService() {
    return this.userRoleListService;
  }

  public void afterPropertiesSet() throws Exception {
    if (this.userRoleListService == null) {
      throw new Exception(Messages.getString("UserDetailsRoleListService.ERROR_0001_USERROLELISTSERVICE_NOT_SET")); //$NON-NLS-1$
    }
    PentahoSystem.setUserDetailsRoleListService(this);
  }

  public List getAllRoles() {
    List rtn = new ArrayList();
    GrantedAuthority[] auths = userRoleListService.getAllAuthorities();
    for (GrantedAuthority element : auths) {
      rtn.add(element.getAuthority());
    }
    return rtn;
  }

  public List getAllUsers() {
    List rtn = new ArrayList();
    String[] users = userRoleListService.getAllUsernames();
    for (String element : users) {
      rtn.add(element);
    }
    return rtn;
  }

  public List getAllUsersInRole(final String role) {
    String[] users = userRoleListService.getUsernamesInRole(new GrantedAuthorityImpl(role));
    List rtn = new ArrayList();
    for (String element : users) {
      rtn.add(element);
    }
    return rtn;

  }

  public List getRolesForUser(final String userName) {
    List rtn = new ArrayList();
    GrantedAuthority[] auths = userRoleListService.getAuthoritiesForUser(userName);
    for (GrantedAuthority element : auths) {
      rtn.add(element.getAuthority());
    }
    return rtn;
  }

  public IPentahoSession getEffectiveUserSession(final String userName, final IParameterProvider paramProvider) {
    // Create user session object as un-authenticated so 
    // we can setup the roles before doing the startup actions.
    UserSession session = new UserSession(userName, null, false, paramProvider);
    session.setAuthenticated(userName);
    // Get roles into the session
    GrantedAuthority[] auths = userRoleListService.getAuthoritiesForUser(userName);
    Authentication auth = new UsernamePasswordAuthenticationToken(userName, null, auths);
    session.setAttribute(SecurityHelper.SESSION_PRINCIPAL, auth);
    // Now that roles are in place, do startup actions
    session.doStartupActions(paramProvider);
    // OK - Return back to the user.
    return session;
  }
}
