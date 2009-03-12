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
import java.io.PrintWriter;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.pentaho.platform.web.portal.messages.Messages;

public abstract class ViewPortlet extends BasePortlet {

  @Override
  public void doPortletHelp(final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {
    response.setContentType("text/html"); //$NON-NLS-1$
    PrintWriter out = response.getWriter();
    out.println(Messages.getString(
        "ViewPortlet.CODE_MESSAGE_TEMPLATE", Messages.getString("ViewPortlet.USER_HELP_NOT_AVAILABLE"))); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void doPortletEdit(final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {
    response.setContentType("text/html"); //$NON-NLS-1$
    PrintWriter out = response.getWriter();
    // TODO sbarkdull, ViewPortlet.CODE_MESSAGE_TEMPLATE probably shouldnt be in resource file
    out.println(Messages.getString(
        "ViewPortlet.CODE_MESSAGE_TEMPLATE", Messages.getString("ViewPortlet.USER_OPTIONS_NOT_AVAILABLE"))); //$NON-NLS-1$ //$NON-NLS-2$
  }

  protected String getSetting(final String name, final String defaultValue, final ActionRequest request,
      final PortletRequestParameterProvider requestParameters) {

    PortletPreferences prefs = request.getPreferences();
    String value = request.getParameter(name);
    if (value == null) {
      // get the default value from the preferences
      value = prefs.getValue(name, null);
      if ((value != null) && (requestParameters != null)) {
        requestParameters.setParameter(name, value);
      } else {
        value = defaultValue;
      }
    }
    return value;
  }

  /**
   * Using <code>name</code> as a key, look for the key first in the portal's RenderRequest,
   * if it is found, return its value.
   * If it is not found in the RenderRequest, look in the PortletPreferences. If it is
   * found, and the PortletRequestParameterProvider is not null, add the key/value
   * pair to the PortletRequestParameterProvider, and return the value.
   * If it is not found in either the RenderRequest or the PortletPreferences,
   * return the <code>defaultValue</code> parameter.
   * 
   * @param name
   * @param defaultValue String returned if name cannot be found in the RenderRequest or the PortletPreferences.
   * @param request RendRequest active during this request cycle.
   * @param requestParameters PortletRequestParameterProvider active during this request cycle
   * @return the value associated with the key specified by the <code>name</code> parameter.
   */
  protected String getSetting(final String name, final String defaultValue, final RenderRequest request,
      final PortletRequestParameterProvider requestParameters) {

    PortletPreferences prefs = request.getPreferences();
    String value = request.getParameter(name);
    if (value == null) {
      // get the default value from the preferences
      value = prefs.getValue(name, null);
      if (value != null) {
        if (requestParameters != null) {
          requestParameters.setParameter(name, value);
        }
      } else {
        value = defaultValue;
      }
    }
    return value;
  }

  protected long getSetting(final String name, final long defaultValue, final RenderRequest request,
      final PortletRequestParameterProvider requestParameters) {

    String valueStr = getSetting(name, Long.toString(defaultValue), request, requestParameters);

    try {
      return Long.parseLong(valueStr);
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
