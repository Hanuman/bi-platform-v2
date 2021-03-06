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
 * @created Aug 2, 2005 
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
import org.pentaho.platform.api.ui.INavigationComponent;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;

public class NavigationPortlet extends ViewPortlet {

  private final String SOLUTION = "solution"; //$NON-NLS-1$

  private final String PATH = "path"; //$NON-NLS-1$

  private final String XSL = "xsl"; //$NON-NLS-1$

  private static final Log portletLogger = LogFactory.getLog(NavigationPortlet.class);

  @Override
  public Log getLogger() {
    return NavigationPortlet.portletLogger;
  }

  @Override
  public void processPortletAction(final ActionRequest request, final ActionResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void doPortletView(final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

    PortletUrlFactory urlFactory = new PortletUrlFactory(response, request.getWindowState(), request.getPortletMode());

    PortletPreferences prefs = request.getPreferences();
    PortletRequestParameterProvider requestParameters = new PortletRequestParameterProvider(request);
    PortletSessionParameterProvider sessionParameters = new PortletSessionParameterProvider(userSession);
    PortletPreferencesParameterProvider portletPrefsParameters = new PortletPreferencesParameterProvider(prefs);

    boolean allowNavigation = true;
    String solution = request.getParameter(SOLUTION);
    if (solution == null) {
      // get the default value from the preferences
      solution = prefs.getValue(SOLUTION, null);
      if (solution != null) {
        requestParameters.setParameter(SOLUTION, solution);
        allowNavigation = false;
      } else {
        solution = ""; //$NON-NLS-1$
      }
    }
    String path = request.getParameter(PATH);
    if (path == null) {
      // get the default value from the preferences
      path = prefs.getValue(PATH, null);
      if (path != null) {
        requestParameters.setParameter(PATH, path);
        allowNavigation = false;
      } else {
        path = null;
      }
    }

    String hrefUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    String onClick = ""; //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    INavigationComponent navigate = PentahoSystem.get(INavigationComponent.class, userSession);
    navigate.setHrefUrl(hrefUrl);
    navigate.setOnClick(onClick);
    navigate.setSolutionParamName(SOLUTION);
    navigate.setPathParamName(PATH);
    navigate.setAllowNavigation(new Boolean(allowNavigation));
    navigate.setOptions(""); //$NON-NLS-1$
    navigate.setUrlFactory(urlFactory);
    navigate.setMessages(messages);
    navigate.validate(userSession, null);
    String xslName = prefs.getValue(XSL, null);
    if (xslName != null) {
      navigate.setXsl("text/html", xslName); //$NON-NLS-1$
    }
    navigate.setParameterProvider(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters);
    navigate.setParameterProvider(IParameterProvider.SCOPE_SESSION, sessionParameters);
    navigate
        .setParameterProvider(PortletPreferencesParameterProvider.SCOPE_PORTLET_PREFERENCES, portletPrefsParameters);

    String content = navigate.getContent("text/html"); //$NON-NLS-1$
    if ((content == null) || content.equals("")) { //$NON-NLS-1$
      content = "&nbsp;"; //$NON-NLS-1$
    }

    response.setContentType("text/html"); //$NON-NLS-1$
    response.getWriter().print(content);
  }
}
