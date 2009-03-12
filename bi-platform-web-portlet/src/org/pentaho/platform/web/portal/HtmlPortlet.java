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
import org.pentaho.platform.uifoundation.component.HtmlComponent;

public class HtmlPortlet extends ViewPortlet {

  private static final Log portletLogger = LogFactory.getLog(HtmlPortlet.class);

  @Override
  public Log getLogger() {
    return HtmlPortlet.portletLogger;
  }

  @Override
  public void processPortletAction(final ActionRequest request, final ActionResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

  }

  @Override
  public void doPortletView(final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

    PortletUrlFactory urlFactory = new PortletUrlFactory(response, request.getWindowState(), request.getPortletMode());

    PortletPreferences prefs = request.getPreferences();
    String location = prefs.getValue("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
    String error = prefs.getValue("error", null); //$NON-NLS-1$
    String content = prefs.getValue("content", null); //$NON-NLS-1$
    String typeStr = prefs.getValue("type", "url"); //$NON-NLS-1$ //$NON-NLS-2$

    int type = HtmlComponent.TYPE_URL;
    if (typeStr.equalsIgnoreCase("solution-file")) { //$NON-NLS-1$
      type = HtmlComponent.TYPE_SOLUTION_FILE;
    }

    if (content == null) {
      ArrayList messages = new ArrayList();
      HtmlComponent component = new HtmlComponent(type, location, error, urlFactory, messages);
      component.validate(userSession, null);
      content = component.getContent("text/html"); //$NON-NLS-1$
    }

    response.setContentType("text/html"); //$NON-NLS-1$
    if ((content == null) || content.equals("")) { //$NON-NLS-1$
      content = "&nbsp;"; //$NON-NLS-1$
    }
    response.getWriter().print(content);

  }

}
