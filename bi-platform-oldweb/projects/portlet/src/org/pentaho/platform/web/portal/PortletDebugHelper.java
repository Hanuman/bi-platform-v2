/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
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
