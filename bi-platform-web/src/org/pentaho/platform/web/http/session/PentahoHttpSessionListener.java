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
 * @created May 10, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.http.session;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.messages.Messages;

public class PentahoHttpSessionListener implements HttpSessionListener {

  private static final boolean debug = PentahoSystem.debug;

  private static final HashMap sessionMap = new HashMap();

  public void sessionCreated(final HttpSessionEvent event) {
    // we can't find out what the locale of the request is so we go with the
    // default for now...
    LocaleHelper.setLocale(Locale.getDefault());
    String sessionId = event.getSession().getId();
    if (PentahoHttpSessionListener.debug) {
      Logger.debug(this, Messages.getString("HttpSessionListener.DEBUG_SESSION_CREATED", sessionId)); //$NON-NLS-1$
    }

    // AuditHelper.audit( instanceId, String userId, String actionName,
    // String objectType, MessageTypes.PROCESS_ID_SESSION,
    // MessageTypes.SESSION_CREATE, "http session", "", 0, null );

  }

  public void sessionDestroyed(final HttpSessionEvent event) {
    HttpSession session = event.getSession();
    try {
      if (session != null) {
        String sessionId = event.getSession().getId();
        Object obj = session.getAttribute( PentahoSystem.PENTAHO_SESSION_KEY ); //$NON-NLS-1$
        if (obj != null) {
          IPentahoSession userSession = (IPentahoSession) obj;
          userSession.destroy();
        } else {
          String info[] = PentahoHttpSessionListener.getSessionInfo(sessionId);
          if (info != null) {
            String instanceId = info[5];
            String userId = info[3];
            String activityId = info[1];
            String objectType = info[2];
            String processId = info[0];
            String messageType = MessageTypes.SESSION_END;
            String message = "http "; //$NON-NLS-1$
            String value = ""; //$NON-NLS-1$
            long startTime = Long.parseLong(info[4]);
            long endTime = new Date().getTime();
            AuditHelper.audit(instanceId, userId, activityId, objectType, processId, messageType, message, value,
                ((endTime - startTime) / 1000), null);
          }
        }
      }
    } catch (Throwable e) {
      Logger.error(this, Messages.getErrorString("HttpSessionListener.ERROR_0001_ERROR_DESTROYING_SESSION"), e); //$NON-NLS-1$
    }

  }

  public static synchronized void registerHttpSession(final String sessionId, final String processId, final String activityId,
      final String objectName, final String userName, final String id, final long start) {
    PentahoHttpSessionListener.sessionMap.put(id, new String[] { processId, activityId, objectName, userName, new Long(start).toString(),
        sessionId });
  }

  public static synchronized void deregisterHttpSession(final String id) {
    PentahoHttpSessionListener.sessionMap.remove(id);
  }

  private static synchronized String[] getSessionInfo(final String id) {
    return (String[]) PentahoHttpSessionListener.sessionMap.get(id);
  }

}
