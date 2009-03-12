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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.portlet.PortletSession;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.BaseSession;

public class PentahoPortletSession extends BaseSession {

  /**
   * 
   */
  private static final long serialVersionUID = -4543813127374975180L;

  private PortletSession portletSession;

  private ArrayList addedAttributes;

  private static final Log logger = LogFactory.getLog(PentahoPortletSession.class);

  @Override
  public Log getLogger() {
    return PentahoPortletSession.logger;
  }

  public PentahoPortletSession(final String userName, final PortletSession portletSession, final Locale locale) {
    super(userName, portletSession.getId(), locale);
    this.portletSession = portletSession;
    addedAttributes = new ArrayList();
    
    // audit session creation
    AuditHelper.audit(getId(), getName(), getActionName(), getObjectName(), "", MessageTypes.SESSION_START, "", "", 0, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public Object getAttribute(final String attributeName) {
    return portletSession.getAttribute(attributeName, PortletSession.APPLICATION_SCOPE);
  }

  public Object getAttribute(final String attributeName, final int scope) {
    return portletSession.getAttribute(attributeName, scope);
  }

  public Iterator getAttributeNames() {
    return new EnumerationIterator(portletSession.getAttributeNames());
  }

  public void setAttribute(final String attributeName, final Object value) {
    portletSession.setAttribute(attributeName, value, PortletSession.APPLICATION_SCOPE);
    addedAttributes.add(attributeName);
  }

  public void setAttribute(final String attributeName, final Object value, final int scope) {
    portletSession.setAttribute(attributeName, value, scope);
    addedAttributes.add(attributeName);
  }

  public Object removeAttribute(final String attributeName) {
    Object result = getAttribute(attributeName);
    portletSession.removeAttribute(attributeName);
    addedAttributes.remove(attributeName);
    return result;
  }

  public Object removeAttribute(final String attributeName, final int scope) {
    Object result = getAttribute(attributeName, scope);
    portletSession.removeAttribute(attributeName, scope);
    addedAttributes.remove(attributeName);
    return result;
  }

  @Override
  public void destroy() {
    if (portletSession != null) {
      Iterator attributeIterator = addedAttributes.iterator();
      while (attributeIterator.hasNext()) {
        portletSession.removeAttribute((String) attributeIterator.next(), PortletSession.APPLICATION_SCOPE);
      }
    }

    // audit session destruction
    AuditHelper.audit(getId(), getName(), getActionName(), getObjectName(), "", MessageTypes.SESSION_END, "", "", 0, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    super.destroy();
  }

}
