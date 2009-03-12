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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.web.portal;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortalContext;

/**
 * Utility class with (mostly) static methods that aid in getting useful
 * debugging information from various portlet related classes.
 * 
 * @author Steven Barkdull
 *
 */
public class PortletDebugHelper {

  protected String logInfo(final ActionRequest request, final ActionResponse response) {
    StringBuffer b = new StringBuffer();

    String ctxP = request.getContextPath();
    b.append("Ctx Path: " + ctxP + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    Map pMap = request.getParameterMap();
    b.append("Request params:\n"); //$NON-NLS-1$
    Set ks = pMap.keySet();
    for (Iterator k = ks.iterator(); k.hasNext();) {
      String n = (String) k.next();
      Object p = pMap.get(n);
      b.append("param: " + n + ", " + p); //$NON-NLS-1$ //$NON-NLS-2$
    }
    PortalContext pCtx = request.getPortalContext();
    String pInfo = pCtx.getPortalInfo();
    b.append("\npInfo: " + pInfo); //$NON-NLS-1$
    Enumeration en = pCtx.getPropertyNames();
    b.append("Portlet props:\n"); //$NON-NLS-1$
    for (; en.hasMoreElements();) {
      String n = (String) en.nextElement();
      String p = pCtx.getProperty(n);
      b.append("prop: " + n + ", " + p); //$NON-NLS-1$ //$NON-NLS-2$
    }

    b.append("\nRequest props:\n"); //$NON-NLS-1$
    en = request.getPropertyNames();
    while (en.hasMoreElements()) {
      String n = (String) en.nextElement();
      String p = request.getProperty(n);
      b.append("prop: " + n + ", " + p); //$NON-NLS-1$ //$NON-NLS-2$
    }
    b.append("\n"); //$NON-NLS-1$
    return b.toString();
  }
}
