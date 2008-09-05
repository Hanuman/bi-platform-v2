package org.pentaho.test.platform.plugin;

import java.io.OutputStream;
import java.util.Map;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class KettleTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put("kettle", "kettle"); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void testKettle() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream outputStream = getOutputStream("KettleTest.testKettle", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    assertNotNull(outputStream);
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    assertNotNull(outputHandler);
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    assertNotNull(session);
    IRuntimeContext context = run(
        "test", "etl", "SampleTransformation.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    // TODO need some validation of success
    finishTest();
  }

  public void testKettleValidatationFailure() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream outputStream = getOutputStream("KettleTest.testKettle", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    assertNotNull(outputStream);
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    assertNotNull(outputHandler);
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    assertNotNull(session);
    IRuntimeContext context = run(
        "test", "etl", "SampleTransformationInvalid.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus()); //$NON-NLS-1$
    // TODO need some validation of success
    finishTest();
  }

  public void testKettleMissingTransform() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream outputStream = getOutputStream("KettleTest.testKettle", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    assertNotNull(outputStream);
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    assertNotNull(outputHandler);
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    assertNotNull(session);
    IRuntimeContext context = run(
        "test", "etl", "SampleTransformationMissingKTR.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus()); //$NON-NLS-1$
    // TODO need some validation of success
    finishTest();
  }

  public static void main(String[] args) {
    KettleTest test = new KettleTest();
    test.setUp();
    try {
      test.testKettle();
      test.testKettleValidatationFailure();
      test.testKettleMissingTransform();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
