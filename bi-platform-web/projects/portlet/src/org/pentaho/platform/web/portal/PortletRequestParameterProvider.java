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
