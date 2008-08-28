package org.pentaho.platform.scheduler;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler.IJobSchedule;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class SchedulerHelper {
  public static List<IJobSchedule> getMySchedules(IPentahoSession session) throws SchedulerException {
    try {
      List<IJobSchedule> jobSchedules = new ArrayList<IJobSchedule>();
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      String[] triggerGroups = sched.getTriggerGroupNames();
      for (String triggerGroup : triggerGroups) {
        String[] triggerNames = sched.getTriggerNames(triggerGroup);
        for (String triggerName : triggerNames) {
          JobSchedule jobSchedule = new JobSchedule();
          Trigger trigger = sched.getTrigger(triggerName, triggerGroup);
          // filter on group = my userid
          if (trigger.getJobGroup().equalsIgnoreCase(session.getName())) {
            org.quartz.JobDetail jobDetail = sched.getJobDetail(trigger.getJobName(), trigger.getJobGroup());
            jobSchedule.setName(jobDetail.getJobDataMap().getString("background_action_name"));
            jobSchedule.setFullname(jobDetail.getJobDataMap().getString("background_action_name"));
            jobSchedule.setTriggerState(sched.getTriggerState(triggerName, triggerGroup));
            jobSchedule.setTriggerName(trigger.getName()); //$NON-NLS-1$
            jobSchedule.setTriggerGroup(trigger.getGroup()); //$NON-NLS-1$
            jobSchedule.setNextFireTime(trigger.getNextFireTime());
            jobSchedule.setPreviousFireTime(trigger.getPreviousFireTime());
            jobSchedule.setJobName(trigger.getJobName());
            jobSchedule.setJobGroup(trigger.getJobGroup());
            jobSchedule.setJobDescription(jobDetail.getDescription());
            jobSchedules.add(jobSchedule);
          }
        }
      }
      return jobSchedules;
    } catch (org.quartz.SchedulerException se) {
      throw new SchedulerException(se);
    }
  }

  public static List<IJobSchedule> getAllSchedules(IPentahoSession session) throws SchedulerException {
    List<IJobSchedule> jobSchedules = new ArrayList<IJobSchedule>();
    if (SecurityHelper.isPentahoAdministrator(session)) {
      try {
        Scheduler sched = QuartzSystemListener.getSchedulerInstance();
        String[] triggerGroups = sched.getTriggerGroupNames();
        for (String triggerGroup : triggerGroups) {
          String[] triggerNames = sched.getTriggerNames(triggerGroup);
          for (String triggerName : triggerNames) {
            JobSchedule jobSchedule = new JobSchedule();
            Trigger trigger = sched.getTrigger(triggerName, triggerGroup);
            org.quartz.JobDetail jobDetail = sched.getJobDetail(trigger.getJobName(), trigger.getJobGroup());
            jobSchedule.setName(jobDetail.getJobDataMap().getString("background_action_name"));
            jobSchedule.setFullname(jobDetail.getJobDataMap().getString("background_action_name"));
            jobSchedule.setTriggerState(sched.getTriggerState(triggerName, triggerGroup));
            jobSchedule.setTriggerName(trigger.getName()); //$NON-NLS-1$
            jobSchedule.setTriggerGroup(trigger.getGroup()); //$NON-NLS-1$
            jobSchedule.setNextFireTime(trigger.getNextFireTime());
            jobSchedule.setPreviousFireTime(trigger.getPreviousFireTime());
            jobSchedule.setJobName(trigger.getJobName());
            jobSchedule.setJobGroup(trigger.getJobGroup());
            jobSchedule.setJobDescription(jobDetail.getDescription());
            jobSchedules.add(jobSchedule);
          }
        }
      } catch (org.quartz.SchedulerException se) {
        throw new SchedulerException(se);
      }
    }
    return jobSchedules;
  }

  public static void runJob(IPentahoSession session, String jobName, String jobGroup) throws SchedulerException {
    // only if owner or admin of job can job be run
    if (SecurityHelper.isPentahoAdministrator(session) || jobGroup.equalsIgnoreCase(session.getName())) {
      try {
        Scheduler sched = QuartzSystemListener.getSchedulerInstance();
        sched.triggerJob(jobName, jobGroup);
      } catch (org.quartz.SchedulerException se) {
        try {
          Scheduler sched = QuartzSystemListener.getSchedulerInstance();
          sched.triggerJobWithVolatileTrigger(jobName, jobGroup);
        } catch (org.quartz.SchedulerException se2) {
          throw new SchedulerException(se2);
        }
      }
    }
  }

  public static void resumeJob(IPentahoSession session, String jobName, String jobGroup) throws SchedulerException {
    // only if owner or admin of job can job be resumed
    if (SecurityHelper.isPentahoAdministrator(session) || jobGroup.equalsIgnoreCase(session.getName())) {
      try {
        Scheduler sched = QuartzSystemListener.getSchedulerInstance();
        sched.resumeJob(jobName, jobGroup);
      } catch (org.quartz.SchedulerException se) {
        throw new SchedulerException(se);
      }
    }
  }

  public static void suspendJob(IPentahoSession session, String jobName, String jobGroup) throws SchedulerException {
    // only if owner or admin of job can job be resumed
    if (SecurityHelper.isPentahoAdministrator(session) || jobGroup.equalsIgnoreCase(session.getName())) {
      try {
        Scheduler sched = QuartzSystemListener.getSchedulerInstance();
        sched.pauseJob(jobName, jobGroup);
      } catch (org.quartz.SchedulerException se) {
        throw new SchedulerException(se);
      }
    }
  }

  public static void deleteJob(IPentahoSession session, String jobName, String jobGroup) throws SchedulerException {
    // only if owner or admin of job can job be resumed
    if (SecurityHelper.isPentahoAdministrator(session) || jobGroup.equalsIgnoreCase(session.getName())) {
      try {
        Scheduler sched = QuartzSystemListener.getSchedulerInstance();
        sched.deleteJob(jobName, jobGroup);
      } catch (org.quartz.SchedulerException se) {
        throw new SchedulerException(se);
      }
    }
  }

  public static void createCronJob(IPentahoSession session, String solutionName, String path, String actionName,
      String triggerName, String group, String description, String cronExpression) {
    // Seconds 0-59 , - * /
    // Minutes 0-59 , - * /
    // Hours 0-23 , - * /
    // Day-of-month 1-31 , - * ? / L W C
    // Month 1-12 or JAN-DEC , - * /
    // Day-of-Week 1-7 or SUN-SAT , - * ? / L C #
    // Year (Optional) empty, 1970-2099 , - * /
    try {
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      org.quartz.JobDetail jobDetail = new org.quartz.JobDetail(actionName, session.getName(), QuartzExecute.class);
      jobDetail.setDescription(description);
      Trigger trigger = new CronTrigger(triggerName, group, cronExpression);
      JobDataMap jdm = jobDetail.getJobDataMap();
      jdm.put("solution", solutionName); //$NON-NLS-1$
      jdm.put("path", path); //$NON-NLS-1$
      jdm.put("action", actionName); //$NON-NLS-1$

      // Support scheduled actions in the portal and assigning the credential properly in the execute.
      if ((session != null) && (session.isAuthenticated())) {
        jdm.put("username", session.getName()); //$NON-NLS-1$
      }
      if (sched.getJobDetail(jobDetail.getName(), Scheduler.DEFAULT_GROUP) != null) {
        SchedulerHelper.deleteJob(session, jobDetail.getName(), Scheduler.DEFAULT_GROUP);
      }
      sched.scheduleJob(jobDetail, trigger);
    } catch (ParseException e) {
      throw new SchedulerException(e);
    } catch (org.quartz.SchedulerException se) {
      throw new SchedulerException(se);
    }
  }
  
  public static void createCronJob(IPentahoSession session, String solutionName, String path, String actionName,
      String cronExpression) {
    createCronJob(session, solutionName, path, actionName, actionName, session.getName(), "", cronExpression);
  }
  
  public static void createSimpleTriggerJob(IPentahoSession session, String solutionName, String path, String actionName, 
      String triggerName, String group, String description, Date strStartDate, Date strEndDate, int repeatCount, int strRepeatInterval) {
    try {
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      org.quartz.JobDetail jobDetail = new org.quartz.JobDetail(actionName, session.getName(), QuartzExecute.class);
      jobDetail.setDescription(description);
      SimpleTrigger trigger = createRepeatTrigger(triggerName, group, strStartDate, strEndDate, repeatCount, strRepeatInterval);
      JobDataMap jdm = jobDetail.getJobDataMap();
      jdm.put("solution", solutionName); //$NON-NLS-1$
      jdm.put("path", path); //$NON-NLS-1$
      jdm.put("action", actionName); //$NON-NLS-1$

      // Support scheduled actions in the portal and assigning the credential properly in the execute.
      if ((session != null) && (session.isAuthenticated())) {
        jdm.put("username", session.getName()); //$NON-NLS-1$
      }
      if (sched.getJobDetail(jobDetail.getName(), Scheduler.DEFAULT_GROUP) != null) {
        SchedulerHelper.deleteJob(session, jobDetail.getName(), Scheduler.DEFAULT_GROUP);
      }
      sched.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException e) {
      throw e;
    } catch (org.quartz.SchedulerException se) {
      throw new SchedulerException(se);
    }
  }

  public static void executeJobNow(String jobName, String jobGroup) throws SchedulerException {
    try {
      Trigger trigger = new SimpleTrigger("Immediate", "DEFAULT"); //$NON-NLS-1$ //$NON-NLS-2$
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      JobDetail jobDetail = sched.getJobDetail(jobName, jobGroup);
      if (jobDetail == null) {
        throw new SchedulerException("Trying to execute a job that doesn't exist" + jobName); //$NON-NLS-1$
      } else {
        jobDetail.setGroup("Immediate"); //$NON-NLS-1$
        sched.scheduleJob(jobDetail, trigger);
      }
    } catch (org.quartz.SchedulerException se) {
      throw new SchedulerException(se);
    }
  }

  public static boolean isInStandbyMode() {
    try {
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      return sched.isInStandbyMode();
    } catch (org.quartz.SchedulerException se) {
      throw new SchedulerException(se);
    }
  }

  public static void resumeAll() {
    try {
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      sched.resumeAll();
    } catch (org.quartz.SchedulerException se) {
      throw new SchedulerException(se);
    }
  }

  public static void pauseAll() {
    try {
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      sched.pauseAll();
    } catch (org.quartz.SchedulerException se) {
      throw new SchedulerException(se);
    }
  }
  
  public static CronTrigger createCronTrigger( String triggerName, String triggerGroupName, 
      String strStartDate, String strEndDate, String cronString ) throws ParseException {

    DateFormat fmtr = SubscriptionHelper.getDateTimeFormatter();

    Date startDate = ( null != strStartDate ) ? fmtr.parse( strStartDate ) : null;
    Date endDate = ( null != strEndDate ) ? fmtr.parse( strEndDate ) : null;
    CronTrigger trigger = new CronTrigger( triggerName, triggerGroupName, cronString );
    if ( null != startDate ) {
      trigger.setStartTime( startDate );
    }
    if ( null != endDate ) {
      trigger.setEndTime( endDate );
    }
    return trigger;
  }

  public static SimpleTrigger createRepeatTrigger(String triggerName, String triggerGroupName, Date startDate, Date endDate, int repeatCount, int repeatInterval) {
    
    SimpleTrigger trigger = new SimpleTrigger( triggerName, triggerGroupName, repeatCount, repeatInterval );
    if ( null != startDate ) {
      trigger.setStartTime( startDate );
    }
    if ( null != endDate ) {
      trigger.setEndTime( endDate );
    }
    return trigger;
  }
  /**
   * 
   * @param triggerName
   * @param triggerGroupName
   * @param strStartDate
   * @param strEndDate
   * @param repeatCount
   * @param strRepeatInterval repeat interval in milliseconds
   * @return
   * @throws ParseException
   */
  public static SimpleTrigger createRepeatTrigger( String triggerName, String triggerGroupName, 
      String strStartDate, String strEndDate, String repeatCount, String strRepeatInterval ) throws ParseException {

    DateFormat fmtr = SubscriptionHelper.getDateTimeFormatter();
    Date startDate = ( null != strStartDate ) ? fmtr.parse( strStartDate ) : null;
    Date endDate = ( null != strEndDate ) ? fmtr.parse( strEndDate ) : null;
    int repeatInterval = Integer.parseInt( strRepeatInterval );
    int intRepeatCount = ( null == repeatCount )
      ? SimpleTrigger.REPEAT_INDEFINITELY
      : Integer.parseInt( repeatCount );
    return createRepeatTrigger(triggerName, triggerGroupName, startDate, endDate, intRepeatCount, repeatInterval);
  }
}
