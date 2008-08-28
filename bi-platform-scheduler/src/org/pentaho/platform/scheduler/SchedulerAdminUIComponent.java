/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
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
 * Created Aug 15, 2005 
 * @author wseyler
 */

package org.pentaho.platform.scheduler;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.ComponentException;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.WebServiceUtil;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.scheduler.messages.Messages;
import org.pentaho.platform.uifoundation.component.xml.XmlComponent;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * API description:
 * Base URL is: http://<servername>:<portnum>/<contextname>/SchedulerAdmin
 *  or
 *  http://localhost:8080/pentaho/SchedulerAdmin
 *  
 * deleteJob:
 *  schedulerAction=deleteJob&jobName=PentahoSystemVersionCheck&jobGroup=DEFAULT
 *  
 * executeJobNow:
 *  schedulerAction=executeJob&jobName=PentahoSystemVersionCheck&jobGroup=DEFAULT
 *  
 * getJobNames:
 *  schedulerAction=getJobNames
 *  
 * isSchedulerPaused:
 *  schedulerAction=isSchedulerPaused
 *  
 * pauseAll:
 *  schedulerAction=suspendScheduler
 *  
 * pauseJob:
 *  schedulerAction=pauseJob&jobName=PentahoSystemVersionCheck&jobGroup=DEFAULT
 *  
 * resumeAll:
 *  schedulerAction=resumeScheduler
 *  
 * resumeJob:
 *  schedulerAction=resumeJob&jobName=PentahoSystemVersionCheck&jobGroup=DEFAULT
 * 
 * TODO sbarkdull, add API for create and update
 * 
 * NOTE: the term "Job" is somewhat misused in much of this file. Often where you see
 * the term "Job", it means "Schedule". A Schedule is a combination of a trigger
 * and a Job. A trigger is essentially a time to execute something, a Job is
 * essentially the set of things to execute when a trigger fires. 
 * 
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SchedulerAdminUIComponent extends XmlComponent {
  /**
   * 
   */
  private static final long serialVersionUID = 2963902264708970014L;

  private static final String JOB = "job"; //$NON-NLS-1$

  private static final String JOB_NAME = "jobName"; //$NON-NLS-1$

  private static final String RESULT = "schedulerResults"; //$NON-NLS-1$

  private static final String ERROR_NODE_NAME = "error"; //$NON-NLS-1$

  private static final String MSG_ATTR_NAME = "msg"; //$NON-NLS-1$

  private static final String JOB_GROUP = "jobGroup"; //$NON-NLS-1$

  public static final String RESUME_SCHEDULER_ACTION_STR = "resumeScheduler"; //$NON-NLS-1$

  public static final String SUSPEND_SCHEDULER_ACTION_STR = "suspendScheduler"; //$NON-NLS-1$

  public static final String GET_JOB_NAMES_ACTION_STR = "getJobNames"; //$NON-NLS-1$

  public static final String GET_IS_SCHEDULER_PAUSED_ACTION_STR = "isSchedulerPaused"; //$NON-NLS-1$

  public static final String PAUSE_JOB_ACTION_STR = "pauseJob"; //$NON-NLS-1$

  public static final String RESUME_JOB_ACTION_STR = "resumeJob"; //$NON-NLS-1$

  public static final String DELETE_JOB_ACTION_STR = "deleteJob"; //$NON-NLS-1$

  public static final String SCHEDULER_ACTION_STR = "schedulerAction"; //$NON-NLS-1$

  public static final String RUN_JOB_ACTION_STR = "executeJob"; //$NON-NLS-1$

  public static final String CREATE_JOB_ACTION_STR = "createJob"; //$NON-NLS-1$

  public static final String UPDATE_JOB_ACTION_STR = "updateJob"; //$NON-NLS-1$

  private Scheduler sched = null;

  private static final Log logger = LogFactory.getLog(SchedulerAdminUIComponent.class);

  /**
   * @param urlFactory
   */
  public SchedulerAdminUIComponent(IPentahoUrlFactory urlFactory, List<String> messages) {
    super(urlFactory, messages, null);
    try {
      sched = QuartzSystemListener.getSchedulerInstance();
    } catch (Exception e) {
      error(Messages
          .getString(Messages.getErrorString("SchedulerAdminUIComponent.ERROR_0002_NoScheduler") + e.toString())); //$NON-NLS-1$
      e.printStackTrace();
    }
    setXsl("text/html", "SchedulerAdmin.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.PentahoBase#getLogger()
   */
  public Log getLogger() {
    return logger;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.ui.component.BaseUIComponent#validate()
   */
  public boolean validate() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.ui.component.BaseUIComponent#getXmlContent()
   */
  public Document getXmlContent() {
    String schedulerActionStr = getParameter(SCHEDULER_ACTION_STR, GET_JOB_NAMES_ACTION_STR);
    setXslProperty("baseUrl", urlFactory.getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$ 

    try {
      if (SUSPEND_SCHEDULER_ACTION_STR.equals(schedulerActionStr)) {
        return doPauseAll();
      } else if (RESUME_SCHEDULER_ACTION_STR.equals(schedulerActionStr)) {
        return doResumeAll();
      } else if (GET_JOB_NAMES_ACTION_STR.equals(schedulerActionStr)) {
        return doGetJobNames();
      } else if (GET_IS_SCHEDULER_PAUSED_ACTION_STR.equals(schedulerActionStr)) {
        return doIsSchedulerPaused();
      } else if (PAUSE_JOB_ACTION_STR.equalsIgnoreCase(schedulerActionStr)) {
        return doPauseJob();
      } else if (RESUME_JOB_ACTION_STR.equalsIgnoreCase(schedulerActionStr)) {
        return doResumeJob();
      } else if (DELETE_JOB_ACTION_STR.equalsIgnoreCase(schedulerActionStr)) {
        return doDeleteJob();
      } else if (RUN_JOB_ACTION_STR.equalsIgnoreCase(schedulerActionStr)) {
        return doExecuteJobNow();
      } else if (CREATE_JOB_ACTION_STR.equalsIgnoreCase(schedulerActionStr)) {
        return doCreateJob();
      } else if (UPDATE_JOB_ACTION_STR.equalsIgnoreCase(schedulerActionStr)) {
        return doUpdateJob();
      } else {
        Document document = DocumentHelper.createDocument();
        document.setName(SCHEDULER_ACTION_STR);

        return document;
        // returns a blank document if
        // nothing else executed.
        /*
         * TODO Create some sort of document to display when the default
         * action occurs.
         */
      }
    } catch (ComponentException e) {
      // TODO sbarkdull, lame attempt to start to get some error info returned to caller,
      // should be much more robust, and clients need to be coded to respond to error xml message
      logger.error( e.getMessage() );
      String strXml = WebServiceUtil.getErrorXml( e.getMessage() );
      Document document;
      document = XmlDom4JHelper.getDocFromString( strXml, null );
      return document;
    }
  }

  private Document doExecuteJobNow() throws ComponentException {
    String jobName = getParameter(JOB_NAME, ""); //$NON-NLS-1$
    String groupName = getParameter(JOB_GROUP, ""); //$NON-NLS-1$ 
    Trigger trigger = new SimpleTrigger("Immediate", "DEFAULT"); //$NON-NLS-1$ //$NON-NLS-2$

    try {
      JobDetail jobDetail = sched.getJobDetail(jobName, groupName);
      if (jobDetail == null) {
        throw new ComponentException("Failed to execute job {0}. Job with that name does not exist in scheduler. "
            + jobName);
      } else {
        jobDetail.setGroup("Immediate"); //$NON-NLS-1$
        sched.scheduleJob(jobDetail, trigger);
      }
    } catch (SchedulerException e) {
      throw new ComponentException("Failed to execute job {0}." + jobName, e);
    }

    return doGetJobNames();
  }

  private Document doCreateJob() {

    IBackgroundExecution helper = PentahoSystem.getBackgroundExecutionHandler(getSession());
    String strReturn = helper.backgroundExecuteAction(getSession(), (IParameterProvider) getParameterProviders().get(
        IParameterProvider.SCOPE_REQUEST));

    String strXml = WebServiceUtil.getStatusXml("ok");
    Document d = XmlDom4JHelper.getDocFromString(strXml, null);
    return d;
  }

  private Document doUpdateJob() throws ComponentException {

    String jobName = getParameter("oldJobName", null); //$NON-NLS-1$
    String groupName = getParameter("oldJobGroup", null); //$NON-NLS-1$

    if (null == jobName || null == groupName) {
      throw new ComponentException(Messages.getErrorString("SchedulerAdminUIComponent.ERROR_0420_MISSING_PARAMS")); //$NON-NLS-1$
    }
    try {
      sched.deleteJob(jobName, groupName);
    } catch (SchedulerException e) {
      throw new ComponentException(Messages.getErrorString(
          "SchedulerAdminUIComponent.ERROR_0421_FAILED_TO_UPDATE", jobName, groupName)); //$NON-NLS-1$
    }
    /*
    JobDetail jd = sched.getJobDetail( jobName, groupName );
    Trigger t = sched.getTrigger(jobName, groupName);
    sched.unscheduleJob( jobName, groupName );
    sched.scheduleJob(jd, t);
    */

    IBackgroundExecution helper = PentahoSystem.getBackgroundExecutionHandler(getSession());
    String strReturn = helper.backgroundExecuteAction(getSession(), (IParameterProvider) getParameterProviders().get(
        IParameterProvider.SCOPE_REQUEST));

    String strXml = WebServiceUtil.getStatusXml("ok");
    Document d = XmlDom4JHelper.getDocFromString(strXml, null);
    return d;
  }

  private Document doDeleteJob() throws ComponentException {
    String jobName = getParameter(JOB_NAME, ""); //$NON-NLS-1$
    String groupName = getParameter(JOB_GROUP, ""); //$NON-NLS-1$
    try {
      sched.deleteJob(jobName, groupName);
    } catch (SchedulerException e) {
      throw new ComponentException(Messages.getErrorString(
          "SchedulerAdminUIComponent.ERROR_0422_FAILED_TO_DELETE", jobName, groupName), e); //$NON-NLS-1$
    }

    return doGetJobNames();
  }

  private Document doResumeJob() throws ComponentException {
    String jobName = getParameter(JOB_NAME, ""); //$NON-NLS-1$
    String groupName = getParameter(JOB_GROUP, ""); //$NON-NLS-1$
    try {
      sched.resumeJob(jobName, groupName);
    } catch (SchedulerException e) {
      throw new ComponentException("Failed to resume job {0}." + jobName, e);
    }

    return doGetJobNames();
  }

  private Document doPauseJob() throws ComponentException {
    String jobName = getParameter(JOB_NAME, ""); //$NON-NLS-1$
    String groupName = getParameter(JOB_GROUP, ""); //$NON-NLS-1$
    try {
      sched.pauseJob(jobName, groupName);
    } catch (SchedulerException e) {
      throw new ComponentException("Failed to pause job {0}." + jobName, e);
    }
    return doGetJobNames();
  }

  /**
   * @return
   * @throws ComponentException 
   */
  private Document doIsSchedulerPaused() throws ComponentException {
    Document document = DocumentHelper.createDocument();
    document.setName(SCHEDULER_ACTION_STR);
    Element root = document.addElement(getParameter(SCHEDULER_ACTION_STR, "")); //$NON-NLS-1$
    try {
      boolean isInStandby = sched.isInStandbyMode();
      root
          .addAttribute(
              RESULT,
              isInStandby ? Messages.getString("SchedulerAdminUIComponent.USER_isPaused") : Messages.getString("SchedulerAdminUIComponent.USER_isRunning")); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (SchedulerException e) {
      throw new ComponentException("Failed to determine if scheduler is paused. ", e);
    }
    return document;
  }

  /**
   * @return
   */
  private Document doGetJobNames() {
    Document document = DocumentHelper.createDocument();
    document.setName(SCHEDULER_ACTION_STR);
    Element root = document.addElement(GET_JOB_NAMES_ACTION_STR);
    try {
      String[] triggerGroups = sched.getTriggerGroupNames();
      for (int i = 0; i < triggerGroups.length; i++) {
        String[] triggerNames = sched.getTriggerNames(triggerGroups[i]);
        for (int j = 0; j < triggerNames.length; j++) {
          Element job = root.addElement(JOB);
          try {
            job
                .addAttribute(
                    "triggerState", Integer.toString(sched.getTriggerState(triggerNames[j], triggerGroups[i]))); //$NON-NLS-1$

            Trigger trigger = sched.getTrigger(triggerNames[j], triggerGroups[i]);

            job.addAttribute("triggerName", trigger.getName()); //$NON-NLS-1$
            job.addAttribute("triggerGroup", trigger.getGroup()); //$NON-NLS-1$
            Date date = trigger.getNextFireTime();
            job
                .addAttribute(
                    "nextFireTime", (date == null) ? Messages.getString("SchedulerAdminUIComponent.USER_NEVER") : date.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            date = trigger.getPreviousFireTime();
            job
                .addAttribute(
                    "prevFireTime", (date == null) ? Messages.getString("SchedulerAdminUIComponent.USER_NEVER") : date.toString()); //$NON-NLS-1$ //$NON-NLS-2$

            // get the job info
            job.addAttribute(JOB_NAME, trigger.getJobName());
            job.addAttribute(JOB_GROUP, trigger.getJobGroup());
            JobDetail jobDetail = sched.getJobDetail(trigger.getJobName(), trigger.getJobGroup());

            job.addElement("description").addCDATA(jobDetail.getDescription()); //$NON-NLS-1$

            DateFormat dateTimeFormatter = getDateTimeFormatter();
            Date d = trigger.getStartTime();
            if (null != d) {
              String startDate = dateTimeFormatter.format(d);
              job.addAttribute(StandardSettings.START_DATE_TIME, startDate);
            }
            d = trigger.getEndTime();
            if (null != d) {
              String endDate = dateTimeFormatter.format(d);
              job.addAttribute(StandardSettings.END_DATE_TIME, endDate);
            }

            if (trigger instanceof CronTrigger) {
              job.addAttribute(StandardSettings.CRON_STRING, ((CronTrigger) trigger).getCronExpression());
            } else if (trigger instanceof SimpleTrigger) {
              long repeatInSecs = ((SimpleTrigger) trigger).getRepeatInterval();
              job.addAttribute(StandardSettings.REPEAT_TIME_MILLISECS, Long.toString(repeatInSecs));
            } else {
              throw new RuntimeException(Messages.getErrorString(
                  "SchedulerAdminUIComponent.ERROR_0423_UNRECOGNIZED_TRIGGER", trigger.getClass().getName())); //$NON-NLS-1$
            }

            JobDataMap m = jobDetail.getJobDataMap();
            if (null != m.getString(StandardSettings.ACTION)) {
              ActionInfo actionInfo = new ActionInfo(m.getString(StandardSettings.SOLUTION), m
                  .getString(StandardSettings.PATH), m.getString(StandardSettings.ACTION));
              job.addAttribute(StandardSettings.ACTIONS_REFS, actionInfo.toString());
            }

            // job.addAttribute("class",
            // jobDetail.getClass().getName()); //$NON-NLS-1$
          } catch (RuntimeException e) {
            throw e;
          } catch (Exception e) {
            job.addElement("description").addCDATA(e.getMessage()); //$NON-NLS-1$
            job.addAttribute("triggerState", "3"); // ERROR //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
      }
    } catch (SchedulerException e) {
      String msg = Messages.getErrorString("SchedulerAdminUIComponent.ERROR_0001_ErrorInScheduler") + e.toString();
      error(msg);
      root.addAttribute(RESULT, msg);
      addErrorElementToDocument(document, msg);
    }

    return document;
  }

  /**
   * This formatter works with a date/time string with this format:
   * May 21, 2008 8:29:21 PM
   * 
   * NOTE: the formatter cannot be shared across threads (since DateFormat implementations
   * are not guaranteed to be thread safe) or across sessions (since different 
   * sessions may have different locales). So create a new one an each call.
   * @return
   */
  private DateFormat getDateTimeFormatter() {
    return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, LocaleHelper.getLocale());
  }

  /**
   * @return
   * @throws ComponentException 
   */
  private Document doResumeAll() throws ComponentException {
    Document document = DocumentHelper.createDocument();
    document.setName(SCHEDULER_ACTION_STR);
    Element root = document.addElement(getParameter(SCHEDULER_ACTION_STR, "")); //$NON-NLS-1$
    try {
      sched.resumeAll();
      root.addAttribute(RESULT, Messages.getString("SchedulerAdminUIComponent.USER_JobsResumed")); //$NON-NLS-1$
    } catch (SchedulerException e) {
      throw new ComponentException(Messages.getErrorString("SchedulerAdminUIComponent.ERROR_0001_ErrorInScheduler")
          + e.toString());
    }

    return document;
  }

  /**
   * @return
   */
  private Document doPauseAll() throws ComponentException {
    Document document = DocumentHelper.createDocument();
    document.setName(SCHEDULER_ACTION_STR);
    Element root = document.addElement(getParameter(SCHEDULER_ACTION_STR, "")); //$NON-NLS-1$
    try {
      sched.pauseAll();
      root.addAttribute(RESULT, Messages.getString("SchedulerAdminUIComponent.USER_JobsSuspended")); //$NON-NLS-1$
    } catch (SchedulerException e) {
      throw new ComponentException(Messages.getErrorString("SchedulerAdminUIComponent.ERROR_0001_ErrorInScheduler")
          + e.toString());
    }

    return document;
  }

  private static void addErrorElementToDocument(Document document, String msg) {
    Element parentElem = document.getRootElement();
    Element error = parentElem.addElement(ERROR_NODE_NAME);
    error.addAttribute(MSG_ATTR_NAME, StringEscapeUtils.escapeXml(msg));
  }
}
