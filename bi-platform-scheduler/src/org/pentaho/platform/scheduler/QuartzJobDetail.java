/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.platform.scheduler;

import java.io.Serializable;

import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.scheduler.IJobDetail;
import org.quartz.JobDetail;

public class QuartzJobDetail implements IJobDetail, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 420L;
  // quartz job detail itself
  private JobDetail jobDetail = null;

  public QuartzJobDetail(JobDetail jobDetail) {
    this.jobDetail = jobDetail;
  }

  public String getName() {
    return jobDetail.getName();
  }

  public String getActionName() {
    return jobDetail.getJobDataMap().getString(IBackgroundExecution.BACKGROUND_ACTION_NAME_STR);
  }

  public String getSubmissionDate() {
    return jobDetail.getJobDataMap().getString(IBackgroundExecution.BACKGROUND_SUBMITTED);
  }

  public String getGroupName() {
    return jobDetail.getGroup();
  }

  public String getDescription() {
    return jobDetail.getDescription();
  }

  public String getFullName() {
    return jobDetail.getDescription();
  }

}
