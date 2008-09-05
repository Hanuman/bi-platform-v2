package org.pentaho.test.platform.plugin;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class WorkflowTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testWorkflow2() {
    startTest();
    IRuntimeContext context = run("test", "workflow", "start-process2.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  public void testBurstWorkflow() {
    startTest();
    IRuntimeContext context = run("test", "bursting", "BurstShark.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  public void testScheduledBurstWorkflow() {
    startTest();
    IRuntimeContext context = run("test", "bursting", "schedule-burst.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  public void testSendEmailWorkflow() {
    startTest();
    IRuntimeContext context = run("test", "bursting", "send-email.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  public void testSharkEmailReportWorkflow() {
    startTest();
    IRuntimeContext context = run("test", "bursting", "shark-email-report.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  public static void main(String[] args) {

    WorkflowTest test = new WorkflowTest();
    test.setUp();
    try {
      test.testWorkflow2();
      test.testBurstWorkflow();
      test.testScheduledBurstWorkflow();
      test.testSendEmailWorkflow();
      test.testSharkEmailReportWorkflow();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }

  }
}
