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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
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
