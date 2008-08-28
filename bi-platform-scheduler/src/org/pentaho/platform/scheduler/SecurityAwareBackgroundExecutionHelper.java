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
package org.pentaho.platform.scheduler;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.content.CoreContentRepositoryOutputHandler;

public class SecurityAwareBackgroundExecutionHelper extends QuartzBackgroundExecutionHelper {

  @Override
  public IPentahoSession getEffectiveUserSession(final String userName) {
    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
    if (userDetailsRoleListService != null) {
      return userDetailsRoleListService.getEffectiveUserSession(userName, null);
    } else {
      return super.getEffectiveUserSession(userName);
    }
  }

  @Override
  public IOutputHandler getContentOutputHandler(final String location, final String fileName,
      final String solutionName, final IPentahoSession userSession, final IParameterProvider parameterProvider) {
    // todo MB - try to detect background execution of a subscription in a better way
    String subsName = parameterProvider.getStringParameter("subscribe-name", null); //$NON-NLS-1$
    CoreContentRepositoryOutputHandler outputHandler = (CoreContentRepositoryOutputHandler) super
        .getContentOutputHandler(location, fileName, solutionName, userSession, parameterProvider);
    if (subsName != null) {
      // Any subscription-specific changes to outputHandler goes here...
      outputHandler.setWriteMode(IContentItem.WRITEMODE_KEEPVERSIONS);
    }
    return outputHandler;
  }

}
