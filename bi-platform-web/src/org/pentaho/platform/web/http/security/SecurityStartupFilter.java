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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created Jan 18, 2006
 * @author mbatchel
 */
package org.pentaho.platform.web.http.security;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;

public class SecurityStartupFilter implements Filter {
  private static final Log logger = LogFactory.getLog(SecurityStartupFilter.class);

  public void destroy() {

  }

  public void init(final FilterConfig filterConfig) throws ServletException {
  }

  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    String user = request.getRemoteUser();
    if (user != null) {
      // User is authenticated. Check session to see if the users' startup
      // actions have already been done.
      IPentahoSession userSession = getPentahoSession(request);

      if ((user != null) && !userSession.isAuthenticated()) {
        // the user was not logged in before but is now....
        userSession.setAuthenticated(user);
      }
      Principal principal = SecurityHelper.getPrincipal(userSession);
      if (principal == null) {
        // principal = request.getUserPrincipal();
        principal = SecurityContextHolder.getContext().getAuthentication();
        if (SecurityStartupFilter.logger.isDebugEnabled()) {
          SecurityStartupFilter.logger.debug(principal);
        }
        SecurityHelper.setPrincipal(principal, userSession);
        try {
          // Do the startup actions...
        	
          IParameterProvider sessionParameters = new PentahoSessionParameterProvider(userSession);
          PentahoSystem.sessionStartup(userSession, sessionParameters);
        } catch (Exception ex) {
          SecurityStartupFilter.logger.error(ex.getLocalizedMessage(), ex);
          // Yes, keep going, in spite of the error.
        }
      }
      filterChain.doFilter(request, servletResponse);
    } else {
      filterChain.doFilter(request, servletResponse);
    }

  }

  protected IPentahoSession getPentahoSession(final HttpServletRequest request) {
    return PentahoHttpSessionHelper.getPentahoSession(request);
  }

}
