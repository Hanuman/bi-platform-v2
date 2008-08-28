/*
 * Copyright 2005 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
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
          PentahoSystem.sessionStartup(userSession, true, sessionParameters);
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
