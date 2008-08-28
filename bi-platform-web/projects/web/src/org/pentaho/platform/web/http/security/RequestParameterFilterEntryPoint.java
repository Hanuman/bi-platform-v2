/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.web.http.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.ui.basicauth.BasicProcessingFilter;
import org.springframework.beans.factory.InitializingBean;

/**
 * Used by the <code>SecurityEnforcementFilter</code> to commence
 * authentication via the {@link BasicProcessingFilter}.
 * 
 * <P>
 * Once a user agent is authenticated using Request Parameter authentication, logout
 * requires that the browser be closed or an unauthorized (401) header be
 * sent. The simplest way of achieving the latter is to call the {@link
 * #commence(ServletRequest, ServletResponse)} method below. This will
 * indicate to the browser its credentials are no longer authorized, causing
 * it to prompt the user to login again.
 * </p>
 */
public class RequestParameterFilterEntryPoint implements AuthenticationEntryPoint, InitializingBean {
  //~ Instance fields ========================================================

  //~ Methods ================================================================

  public void afterPropertiesSet() throws Exception {
    // Everything is OK
  }

  public void commence(final ServletRequest request, final ServletResponse response, final AuthenticationException authException)
      throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
  }
}
