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
