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

package org.pentaho.platform.web.portal;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.portal.messages.Messages;

public class PortletApplicationContext extends StandaloneApplicationContext {

  private String baseUrl;

  public PortletApplicationContext(final String solutionRootPath, final String baseUrl, final String applicationPath) {
    super(solutionRootPath, applicationPath);
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

}
