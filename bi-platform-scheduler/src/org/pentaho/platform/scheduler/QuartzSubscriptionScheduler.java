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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IScheduledJob;
import org.pentaho.platform.api.engine.ISubscriptionScheduler;
import org.pentaho.platform.api.engine.SubscriptionSchedulerException;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.scheduler.messages.Messages;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

// TODO sbarkdull, this class should be logging, possibly at the info level,
// all operations like create, update, delete, suspend, and resume, and include the time the operation occurred.
/**
 * Provides the interface between the Quartz Scheduling component and the Subscription
 * Subsystem
 * 
 * @author dmoran
 * 
 */
public class QuartzSubscriptionScheduler implements ISubscriptionScheduler {

  protected static final Log logger = LogFactory.getLog(QuartzSubscriptionScheduler.class);

  public static final String GROUP_NAME = Messages.getString("QuartzSubscriptionScheduler.GROUP_NAME");

  private static final int PAUSE = 0;

  private static final int RESUME = 1;

  private static final int EXECUTE = 2;

  private static final int DELETE = 3;

  private static final String exceptionMessages[] = {
      Messages.getString("QuartzSubscriptionScheduler.USER_UNABLE_TO_PAUSE"), //$NON-NLS-1$
      Messages.getString("QuartzSubscriptionScheduler.USER_UNABLE_TO_RESUME"), //$NON-NLS-1$
      Messages.getString("QuartzSubscriptionScheduler.USER_UNABLE_TO_EXECUTE"), //$NON-NLS-1$
      Messages.getString("QuartzSubscriptionScheduler.USER_UNABLE_TO_DELETE"), //$NON-NLS-1$
  };

  // TODO sbarkdull, clean up the exception communication
  /**
   * Synchronizes The Scheduler schedule with the subscription schedule.  Returns the scheduled job or null
   * if the job was deleted
   * 
   * @throws SubscriptionSchedulerException 
   */
  // TODO sbarkdull really need to throw a SchedulerBadCronStringException when Cron string is bad,
  // will disambiguate for the client why the exception was throw
  public IScheduledJob syncSchedule(final String oldScheduleReference, final ISchedule newSchedule) throws SubscriptionSchedulerException {
    try {
        Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
    
        if (oldScheduleReference != null) {
          scheduler.deleteJob(oldScheduleReference, QuartzSubscriptionScheduler.GROUP_NAME);
        }
    
        // Delete
        if (newSchedule == null) {
          return (null);
        }
        Trigger trigger = createTriggerFromSchedule( newSchedule );
        trigger.setDescription(newSchedule.getGroup() + " : " + newSchedule.getDescription()); //$NON-NLS-1$
        trigger.setMisfireInstruction(Trigger.MISFIRE_INSTRUCTION_SMART_POLICY);
    
        // Delete just in case some old one was left lying around with this name
        scheduler.deleteJob(newSchedule.getScheduleReference(), QuartzSubscriptionScheduler.GROUP_NAME);
    
        JobDetail jobDetail = new JobDetail(newSchedule.getScheduleReference(), QuartzSubscriptionScheduler.GROUP_NAME,
            QuartzSubscriptionJob.class);
        jobDetail.setDescription(newSchedule.getGroup() + " : " + newSchedule.getDescription()); //$NON-NLS-1$
        scheduler.scheduleJob(jobDetail, trigger);
        return (new QuartzScheduledJob(trigger));
    } catch (SchedulerException e ) {
      throw new SubscriptionSchedulerException( Messages.getErrorString("QuartzSubscriptionScheduler.ERROR_0421_SYNC_SCHED_FAILED0"), e ); //$NON-NLS-1$
    } catch (ParseException e ) {
      throw new SubscriptionSchedulerException( Messages.getErrorString("QuartzSubscriptionScheduler.ERROR_0422_SYNC_SCHED_FAILED1", newSchedule.getCronString() ), e ); //$NON-NLS-1$
    }
  }

