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
