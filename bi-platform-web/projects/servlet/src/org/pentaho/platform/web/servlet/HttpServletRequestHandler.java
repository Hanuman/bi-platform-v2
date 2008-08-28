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
 * @created Jun 18, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.ui.IUIComponent;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;

public class HttpServletRequestHandler extends BaseRequestHandler {

  private HttpServletRequest request;

  public HttpServletRequestHandler(final IPentahoSession session, final String instanceId, final HttpServletRequest request,
      final IOutputHandler outputHandler, final IPentahoUrlFactory urlFactory) {
    super(session, instanceId, outputHandler, null, urlFactory);
    HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(session);
    setParameterProvider(IParameterProvider.SCOPE_SESSION, sessionParameters);
    setRequest(request);
  }

  public void handleUIRequest(final IUIComponent component, final String contentType) throws IOException {
    IContentItem contentItem = getOutputHandler().getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT,
        getSolutionName(), getInstanceId(), null);
    OutputStream outputStream = contentItem.getOutputStream(this.getActionName());
    component.handleRequest(outputStream, this, contentType, getParameterProviders());

  }

  public void setRequest(final HttpServletRequest request) {
    this.request = request;
    IParameterProvider requestParameters = new HttpRequestParameterProvider(request);
    setParameterProvider(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters);

  }

  public String getStringParameter(final String name) {
    return request.getParameter(name);
  }

  public Set getParameterNames() {
    return request.getParameterMap().keySet();
  }

}
