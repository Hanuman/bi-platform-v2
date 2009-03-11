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

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

public class SecurityAwareBackgroundSubscriptionHelper extends SecurityAwareBackgroundExecutionHelper {

  public void trackBackgroundExecution(IPentahoSession userSession, String GUID) {
    // Don't need to track this using hibernate, it is already tracked by subscription admin
  }

  // Helper Utility Methods
  @Override
  protected JobDetail createDetailFromParameterProvider(IParameterProvider parameterProvider,
      IPentahoSession userSession, String outputContentGUID, String jobName, String jobGroup, String description, 
      String actionSeqPath) {

    String subscribeName = parameterProvider.getStringParameter("subscribe-name", null); //$NON-NLS-1$
    JobDetail jobDetail = super.createDetailFromParameterProvider(parameterProvider, userSession, 
        outputContentGUID, subscribeName, jobGroup, description, actionSeqPath);
    JobDataMap data = jobDetail.getJobDataMap();

    ActionInfo actionInfo = ActionInfo.parseActionString( actionSeqPath );
    data.put(BACKGROUND_CONTENT_LOCATION_STR, SubscriptionHelper.getSubscriptionOutputLocation(
        actionInfo.getSolutionName(), actionInfo.getPath(), actionInfo.getActionName() ) );
    
    // Make the subscribe name the GUID for the content.
    data.put(QuartzBackgroundExecutionHelper.BACKGROUND_CONTENT_GUID_STR, subscribeName);
    return jobDetail;
  }
}