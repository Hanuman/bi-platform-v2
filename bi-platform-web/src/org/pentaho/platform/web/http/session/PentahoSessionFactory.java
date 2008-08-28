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
 * @created Jul 24, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.http.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.pentaho.platform.api.engine.IPentahoSession;

public class PentahoSessionFactory {

  public static final String PENTAHO_SESSION_KEY = "pentaho_session"; //$NON-NLS-1$

  public static IPentahoSession getSession(final String userName, final HttpSession session, final HttpServletRequest request) {

    IPentahoSession userSession = (IPentahoSession) session.getAttribute(PentahoSessionFactory.PENTAHO_SESSION_KEY);
    if (userSession != null) {
      return userSession;
    }
    userSession = new PentahoHttpSession(userName, session, request.getLocale(), userSession);
    return userSession;

  }

}
