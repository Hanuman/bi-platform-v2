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
 * @created Sep 17, 2007 
 * @author Will Gorman
 */
package org.pentaho.platform.scheduler.versionchecker;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.versionchecker.PentahoVersionCheckReflectHelper;
import org.pentaho.platform.scheduler.QuartzSystemListener;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class VersionCheckSystemListener implements IPentahoSystemListener {

  private static final String VERSION_CHECK_JOBNAME = "PentahoSystemVersionCheck"; //$NON-NLS-1$
  private static int MIN_CHECK_INTERVAL = 43200;
  private static int DEFAULT_CHECK_INTERVAL = 86400;
  
  private int repeatIntervalSeconds = DEFAULT_CHECK_INTERVAL;
  private String requestedReleases = "minor, ga";
  private boolean disableVersionCheck = false;

  public boolean startup(final IPentahoSession session) {
    if (PentahoVersionCheckReflectHelper.isVersionCheckerAvailable()) {
      // register version check job
      try {
        final ISystemSettings config = PentahoSystem.getSystemSettings();
        int repeatSeconds = Math.max(MIN_CHECK_INTERVAL, repeatIntervalSeconds); // Force maximum number of times to check in a day 
        int versionRequestFlags = -1;
        boolean requestMajorReleases = requestedReleases.indexOf("major") >= 0; //$NON-NLS-1$
        boolean requestMinorReleases = requestedReleases.indexOf("minor") >= 0; //$NON-NLS-1$
        boolean requestRCReleases = requestedReleases.indexOf("rc") >= 0; //$NON-NLS-1$
        boolean requestGAReleases = requestedReleases.indexOf("ga") >= 0; //$NON-NLS-1$
        boolean requestMilestoneReleases = requestedReleases.indexOf("milestone") >= 0; //$NON-NLS-1$
        
        versionRequestFlags = (requestMajorReleases ? 4 : 0) + (requestMinorReleases ? 8 : 0) + 
                              (requestRCReleases ? 16 : 0) + (requestGAReleases ? 32 : 0) +
                              (requestMilestoneReleases ? 64 : 0);
        
        if (!disableVersionCheck) {
          scheduleJob(versionRequestFlags, repeatSeconds);
        } else {
          deleteJobIfNecessary();
        }
      } catch (Exception e) {
        // ignore errors
      }

    } else {
      deleteJobIfNecessary();
    }
    return true;
  }

  protected void scheduleJob(final int versionRequestFlags, final int repeatSeconds) throws Exception {
    Scheduler sched = QuartzSystemListener.getSchedulerInstance();

    JobDetail jobDetail = new JobDetail(VersionCheckSystemListener.VERSION_CHECK_JOBNAME, Scheduler.DEFAULT_GROUP,
        VersionCheckerJob.class);

    // setup version request flags
    jobDetail.getJobDataMap().put(VersionCheckerJob.VERSION_REQUEST_FLAGS, versionRequestFlags);

    // do not persist the job
    jobDetail.setVolatility(true);

    Trigger trigger = new SimpleTrigger(
        "DailyTrigger", Scheduler.DEFAULT_GROUP, SimpleTrigger.REPEAT_INDEFINITELY, repeatSeconds * 1000); //$NON-NLS-1$

    // do not persist the trigger
    trigger.setVolatility(true);

    if (sched.getJobDetail(jobDetail.getName(), Scheduler.DEFAULT_GROUP) != null) {
      sched.deleteJob(jobDetail.getName(), Scheduler.DEFAULT_GROUP);
    }
    sched.scheduleJob(jobDetail, trigger);
  }

  protected void deleteJobIfNecessary() {
    try {
      // delete the job if it exists in the system
      Scheduler sched = QuartzSystemListener.getSchedulerInstance();
      if (sched.getJobDetail(VersionCheckSystemListener.VERSION_CHECK_JOBNAME, Scheduler.DEFAULT_GROUP) != null) {
        sched.deleteJob(VersionCheckSystemListener.VERSION_CHECK_JOBNAME, Scheduler.DEFAULT_GROUP);
      }
    } catch (Exception e) {
      // ignore errors
    }
  }

  public void shutdown() {
  }

  public int getRepeatIntervalSeconds() {
    return repeatIntervalSeconds;
  }

  public void setRepeatIntervalSeconds(int repeatIntervalSeconds) {
    this.repeatIntervalSeconds = repeatIntervalSeconds;
  }

  public String getRequestedReleases() {
    return requestedReleases;
  }

  public void setRequestedReleases(String requestedReleases) {
    this.requestedReleases = requestedReleases;
  }

  public boolean isDisableVersionCheck() {
    return disableVersionCheck;
  }

  public void setDisableVersionCheck(boolean disableVersionCheck) {
    this.disableVersionCheck = disableVersionCheck;
  }
}
