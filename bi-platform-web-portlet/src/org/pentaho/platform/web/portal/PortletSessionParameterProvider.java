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
 * @created Jul 11, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.portal;

import java.util.Date;
import java.util.Iterator;

import javax.portlet.PortletSession;

import org.pentaho.platform.api.engine.IParameterSetter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.BaseParameterProvider;

public class PortletSessionParameterProvider extends BaseParameterProvider implements IParameterSetter {

  private IPentahoSession session;

  public PortletSessionParameterProvider(final IPentahoSession session) {
    this.session = session;
  }

  @Override
  public Object getParameter(final String name) {
    if ("name".equals(name)) { //$NON-NLS-1$
      return session.getName();
    }

    if (session instanceof PentahoPortletSession) {
      PentahoPortletSession portletSession = (PentahoPortletSession) session;
      Object value = portletSession.getAttribute(name, PortletSession.PORTLET_SCOPE);
      if (value != null) {
        return value;
      }
      // now look at the application level
      value = portletSession.getAttribute(name, PortletSession.APPLICATION_SCOPE);
      return value;
    } else {
      return session.getAttribute(name);
    }
  }

  @Override
  public String getStringParameter(final String name, final String defaultValue) {
    Object valueObject = getParameter(name);
    if (valueObject != null) {
      return valueObject.toString();
    }
    return defaultValue;
  }

  @Override
  protected String getValue(final String name) {
    return getStringParameter(name, null);
  }

  public void setParameter(final String name, final String value) {
    session.setAttribute(name, value);
  }

  public void setParameter(final String name, final long value) {
    setParameter(name, Long.toString(value));
  }

  public void setParameter(final String name, final Date value) {
    session.setAttribute(name, value);
  }

  public void setParameter(final String name, final Object value) {
    session.setAttribute(name, value);
  }

  public Iterator getParameterNames() {
    return session.getAttributeNames();
  }

}
