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
 * @created Jul 18, 2005 
 * @author James Dixon
 * 
 */
package org.pentaho.test.platform.scheduler;

import org.pentaho.test.platform.engine.core.BaseTest;

public class SchedulerTest extends BaseTest {

//  Todo: get this working!
//  public void testCreateNewInstanceWithOutParameters() {
//    startTest();
//    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
//    OutputStream outputStream = getOutputStream("RuntimeTest.testForcePrompt", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
//    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
//    outputHandler.setOutputPreference(IOutputHandler.OUTPUT_TYPE_PARAMETERS);
//    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//
//    ISolutionEngine solutionEngine = PentahoSystem.getSolutionEngineInstance(session);
//    solutionEngine.setLoggingLevel(getLoggingLevel());
//    solutionEngine.init(session);
//    solutionEngine.setForcePrompt(true);
//    IRuntimeContext context = run(solutionEngine,
//        "test", "reporting", "jfreereport-reports-test-param.xaction", null, false, parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//
//    context.trace("This is a test trace");//$NON-NLS-1$
//    context.debug("This is a test debug");//$NON-NLS-1$
//    context.fatal("This is a test fatal message");//$NON-NLS-1$
//    context.warn("This is a test warning");//$NON-NLS-1$
//    context.info("This is a test information message");//$NON-NLS-1$
//
//    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
//    requestParameters.setParameter("schedulerAction", SchedulerAdminUIComponent.GET_IS_SCHEDULER_PAUSED_ACTION_STR); //$NON-NLS-1$
//
//    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();
//
//    HashMap parameterProviders = new HashMap();
//    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
//    parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
//
//    String result = context.createNewInstance(true);
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE") + result, IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    finishTest();
//  }
//
//  public void testCreateNewInstanceWithParameters() {
//    startTest();
//    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
//    OutputStream outputStream = getOutputStream("RuntimeTest.testForcePrompt", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
//    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
//    outputHandler.setOutputPreference(IOutputHandler.OUTPUT_TYPE_PARAMETERS);
//    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//
//    ISolutionEngine solutionEngine = PentahoSystem.getSolutionEngineInstance(session);
//    solutionEngine.setLoggingLevel(getLoggingLevel());
//    solutionEngine.init(session);
//    solutionEngine.setForcePrompt(true);
//    IRuntimeContext context = run(solutionEngine,
//        "test", "reporting", "jfreereport-reports-test-param.xaction", null, false, parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//
//    context.trace("This is a test trace", new Throwable());//$NON-NLS-1$
//    context.debug("This is a test debug", new Throwable());//$NON-NLS-1$
//    context.fatal("This is a test fatal message", new Throwable());//$NON-NLS-1$
//    context.warn("This is a test warning", new Throwable());//$NON-NLS-1$
//    context.info("This is a test information message", new Throwable());//$NON-NLS-1$
//
//    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
//    requestParameters.setParameter("schedulerAction", SchedulerAdminUIComponent.GET_IS_SCHEDULER_PAUSED_ACTION_STR); //$NON-NLS-1$
//
//    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();
//
//    HashMap parameterProviders = new HashMap();
//    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
//    parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
//    String result2 = context.createNewInstance(true, parameterProviders);
//
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE") + result2, IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    finishTest();
//  }
//
//  public Map getRequiredListeners() {
//    Map listeners = super.getRequiredListeners();
//    listeners.put("quartz", "quartz"); //$NON-NLS-1$ //$NON-NLS-2$
//    return listeners;
//  }
//
//  public void testScheduler_NewJob() {
//    startTest();
//    IRuntimeContext context = run("test", "scheduler", "SchedulerTest_new_job.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    try {
//      Thread.sleep(15000);
//    } catch (Exception e) {
//      error(Messages.getString("SchedulerTest.ERROR_0001_WAITING_INTERRUPTED"), e); //$NON-NLS-1$
//    }
//    finishTest();
//  }
//
//  public void testScheduler_DeleteJob() {
//    startTest();
//    IRuntimeContext context = run("test", "scheduler", "SchedulerTest_delete_job.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    try {
//      Thread.sleep(15000);
//    } catch (Exception e) {
//      error(Messages.getString("SchedulerTest.ERROR_0001_WAITING_INTERRUPTED"), e); //$NON-NLS-1$
//    }
//    finishTest();
//  }
//
//  public void testScheduler_NewCronJob() {
//    startTest();
//    IRuntimeContext context = run("test", "scheduler", "SchedulerTest_new_cron_job.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    try {
//      Thread.sleep(15000);
//    } catch (Exception e) {
//      error(Messages.getString("SchedulerTest.ERROR_0001_WAITING_INTERRUPTED"), e); //$NON-NLS-1$
//    }
//    finishTest();
//  }
//
//  public void testScheduler_PauseJob() {
//    startTest();
//    IRuntimeContext context = run("test", "scheduler", "SchedulerTest_pause_job.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    try {
//      Thread.sleep(15000);
//    } catch (Exception e) {
//      error(Messages.getString("SchedulerTest.ERROR_0001_WAITING_INTERRUPTED"), e); //$NON-NLS-1$
//    }
//    finishTest();
//  }
//
//  public void testScheduler_PauseScheduler() {
//    startTest();
//    IRuntimeContext context = run("test", "scheduler", "SchedulerTest_pause_scheduler.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    try {
//      Thread.sleep(15000);
//    } catch (Exception e) {
//      error(Messages.getString("SchedulerTest.ERROR_0001_WAITING_INTERRUPTED"), e); //$NON-NLS-1$
//    }
//    finishTest();
//  }
//
//  public void testScheduler_ResumeJob() {
//    startTest();
//    IRuntimeContext context = run("test", "scheduler", "SchedulerTest_resume_job.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    try {
//      Thread.sleep(15000);
//    } catch (Exception e) {
//      error(Messages.getString("SchedulerTest.ERROR_0001_WAITING_INTERRUPTED"), e); //$NON-NLS-1$
//    }
//    finishTest();
//  }
//
//  public void testScheduler_ResumeScheduler() {
//    startTest();
//    IRuntimeContext context = run("test", "scheduler", "SchedulerTest_resume_scheduler.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//
//    try {
//      Thread.sleep(15000);
//    } catch (Exception e) {
//      error(Messages.getString("SchedulerTest.ERROR_0001_WAITING_INTERRUPTED"), e); //$NON-NLS-1$
//    }
//    finishTest();
//  }

  public void setUp() {
    // do nothing, get the above test to pass!
  }
  
  public void testDummyTest() {
    // do nothing get the above test to pass!
  }
  
  public static void main(String[] args) {
    SchedulerTest test = new SchedulerTest();
    test.setUp();
    try {
//      test.testCreateNewInstanceWithOutParameters();
//      test.testCreateNewInstanceWithParameters();
//      test.testScheduler_NewJob();
//      test.testScheduler_DeleteJob();
//      test.testScheduler_NewCronJob();
//      test.testScheduler_PauseJob();
//      test.testScheduler_PauseScheduler();
//      test.testScheduler_ResumeJob();
//      test.testScheduler_ResumeScheduler();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
