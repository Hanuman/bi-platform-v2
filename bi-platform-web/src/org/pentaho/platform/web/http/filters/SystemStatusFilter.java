/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
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
 * @created August, 2006
 * @author Marc Batchelor
 * 
 */
package org.pentaho.platform.web.http.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * The purpose of this filter is to check to make sure that the platform is
 * properly initialized before letting requests in.
 *
 */
public class SystemStatusFilter implements Filter {

  private String redirectToOnInitError;

  private boolean systemInitializedOk;

  public void init(final FilterConfig filterConfig) throws ServletException {
    String failurePage = filterConfig.getInitParameter("initFailurePage"); //$NON-NLS-1$
    if ((failurePage == null) || (failurePage.length() == 0)) {
      failurePage = "InitFailure"; //$NON-NLS-1$
    }
    redirectToOnInitError = "/" + failurePage; //$NON-NLS-1$ 
    systemInitializedOk = PentahoSystem.getInitializedOK();
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException,
      ServletException {
    if (systemInitializedOk) {
      filterChain.doFilter(request, response);
    } else {
      HttpServletRequest req = (HttpServletRequest) request;
      if (req.getServletPath().endsWith(redirectToOnInitError)) {
        filterChain.doFilter(request, response);
      } else {
        RequestDispatcher dispatcher = request.getRequestDispatcher(redirectToOnInitError);
        dispatcher.forward(request, response);
      }
    }
  }

  public void destroy() {
  }

}
