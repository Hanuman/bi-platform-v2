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
 * Created Aug 12, 2005 
 * @author wseyler
 */

package org.pentaho.platform.scheduler.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.actions.ActionDefinition;
import org.pentaho.actionsequence.dom.actions.ListSchedJobsAction;
import org.pentaho.actionsequence.dom.actions.ResumeSchedulerAction;
import org.pentaho.actionsequence.dom.actions.SchedulerStatusAction;
import org.pentaho.actionsequence.dom.actions.SuspendSchedulerAction;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.scheduler.QuartzSystemListener;
import org.pentaho.platform.scheduler.messages.Messages;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SchedulerAdminComponent extends ComponentBase {

  /**
   * 
   */
  private static final long serialVersionUID = 5003948074206255569L;

  private static final String SCHEDULER_ACTION_STR = "schedulerAction"; //$NON-NLS-1$

  private Scheduler sched = null;

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.IComponent#init()
   */
  @Override
  public boolean init() {
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
    return isDefinedInput(SchedulerAdminComponent.SCHEDULER_ACTION_STR);
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

    ActionDefinition actionDefinition = (ActionDefinition) getActionDefinition();

    if (actionDefinition instanceof ListSchedJobsAction) {
      return doGetJobNames();
    } else if (actionDefinition instanceof SuspendSchedulerAction) {
      return doPauseAll();
    } else if (actionDefinition instanceof ResumeSchedulerAction) {
      return doResumeAll();
    } else if (actionDefinition instanceof SchedulerStatusAction) {
      return doIsSchedulerPaused();
    } else {
      return false;
    }
  }

  /**
   * @return
   */
  private boolean doIsSchedulerPaused() {
    if (feedbackAllowed()) {
      try {
        String resultBool = sched.isInStandbyMode() ? Messages.getString("SchedulerAdminComponent.CODE_true") : Messages.getString("SchedulerAdminComponent.CODE_false"); //$NON-NLS-1$ //$NON-NLS-2$
        createFeedbackParameter(
            "isPaused", Messages.getString("SchedulerAdminComponent.USER_IS_PAUSED"), "", resultBool, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return true;
      } catch (SchedulerException e) {
        error(Messages.getErrorString("SchedulerAdminComponent.ERROR_0001_SchedulerError"), e); //$NON-NLS-1$
        return false;
      }
    }
    return false;
  }

  /**
   * @return
   */
  private boolean doGetJobNames() {
    if (feedbackAllowed()) {
      try {
        String[] jobNames = sched.getJobNames(Scheduler.DEFAULT_GROUP);
        ArrayList values = new ArrayList();
        for (String element : jobNames) {
          values.add(element);
        }
        createFeedbackParameter(
            "jobNames", Messages.getString("SchedulerAdminComponent.USER_JOB_NAMES"), "", null, values, null, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return true;
      } catch (SchedulerException e) {
        error(Messages.getErrorString("SchedulerAdminComponent.ERROR_0001_SchedulerError"), e); //$NON-NLS-1$
        return false;
      }
    }
    return false;
  }

  /**
   * @return
   */
  private boolean doResumeAll() {
    try {
      sched.resumeAll();
    } catch (SchedulerException e) {
      error(Messages.getErrorString("SchedulerAdminComponent.ERROR_0001_SchedulerError"), e); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  /**
   * @return
   */
  private boolean doPauseAll() {
    try {
      sched.pauseAll();
    } catch (SchedulerException e) {
      error(Messages.getErrorString("SchedulerAdminComponent.ERROR_0001_SchedulerError"), e); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.PentahoBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return LogFactory.getLog(SchedulerAdminComponent.class);
  }

}
