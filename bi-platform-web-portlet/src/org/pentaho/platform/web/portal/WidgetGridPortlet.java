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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
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
import org.pentaho.platform.api.engine.IMessageFormatter;
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
        PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage(
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