  /**
   * Returns a list of exception messages
   */
  public List syncSchedule(final List newSchedules) throws Exception {
    List exceptionList = new ArrayList();
    if (newSchedules == null) {
      return (exceptionList);
    }

    Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
    HashSet jobSet = new HashSet(Arrays.asList(scheduler.getJobNames(QuartzSubscriptionScheduler.GROUP_NAME)));

    // Add/modify the good schedules
    for (int i = 0; i < newSchedules.size(); ++i) {
      ISchedule sched = (ISchedule) newSchedules.get(i);
      try {
        syncSchedule(sched.getScheduleReference(), sched);
      } catch (Throwable t) {
        exceptionList.add(Messages.getString(
            "QuartzSubscriptionScheduler.ERROR_SCHEDULING", sched.getScheduleReference(), t.getLocalizedMessage())); //$NON-NLS-1$
      }
      jobSet.remove(sched.getScheduleReference());
    }

    // Now delete the left overs
    for (Iterator it = jobSet.iterator(); it.hasNext();) {
      scheduler.deleteJob((String) it.next(), QuartzSubscriptionScheduler.GROUP_NAME);
    }

    return (exceptionList);
  }

  /**
   * NOTE: doesn't actually throw any checked exceptions
   * @throws SchedulerException 
   * @throws SubscriptionSchedulerException 
   */
  public Map<String,IScheduledJob> getScheduledJobMap() throws SchedulerException, SubscriptionSchedulerException {
    Map<String,IScheduledJob> jobMap = new HashMap<String,IScheduledJob>();

    Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
    String jobs[] = scheduler.getJobNames(QuartzSubscriptionScheduler.GROUP_NAME);
    for (String jobName : jobs) {
        Trigger t = scheduler.getTrigger(jobName, QuartzSubscriptionScheduler.GROUP_NAME);
        if ( null != t ) {
          jobMap.put(jobName, new QuartzScheduledJob(t));
        } else {
          throw new SubscriptionSchedulerException( Messages.getErrorString("QuartzSubscriptionScheduler.ERROR_0423_GET_JOB_MAP_FAILED", jobName ) ); //$NON-NLS-1$
        }
    }
    return (jobMap);
  }

  public IScheduledJob getScheduledJob(final String schedRef) throws SubscriptionSchedulerException {
    Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
    Trigger trigger;
    try {
      trigger = scheduler.getTrigger(schedRef, QuartzSubscriptionScheduler.GROUP_NAME);
    } catch (SchedulerException e) {
      throw new SubscriptionSchedulerException( Messages.getErrorString("QuartzSubscriptionScheduler.ERROR_0424_FAILED_TO_GET_JOB_WITH_NAME", schedRef ), e ); //$NON-NLS-1$
    }
    return (new QuartzScheduledJob( trigger ));
  }

  public List<QuartzScheduledJob> getScheduledJobs() {
    List<QuartzScheduledJob> jobList = new ArrayList<QuartzScheduledJob>();
    try {
      Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
      String jobs[] = scheduler.getJobNames(QuartzSubscriptionScheduler.GROUP_NAME);
      for (String element : jobs) {
        jobList.add(new QuartzScheduledJob(scheduler.getTrigger(element, QuartzSubscriptionScheduler.GROUP_NAME)));
      }
    } catch (SchedulerException se) {
      QuartzSubscriptionScheduler.logger.error(null, se);
    } catch (Exception e) {
      QuartzSubscriptionScheduler.logger.error(null, e);
    }
    return (jobList);
  }

