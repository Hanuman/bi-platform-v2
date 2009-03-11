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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
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
