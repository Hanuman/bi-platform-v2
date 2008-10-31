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