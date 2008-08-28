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
 * Created Apr 9, 2006
 *
 * @author mbatchel
 */
package org.pentaho.platform.web.http.session;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PentahoCacheSessionListener implements HttpSessionListener {

  public void sessionCreated(final HttpSessionEvent event) {
    // Nothing to do here.
  }

  public void sessionDestroyed(final HttpSessionEvent event) {
    HttpSession session = event.getSession();
    Object obj = session.getAttribute( PentahoSystem.PENTAHO_SESSION_KEY ); //$NON-NLS-1$
    if (obj != null) {
      IPentahoSession userSession = (IPentahoSession)obj;
      ICacheManager cacheManager = PentahoSystem.getCacheManager( userSession );
      if ( null != cacheManager ) {
        IPentahoSession pentahoSession = (IPentahoSession) obj;
        if(pentahoSession != null) {
          cacheManager.removeRegionCache(pentahoSession.getId());  
        }
        
      }
    }
  }

}
