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
package org.pentaho.platform.web.http.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.Authentication;
import org.acegisecurity.ui.logout.LogoutHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.messages.Messages;

/**
 * Pentaho behavior that should be invoked when a web user logs out.
 * 
 * @author mlowery
 * @see org.acegisecurity.ui.logout.LogoutHandler
 * @see org.acegisecurity.ui.logout.LogoutFilter
 */
public class PentahoLogoutHandler implements LogoutHandler {
  private static final Log logger = LogFactory.getLog(PentahoLogoutHandler.class);

  public void logout(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) {
    if (PentahoLogoutHandler.logger.isDebugEnabled()) {
      PentahoLogoutHandler.logger.debug(Messages.getString("PentahoLogoutHandler.DEBUG_HANDLE_LOGOUT")); //$NON-NLS-1$
    }
    IPentahoSession userSession = PentahoHttpSessionHelper.getPentahoSession(request);
    PentahoSystem.invokeLogoutListeners(userSession);
  }

}
