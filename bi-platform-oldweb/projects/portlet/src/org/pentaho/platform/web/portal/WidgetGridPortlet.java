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
 * @created Aug 24, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.portal;

import java.io.IOException;
import java.util.ArrayList;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.component.xml.WidgetGridComponent;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.portal.messages.Messages;

public class WidgetGridPortlet extends ViewPortlet {

  private final String WIDGET = "widget"; //$NON-NLS-1$

  private final String WIDGETGRID = "widget-grid"; //$NON-NLS-1$

  private static final Log portletLogger = LogFactory.getLog(WidgetGridPortlet.class);

  @Override
  public Log getLogger() {
    return WidgetGridPortlet.portletLogger;
  }

  @Override
  public void processPortletAction(final ActionRequest request, final ActionResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

  }

  @Override
  public void doPortletView(final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

    response.setContentType("text/html"); //$NON-NLS-1$

    PortletUrlFactory urlFactory = new PortletUrlFactory(response, request.getWindowState(), request.getPortletMode());

    PortletRequestParameterProvider requestParameters = new PortletRequestParameterProvider(request);
    PortletSessionParameterProvider sessionParameters = new PortletSessionParameterProvider(userSession);
    PortletPreferences prefs = request.getPreferences();
    PortletPreferencesParameterProvider portletPrefsParameters = new PortletPreferencesParameterProvider(prefs);

    String widgetDefinition = getSetting(WIDGET, null, request, requestParameters);
    String widgetGridDataDefinition = getSetting(WIDGETGRID, null, request, requestParameters);

    if (widgetDefinition == null) {
      response.getWriter().print(Messages.getString("Widget.USER_WIDGET_NOT_SPECIFIED")); //$NON-NLS-1$
      return;
    }

    String urlDrillTemplate = getSetting("drill-url", null, request, null); //$NON-NLS-1$
    ArrayList messages = new ArrayList();
    WidgetGridComponent widget = null;
    try {
      widget = new WidgetGridComponent(widgetDefinition, urlFactory, messages);
      widget.validate(userSession, null);
      widget.setDataAction(widgetGridDataDefinition);
      widget.setDrillUrlTemplate(urlDrillTemplate);

      widget.setParameterProvider(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters);
      widget.setParameterProvider(IParameterProvider.SCOPE_SESSION, sessionParameters);
      widget
          .setParameterProvider(PortletPreferencesParameterProvider.SCOPE_PORTLET_PREFERENCES, portletPrefsParameters);

      String content = widget.getContent("text/html"); //$NON-NLS-1$

      if (content == null) {
        StringBuffer buffer = new StringBuffer();
        PentahoSystem.getMessageFormatter(userSession).formatErrorMessage(
            "text/html", Messages.getString("Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET"), messages, buffer); //$NON-NLS-1$ //$NON-NLS-2$
        content = buffer.toString();
      }
      if ((content == null) || content.equals("")) { //$NON-NLS-1$
        content = "&nbsp;"; //$NON-NLS-1$
      }
      response.getWriter().print(content);
    } finally {
      if (widget != null) {
        widget.dispose();
      }
    }
  }
}
