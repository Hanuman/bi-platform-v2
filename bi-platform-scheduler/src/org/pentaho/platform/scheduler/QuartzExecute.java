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
 */
/*
 * Created on Jul 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.pentaho.platform.scheduler;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.scheduler.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * @author James Dixon
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class QuartzExecute extends PentahoBase implements Job {

  /**
   * 
   */
  private static final long serialVersionUID = -1897327117890535410L;

  private static final boolean debug = PentahoSystem.debug;

  private String logId;

  private static final Log logger = LogFactory.getLog(QuartzExecute.class);

  @Override
  public Log getLogger() {
    return QuartzExecute.logger;
  }

  @Override
  public String getLogId() {
    return logId;
  }

  public void execute(final JobExecutionContext context) {
    PentahoSystem.systemEntryPoint();
    setLoggingLevel(PentahoSystem.loggingLevel);
    try {
      LocaleHelper.setLocale(Locale.getDefault());
      logId = "Schedule:" + context.getJobDetail().getName(); //$NON-NLS-1$

      Date now = new Date();
      QuartzExecute.logger
          .info(Messages
              .getString(
                  "QuartzExecute.INFO_TRIGGER_TIME", context.getJobDetail().getName(), DateFormat.getDateInstance().format(now), DateFormat.getTimeInstance().format(now))); //$NON-NLS-1$

      JobDataMap dataMap = context.getJobDetail().getJobDataMap();

      // Save the user parameters for use as an agument to
      // parameterProvider
      HashMap parameters = new HashMap();
      String[] keys = dataMap.getKeys();
      for (String element : keys) {
        parameters.put(element, dataMap.get(element));
      }

      // we need to generate a unique session id
      // String sessionId = "scheduler-"+this.hashCode()+"-"+new
      // Date().getTime(); //$NON-NLS-1$ //$NON-NLS-2$
      String sessionId = "scheduler-" + UUIDUtil.getUUIDAsString(); //$NON-NLS-1$

      StandaloneSession executeSession = new StandaloneSession(context.getJobDetail().getName(), sessionId);

      String solutionName = dataMap.getString("solution"); //$NON-NLS-1$
      String actionPath = dataMap.getString("path"); //$NON-NLS-1$
      String actionName = dataMap.getString("action"); //$NON-NLS-1$
      String instanceId = null;
      String processId = this.getClass().getName();
      IPentahoSession userSession = null;
      if (solutionName == null) {
        error(Messages.getErrorString("QuartzExecute.ERROR_0001_SOLUTION_NAME_MISSING")); //$NON-NLS-1$
        return;
      }
      if (actionPath == null) {
        error(Messages.getErrorString("QuartzExecute.ERROR_0002_ACTION_PATH_MISSING")); //$NON-NLS-1$
        return;
      }
      if (actionName == null) {
        error(Messages.getErrorString("QuartzExecute.ERROR_0003_ACTION_NAME_MISSING")); //$NON-NLS-1$
        return;
      }
      if (QuartzExecute.debug) {
        debug(Messages.getString(
            "QuartzExecute.DEBUG_EXECUTION_INFO", solutionName + "/" + actionPath + "/" + actionName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }

      boolean backgroundExecution = "true".equals(dataMap.getString(QuartzBackgroundExecutionHelper.BACKGROUND_EXECUTION_FLAG)); //$NON-NLS-1$
      IOutputHandler outputHandler = null;
      SimpleParameterProvider parameterProvider = new SimpleParameterProvider(parameters);
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.get(IBackgroundExecution.class, executeSession);
      if (backgroundExecution) {
        String location = dataMap.getString(QuartzBackgroundExecutionHelper.BACKGROUND_CONTENT_LOCATION_STR);
        String fileName = dataMap.getString(QuartzBackgroundExecutionHelper.BACKGROUND_CONTENT_GUID_STR);
        String userName = dataMap.getString(QuartzBackgroundExecutionHelper.BACKGROUND_USER_NAME_STR);

        userSession = backgroundExecutionHandler.getEffectiveUserSession(userName);
        // session.setAuthenticated(userName);
        outputHandler = backgroundExecutionHandler.getContentOutputHandler(location, fileName, solutionName,
            userSession, parameterProvider);

      } else {
        outputHandler = new SimpleOutputHandler((OutputStream) null, false);
        // Check to see if the user was authenticated (via the portal) in the JobSchedulerComponent
        String userName = dataMap.getString("username"); //$NON-NLS-1$
        if (userName != null) {
          // Well, we got a valid user name - let's try to use the
          // background execute component to establish the user
          userSession = backgroundExecutionHandler.getEffectiveUserSession(userName);
        } else {
          // User wasn't authenticated when the job was scheduled - use default behavior from old...
          userSession = executeSession;
        }
      }

      BaseRequestHandler requestHandler = new BaseRequestHandler(userSession, null, outputHandler, parameterProvider,
          null);
      requestHandler.setParameterProvider(IParameterProvider.SCOPE_SESSION, new PentahoSessionParameterProvider(
          userSession));

      requestHandler.setInstanceId(instanceId);
      requestHandler.setProcessId(processId);
      requestHandler.setAction(actionPath, actionName);
      requestHandler.setSolutionName(solutionName);

      IRuntimeContext rt = null;
      try {
        rt = requestHandler.handleActionRequest(0, 0);
        if (backgroundExecution) {
          if (!outputHandler.contentDone()) {
            IContentItem outputContentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE,
                IOutputHandler.CONTENT, rt.getActionTitle(), null, rt.getSolutionName(), rt.getInstanceId(),
                "text/html"); //$NON-NLS-1$
            outputContentItem.setMimeType("text/html"); //$NON-NLS-1$
            try {
              if ((rt != null) && (rt.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS)) {
                StringBuffer buffer = new StringBuffer();
                PentahoSystem.getMessageFormatter(userSession).formatSuccessMessage("text/html", rt, buffer, false); //$NON-NLS-1$
                OutputStream os = outputContentItem.getOutputStream(actionName);
                os.write(buffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
                os.close();
              } else {
                // we need an error message...
                StringBuffer buffer = new StringBuffer();
                PentahoSystem.getMessageFormatter(userSession).formatFailureMessage(
                    "text/html", rt, buffer, requestHandler.getMessages()); //$NON-NLS-1$
                OutputStream os = outputContentItem.getOutputStream(actionName);
                os.write(buffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
                os.close();
              }
            } catch (IOException ex) {
              QuartzExecute.logger.error(ex.getLocalizedMessage());
            }
          }

        }

        IContentItem outputContentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE,
            IOutputHandler.CONTENT, rt.getSolutionName(), rt.getInstanceId(), "text/html"); //$NON-NLS-1$
        if (outputContentItem != null) {
          context.put(QuartzBackgroundExecutionHelper.BACKGROUND_CONTENT_GUID_STR, outputContentItem.getId());
        }
      } finally {
        if (rt != null) {
          rt.dispose();
        }
      }
      if (QuartzExecute.debug) {
        QuartzExecute.logger.debug(Messages.getString(
            "QuartzExecute.DEBUG_FINISHED_EXECUTION", context.getJobDetail().getName())); //$NON-NLS-1$
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

}
