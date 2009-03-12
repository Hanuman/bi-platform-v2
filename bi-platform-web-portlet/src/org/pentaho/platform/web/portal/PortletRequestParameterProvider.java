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

import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.pentaho.platform.api.engine.IParameterSetter;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.util.web.HttpUtil;

public class PortletRequestParameterProvider extends SimpleParameterProvider implements IParameterSetter {

  public static final String SCOPE_PORTLET_REQUEST = "portletRequest"; //$NON-NLS-1$

  private PortletRequest portletRequest;

  public PortletRequestParameterProvider(final PortletRequest portletRequest) {
    this.portletRequest = portletRequest;
    setPortletRequestParameters(portletRequest.getParameterMap());

    if (portletRequest.getParameter(SimpleParameterProvider.ADDITIONAL_PARAMS) != null) {
      String additionalParameters = portletRequest.getParameter(SimpleParameterProvider.ADDITIONAL_PARAMS);
      int idx = additionalParameters.indexOf("?"); //$NON-NLS-1$
      if (idx > 0) {
        additionalParameters = additionalParameters.substring(idx + 1);
      }
      Map additionalParms = HttpUtil.parseQueryString(additionalParameters);
      setPortletRequestParameters(additionalParms);
    }
  }

  /**
   * Converts single value arrays to String parameters
   * 
   */
  private void setPortletRequestParameters(final Map paramMap) {
    for (Iterator it = paramMap.entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      Object value = entry.getValue();
      if (value != null) {
        if ((value instanceof Object[]) && (((Object[]) value).length == 1)) {
          setParameter((String) entry.getKey(), String.valueOf(((Object[]) value)[0]));
        } else {
          setParameter((String) entry.getKey(), value);
        }
      }
    }
  }

  public PortletRequest getRequest() {
    return portletRequest;
  }
}
