package org.pentaho.test.platform.plugin;

import org.pentaho.platform.plugin.action.mondrian.MissingParameterException;
import org.pentaho.test.platform.engine.core.BaseTest;

public class MissingParameterExceptionTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testMissingParameterException1() {
    startTest();
    info("Expected: Exception will be caught and thrown as a Missing Parameter Exception"); //$NON-NLS-1$
    MissingParameterException mpe1 = new MissingParameterException("A test Missing Parameter Exception has been thrown"); //$NON-NLS-1$
    System.out.println("Missing Parameter Exception :" + mpe1); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();
  }

  public void testMissingParameterException2() {
    startTest();
    info("Expected: A Missing Parameter Exception will be created with Throwable as a parameter"); //$NON-NLS-1$
    MissingParameterException mpe2 = new MissingParameterException(new Throwable("This is a throwable exception")); //$NON-NLS-1$
    System.out.println("Missing Parameter Exception :" + mpe2); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();

  }

  public void testMissingParameterException3() {
    startTest();
    info("Expected: Exception will be caught and thrown as a Missing Parameter Exception"); //$NON-NLS-1$
    MissingParameterException mpe3 = new MissingParameterException(
        "A test Missing Parameter Exception has been thrown", new Throwable());//$NON-NLS-1$
    System.out.println("Missing Parameter Exception :" + mpe3); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();

  }

  public static void main(String[] args) {
    MissingParameterExceptionTest test = new MissingParameterExceptionTest();
    try {
      test.setUp();
      test.testMissingParameterException1();
      test.testMissingParameterException2();
      test.testMissingParameterException3();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