  /**
   * 
   * @param cmd String the command
   * @param triggerName String the name of the trigger to apply the command to
   * @return
   * @throws Exception
   */
  private IScheduledJob doJob(final int cmd, final String triggerName) throws Exception {
    Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
    Trigger trigger = scheduler.getTrigger(triggerName, QuartzSubscriptionScheduler.GROUP_NAME);
    if (trigger == null) {
      throw new Exception(QuartzSubscriptionScheduler.exceptionMessages[cmd] + triggerName);
    }

    switch (cmd) {
      case PAUSE: {
        scheduler.pauseJob(triggerName, QuartzSubscriptionScheduler.GROUP_NAME);
        break;
      }
      case RESUME: {
        scheduler.resumeJob(triggerName, QuartzSubscriptionScheduler.GROUP_NAME);
        break;
      }
      case EXECUTE: {
        scheduler.triggerJob(triggerName, QuartzSubscriptionScheduler.GROUP_NAME);
        break;
      }
      case DELETE: {
        logger.error( Messages.getErrorString("QuartzSubscriptionScheduler.ERROR_0425_FAILED_TO_DELETE_SCHEDULE", triggerName ) ); //$NON-NLS-1$
        scheduler.deleteJob(triggerName, QuartzSubscriptionScheduler.GROUP_NAME);
        break;
      }
      default:
        return (null);
    }

    return (new QuartzScheduledJob(trigger));
  }

  public IScheduledJob pauseJob(final String jobName) throws Exception {
    return (doJob(QuartzSubscriptionScheduler.PAUSE, jobName));
  }

  public IScheduledJob resumeJob(final String jobName) throws Exception {
    return (doJob(QuartzSubscriptionScheduler.RESUME, jobName));
  }

  public IScheduledJob executeJob(final String jobName) throws Exception {
    return (doJob(QuartzSubscriptionScheduler.EXECUTE, jobName));
  }

  public IScheduledJob deleteJob(final String triggerName) throws Exception {
    return (doJob(QuartzSubscriptionScheduler.DELETE, triggerName));
  }

  public IScheduledJob scheduleJob(final ISchedule schedule) throws Exception {
    if (schedule == null) {
      return (null);
    }

    return (syncSchedule(schedule.getScheduleReference(), schedule));
  }

  public int getSchedulerState() throws Exception {
    Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
    if (scheduler.isInStandbyMode()) {
      return (IScheduledJob.STATE_PAUSED );
    } else if (scheduler.isShutdown()) {
      return (IScheduledJob.STATE_NONE );
    }

    return (IScheduledJob.STATE_NORMAL );
  }

  public void pauseScheduler() throws Exception {
    Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
    scheduler.standby();
  }

  public void resumeScheduler() throws Exception {
    Scheduler scheduler = QuartzSystemListener.getSchedulerInstance();
    scheduler.start();
  }

  public String getCronSummary(final String cron) throws Exception {
    return (new CronExpression(cron).getExpressionSummary());
  }

  // TODO belongs in a subscription schedule helper or util class
  /**
   * @throws ParseException if the schedule is a cron schedule, and the cron string is invalid
   */
  public static Trigger createTriggerFromSchedule( ISchedule sched ) throws ParseException {
    Trigger trigger = null;
    if ( sched.isCronSchedule() ) {
      trigger = new CronTrigger( sched.getScheduleReference(), QuartzSubscriptionScheduler.GROUP_NAME,
          sched.getCronString() );
    } else if ( sched.isRepeatSchedule() ) {
      int repeatCount = null == sched.getRepeatCount()
        ? SimpleTrigger.REPEAT_INDEFINITELY
        : sched.getRepeatCount();
      trigger = new SimpleTrigger( sched.getScheduleReference(), QuartzSubscriptionScheduler.GROUP_NAME,
          repeatCount, sched.getRepeatInterval() );
    } else {
      throw new IllegalStateException( Messages.getErrorString("QuartzSubscriptionScheduler.ERROR_0420_MISSING_CRON_AND_REPEAT_INTERVAL", sched.getId() ) ); //$NON-NLS-1$
    }
    if ( null != sched.getStartDate() ) {
      trigger.setStartTime( sched.getStartDate() );
    }
    if ( null != sched.getEndDate() ) {
      trigger.setEndTime( sched.getEndDate() );
    }
    return trigger;
  }
}
