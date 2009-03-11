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
 *
 * Created Aug 1, 2005 
 * @author wseyler
 */

package org.pentaho.platform.scheduler.action;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.actions.AbstractJobSchedulerAction;
import org.pentaho.actionsequence.dom.actions.DeleteScheduledJobAction;
import org.pentaho.actionsequence.dom.actions.ResumeScheduledJobAction;
import org.pentaho.actionsequence.dom.actions.StartScheduledJobAction;
import org.pentaho.actionsequence.dom.actions.SuspendScheduledJobAction;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.scheduler.QuartzExecute;
import org.pentaho.platform.scheduler.QuartzSystemListener;
import org.pentaho.platform.scheduler.messages.Messages;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class JobSchedulerComponent extends ComponentBase {

  /**
   * 
   */
  private static final long serialVersionUID = -1770772140985331431L;

  private static final String USER_STR = "username"; //$NON-NLS-1$

  // Trigger misfire instructions
  private static final String MISFIRE_POLICY = "misfirePolicy"; //$NON-NLS-1$

  private static final String INSTRUCTION_NOOP = "INSTRUCTION_NOOP"; //$NON-NLS-1$

  private static final String INSTRUCTION_RE_EXECUTE_JOB = "INSTRUCTION_RE_EXECUTE_JOB"; //$NON-NLS-1$

  private static final String INSTRUCTION_DELETE_TRIGGER = "INSTRUCTION_DELETE_TRIGGER"; //$NON-NLS-1$

  private static final String INSTRUCTION_SET_TRIGGER_COMPLETE = "INSTRUCTION_SET_TRIGGER_COMPLETE"; //$NON-NLS-1$

  private static final String INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE = "INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE"; //$NON-NLS-1$

  private static final String MISFIRE_INSTRUCTION_SMART_POLICY = "MISFIRE_INSTRUCTION_SMART_POLICY"; //$NON-NLS-1$

  // Simple Trigger misfire instructions
  private static final String MISFIRE_INSTRUCTION_FIRE_NOW = "MISFIRE_INSTRUCTION_FIRE_NOW"; //$NON-NLS-1$

  private static final String MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT = "MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT"; //$NON-NLS-1$

  private static final String MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT = "MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT"; //$NON-NLS-1$

  private static final String MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT = "MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT"; //$NON-NLS-1$

  private static final String MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT = "MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT"; //$NON-NLS-1$

  // Cron Trigger misfire instructions
  private static final String MISFIRE_INSTRUCTION_FIRE_ONCE_NOW = "MISFIRE_INSTRUCTION_FIRE_ONCE_NOW"; //$NON-NLS-1$

  private static final String MISFIRE_INSTRUCTION_DO_NOTHING = "MISFIRE_INSTRUCTION_DO_NOTHING"; //$NON-NLS-1$

  private Scheduler sched = null;

  private final List localInputNames = new ArrayList();

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.IComponent#init()
   */
  @Override
  public boolean init() {
    localInputNames.add(AbstractJobSchedulerAction.JOB_ACTION_ELEMENT);
    localInputNames.add(StartScheduledJobAction.SOLUTION_ELEMENT);
    localInputNames.add(StartScheduledJobAction.PATH_ELEMENT);
    localInputNames.add(StartScheduledJobAction.ACTION_ELEMENT);
    localInputNames.add(StartScheduledJobAction.TRIGGER_TYPE_ELEMENT);
    localInputNames.add(StartScheduledJobAction.TRIGGER_NAME_ELEMENT);
    localInputNames.add(StartScheduledJobAction.REPEAT_INTERVAL_ELEMENT);
    localInputNames.add(StartScheduledJobAction.REPEAT_COUNT_ELEMENT);
    localInputNames.add(AbstractJobSchedulerAction.JOB_NAME_ELEMENT);
    localInputNames.add(StartScheduledJobAction.CRON_STRING_ELEMENT);
    try {
      sched = QuartzSystemListener.getSchedulerInstance();
    } catch (Exception e) {
      error(Messages.getErrorString("JobSchedulerComponent.ERROR_0001_NoScheduler"), e); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#validateAction()
   */
  @Override
  protected boolean validateAction() {
    return getActionDefinition() instanceof AbstractJobSchedulerAction;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#validateSystemSettings()
   */
  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#done()
   */
  @Override
  public void done() {
    sched = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#executeAction()
   */
  @Override
  protected boolean executeAction() {

    AbstractJobSchedulerAction actionDefinition = (AbstractJobSchedulerAction) getActionDefinition();
    if (actionDefinition instanceof StartScheduledJobAction) {
      JobDetail jobDetail = createJobDetail((StartScheduledJobAction) actionDefinition);
      Trigger trigger = createTrigger((StartScheduledJobAction) actionDefinition);

      if ((trigger == null) || (jobDetail == null)) {
        error(Messages.getErrorString("JobSchedulerComponent.ERROR_0002_UnableToCreateTriggerOrJob")); //$NON-NLS-1$ 
        return false;
      }
      return startJob(jobDetail, trigger);
    } else if (actionDefinition instanceof SuspendScheduledJobAction) {
      return suspendJob(actionDefinition.getJobName().getStringValue(), Scheduler.DEFAULT_GROUP);
    } else if (actionDefinition instanceof DeleteScheduledJobAction) {
      return deleteJob(actionDefinition.getJobName().getStringValue(), Scheduler.DEFAULT_GROUP);
    } else if (actionDefinition instanceof ResumeScheduledJobAction) {
      return resumeJob(actionDefinition.getJobName().getStringValue(), Scheduler.DEFAULT_GROUP);
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.PentahoBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return LogFactory.getLog(JobSchedulerComponent.class);
  }

  /**
   * @return
   */
  private JobDetail createJobDetail(final StartScheduledJobAction actionDefinition) {
    JobDetail jobDetail = new JobDetail(actionDefinition.getJobName().getStringValue(), Scheduler.DEFAULT_GROUP,
        QuartzExecute.class);

    JobDataMap jdm = jobDetail.getJobDataMap();
    jdm.put(StartScheduledJobAction.SOLUTION_ELEMENT, actionDefinition.getSolution().getStringValue());

    // Prevents a null path if the action is at the root of the solution.
    jdm.put(StartScheduledJobAction.PATH_ELEMENT, actionDefinition.getPath().getStringValue("")); //$NON-NLS-1$
    jdm.put(StartScheduledJobAction.ACTION_ELEMENT, actionDefinition.getAction().getStringValue());

    // Support scheduled actions in the portal and assigning the credential properly in the execute.
    IPentahoSession session = this.getSession();
    if ((session != null) && (session.isAuthenticated())) {
      jdm.put(JobSchedulerComponent.USER_STR, session.getName());
    }
    Iterator inputNamesIterator = getInputNames().iterator();
    while (inputNamesIterator.hasNext()) {
      String inputName = (String) inputNamesIterator.next();
      if (!localInputNames.contains(inputName)) {
        Object inputValue = getInputValue(inputName);
        jobDetail.getJobDataMap().put(inputName, inputValue);
      }
    }

    return jobDetail;
  }

  /**
   * @return
   */
  private Trigger createTrigger(final StartScheduledJobAction actionDef) {
    Trigger trigger = null;

    String triggerType = actionDef.getTriggerType().getStringValue(StartScheduledJobAction.SIMPLE_TRIGGER);
    if (triggerType.equals(StartScheduledJobAction.SIMPLE_TRIGGER)) {
      trigger = createSimpleTrigger(actionDef);
    } else if (triggerType.equals(StartScheduledJobAction.CRON_TRIGGER)) {
      trigger = createCRONTrigger(actionDef);
    }
    addMisfireInstruction(trigger);
    return trigger;
  }

  private Trigger createSimpleTrigger(final StartScheduledJobAction actionDef) {
    String triggerName = actionDef.getTriggerName().getStringValue(StartScheduledJobAction.DEFAULT_STR);
    // Convert it into milliseconds
    int repeatInterval = actionDef.getRepeatInterval().getIntValue(0) * 1000;

    int repeatCount = actionDef.getRepeatCount().getIntValue(-1);
    if (repeatCount == -1) {
      repeatCount = SimpleTrigger.REPEAT_INDEFINITELY;
    }

    Trigger trigger = new SimpleTrigger(triggerName, Scheduler.DEFAULT_GROUP, repeatCount, repeatInterval);
    return trigger;
  }

  private Trigger createCRONTrigger(final StartScheduledJobAction actionDef) {
    String triggerName = actionDef.getTriggerName().getStringValue();
    String cronExpression = actionDef.getCronString().getStringValue();
    try {
      Trigger trigger = new CronTrigger(triggerName, Scheduler.DEFAULT_GROUP, cronExpression);
      return trigger;
    } catch (ParseException e) {
      error(Messages.getErrorString("JobSchedulerComponent.ERROR_0003_UnableToParse", cronExpression), e); //$NON-NLS-1$
      return null;
    }
  }

  /**
   * @param trigger
   */
  private void addMisfireInstruction(final Trigger trigger) {

    String misfirePolicy = JobSchedulerComponent.MISFIRE_INSTRUCTION_SMART_POLICY;
    if (isDefinedInput(JobSchedulerComponent.MISFIRE_POLICY)) {
      misfirePolicy = getInputStringValue(JobSchedulerComponent.MISFIRE_POLICY);
    }

    trigger.setMisfireInstruction(Trigger.MISFIRE_INSTRUCTION_SMART_POLICY); // Default
    if (misfirePolicy.equalsIgnoreCase(JobSchedulerComponent.INSTRUCTION_RE_EXECUTE_JOB)) {
      trigger.setMisfireInstruction(Trigger.INSTRUCTION_RE_EXECUTE_JOB);
    } else if (misfirePolicy.equals(JobSchedulerComponent.INSTRUCTION_DELETE_TRIGGER)) {
      trigger.setMisfireInstruction(Trigger.INSTRUCTION_DELETE_TRIGGER);
    } else if (misfirePolicy.equals(JobSchedulerComponent.INSTRUCTION_SET_TRIGGER_COMPLETE)) {
      trigger.setMisfireInstruction(Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
    } else if (misfirePolicy.equals(JobSchedulerComponent.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE)) {
      trigger.setMisfireInstruction(Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE);
    } else if (misfirePolicy.equals(JobSchedulerComponent.MISFIRE_INSTRUCTION_SMART_POLICY)) {
      trigger.setMisfireInstruction(Trigger.MISFIRE_INSTRUCTION_SMART_POLICY);
    } else if (misfirePolicy.equals(JobSchedulerComponent.INSTRUCTION_NOOP)) {
      trigger.setMisfireInstruction(Trigger.INSTRUCTION_NOOP);
    } else if (misfirePolicy.equals(JobSchedulerComponent.MISFIRE_INSTRUCTION_FIRE_NOW)
        && (trigger instanceof SimpleTrigger)) {
      trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
    } else if (misfirePolicy.equals(JobSchedulerComponent.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT)
        && (trigger instanceof SimpleTrigger)) {
      trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT);
    } else if (misfirePolicy.equals(JobSchedulerComponent.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT)
        && (trigger instanceof SimpleTrigger)) {
      trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    } else if (misfirePolicy
        .equals(JobSchedulerComponent.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT)
        && (trigger instanceof SimpleTrigger)) {
      trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT);
    } else if (misfirePolicy
        .equals(JobSchedulerComponent.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT)
        && (trigger instanceof SimpleTrigger)) {
      trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT);
    } else if (misfirePolicy.equals(JobSchedulerComponent.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW)
        && (trigger instanceof CronTrigger)) {
      trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
    } else if (misfirePolicy.equals(JobSchedulerComponent.MISFIRE_INSTRUCTION_DO_NOTHING)
        && (trigger instanceof CronTrigger)) {
      trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    }
  }

  public boolean startJob(final JobDetail jobDetail, final Trigger trigger) {
    try { // If we got one already with the same name... overwrite it.
      if (sched.getJobDetail(jobDetail.getName(), Scheduler.DEFAULT_GROUP) != null) {
        deleteJob(jobDetail.getName(), Scheduler.DEFAULT_GROUP);
      }
      sched.scheduleJob(jobDetail, trigger);
      OutputStream feedbackOutputStream = getFeedbackOutputStream();
      if (feedbackOutputStream != null) {
        feedbackOutputStream.write(Messages.getString("JobSchedulerComponent.INFO_0001").getBytes()); //$NON-NLS-1$
      }
    } catch (SchedulerException e) {
      error(e.getLocalizedMessage());
      return false;
    } catch (IOException e) {
      error(e.getLocalizedMessage());
      return false;
    }
    return true;
  }

  public boolean suspendJob(final String jobName, final String groupName) {
    try {
      sched.pauseJob(jobName, groupName);
    } catch (SchedulerException e) {
      error(e.getLocalizedMessage());
      return false;
    }
    return true;
  }

  public boolean deleteJob(final String jobName, final String groupName) {
    try {
      sched.deleteJob(jobName, groupName);
    } catch (SchedulerException e) {
      error(e.getLocalizedMessage());
      return false;
    }
    return true;
  }

  public boolean resumeJob(final String jobName, final String groupName) {
    try {
      sched.resumeJob(jobName, groupName);
    } catch (SchedulerException e) {
      error(e.getLocalizedMessage());
      return false;
    }
    return true;
  }
}
