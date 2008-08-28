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

import java.util.Date;

import org.pentaho.platform.api.engine.IScheduledJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

public class QuartzScheduledJob implements IScheduledJob {

  private Trigger trigger;

  private Throwable lastException = null;

  public QuartzScheduledJob(final Trigger trigger) {
    this.trigger = trigger;
  }

  public Date getNextTriggerTime() {
    return ((trigger == null ? null : trigger.getNextFireTime()));
  }

  public Date getLastTriggerTime() {
    return ((trigger == null ? null : trigger.getPreviousFireTime()));
  }

  public String getDescription() {
    return ((trigger == null ? null : trigger.getDescription()));
  }

  public int getExecutionState() {
    lastException = null;
    try {
      Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
      int state = scheduler.getTriggerState(trigger.getName(), trigger.getGroup());
      try {
        JobDetail job = scheduler.getJobDetail(trigger.getName(), trigger.getGroup());
        job.validate();
      } catch (Throwable t) {
        lastException = t;
        return (IScheduledJob.STATE_ERROR);
      }
      return (state);
    } catch (Throwable t) {
      lastException = t;
      return (IScheduledJob.STATE_NONE);
    }
  }

  public String getErrorMessage() {
    getExecutionState();
    return (lastException == null ? null : lastException.getLocalizedMessage());
  }

  public String getUniqueId() {
    return (trigger == null ? null : trigger.getFullJobName());
  }

}
