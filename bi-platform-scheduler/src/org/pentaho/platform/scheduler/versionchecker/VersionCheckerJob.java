package org.pentaho.platform.scheduler.versionchecker;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.versionchecker.PentahoVersionCheckReflectHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class VersionCheckerJob implements Job {

  public static final String VERSION_REQUEST_FLAGS = "versionRequestFlags"; //$NON-NLS-1$

  public Log getLogger() {
    return LogFactory.getLog(VersionCheckerJob.class);
  }

  public void execute(final JobExecutionContext context) throws JobExecutionException {
    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
    int versionRequestFlags = -1;
    try {
      versionRequestFlags = dataMap.getInt(VersionCheckerJob.VERSION_REQUEST_FLAGS);
    } catch (Exception e) {
      // ignore
    }
    List results = PentahoVersionCheckReflectHelper.performVersionCheck(false, versionRequestFlags);
    if (results != null) {
      PentahoVersionCheckReflectHelper.logVersionCheck(results, getLogger());
    }
  }
}
