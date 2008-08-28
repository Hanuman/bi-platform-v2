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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.portlet.PortletSession;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    super.destroy();
  }

}
