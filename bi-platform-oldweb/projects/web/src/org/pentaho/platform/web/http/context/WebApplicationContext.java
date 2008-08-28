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
 * @created Jul 12, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.http.context;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.http.messages.Messages;


public class WebApplicationContext extends StandaloneApplicationContext {

  private String baseUrl;

  public WebApplicationContext(final String solutionRootPath, final String baseUrl, final String applicationPath, final Object context) {
    super(solutionRootPath, applicationPath, context);
    //TODO sbarkdull, do we need to consider path separators for windows?
    //assert !baseUrl.endsWith("\\") : "Base URL in WebApplicationContext appears to be using Windows path separators.";

    this.baseUrl = baseUrl;
  }

  public WebApplicationContext(final String solutionRootPath, final String baseUrl, final String applicationPath) {
    super(solutionRootPath, applicationPath);
    //TODO sbarkdull, do we need to consider path separators for windows?
    //assert !baseUrl.endsWith("\\") : "Base URL in WebApplicationContext appears to be using Windows path separators.";

    this.baseUrl = baseUrl;
  }

  @Override
  public String getBaseUrl() {
    if (!baseUrl.endsWith("/")) { //$NON-NLS-1$
      baseUrl = baseUrl + "/"; //$NON-NLS-1$
    }
    return baseUrl;
  }

  @Override
  public String getPentahoServerName() {
    return PentahoSystem.getSystemSetting("name", Messages.getString("PentahoSystem.USER_SYSTEM_TITLE")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
  }

}
