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
*/
package org.pentaho.platform.scheduler;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IScheduler;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.scheduler.messages.Messages;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

public class QuartzScheduler implements IScheduler {

  protected static final Log logger = LogFactory.getLog(QuartzScheduler.class);

  /**
   * NOTE: this method does not support non-cron schedules.
   * @deprecated method never called, except in test classes (sbarkdull). It is
   * also likely that setting the group name to "Subscription Group" is no longer correct, etc.
   */
  public boolean scheduleSubscription(final ISubscription subscription) {
    try {
      Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();

      // String jobAction = null;
      ISubscribeContent subContent = subscription.getContent();
      String solutionName = subContent.getActionReference();
      String solution = solutionName.substring(0, solutionName.indexOf('/'));
      String action = solutionName.substring(solutionName.lastIndexOf('/') + 1);
      String path = solutionName.substring(solution.length() + 1, solutionName.length() - action.length() - 1);

      String jobName = subscription.getUser() + " : " + subscription.getTitle(); //$NON-NLS-1$
      JobDetail jobDetail = new JobDetail(jobName, "Subscription Group", QuartzExecute.class); //$NON-NLS-1$
      jobDetail.getJobDataMap().put("solution", solution); //$NON-NLS-1$
      jobDetail.getJobDataMap().put("path", path); //$NON-NLS-1$
      jobDetail.getJobDataMap().put("action", action); //$NON-NLS-1$

      jobDetail.getJobDataMap().putAll(subscription.getParameters());

      List scheduleList = subscription.getSchedules();
      for (int i = 0; i < scheduleList.size(); ++i) {
        ISchedule schedule = (ISchedule) scheduleList.get(i);
        if ( schedule.isRepeatSchedule() ) {
          throw new IllegalStateException( Messages.getErrorString("QuartzScheduler.ERROR_421_DOES_NOT_SUPPORT_REPEAT_SCHEDULES") ); //$NON-NLS-1$
        }
        Trigger trigger = new CronTrigger(schedule.getScheduleReference(),
            "Subscription Group", schedule.getCronString()); //$NON-NLS-1$
        trigger.setMisfireInstruction(Trigger.MISFIRE_INSTRUCTION_SMART_POLICY);
        if (scheduler.getJobDetail(jobDetail.getName(), "Subscription Group") != null) { //$NON-NLS-1$
          scheduler.deleteJob(jobDetail.getName(), "Subscription Group"); //$NON-NLS-1$
        }
        scheduler.scheduleJob(jobDetail, trigger);
      }
    } catch (ParseException pe) {
      QuartzScheduler.logger.error(null, pe);
      return (false);
    } catch (SchedulerException se) {
      QuartzScheduler.logger.error(null, se);
      return (false);
    } catch (Exception e) {
      QuartzScheduler.logger.error(null, e);
      return (false);
    }

    return (true);

  }

}
