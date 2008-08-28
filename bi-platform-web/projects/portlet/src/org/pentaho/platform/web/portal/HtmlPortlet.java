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
