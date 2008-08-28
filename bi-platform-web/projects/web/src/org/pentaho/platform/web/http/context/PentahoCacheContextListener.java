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
package org.pentaho.platform.web.http.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PentahoCacheContextListener implements ServletContextListener {

  public void contextInitialized(final ServletContextEvent event ) {
    // Nothing to do here...
  }

  public void contextDestroyed(final ServletContextEvent event) {
    // NOTE: if the cacheManager has been configured to have session creation scope
    // getCacheManager will return null, which is fine, since PentahoCacheSessionListener
    // should have cleaned up the session scoped caches. If the cacheManager
    // has been created with global scope, getCacheManager will return a non-null instance.
    ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
    if (cacheManager != null) {
      cacheManager.cacheStop();
    }
  }

}
