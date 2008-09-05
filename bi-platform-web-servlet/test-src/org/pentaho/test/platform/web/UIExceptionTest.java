package org.pentaho.test.platform.web;

import org.pentaho.platform.api.ui.UIException;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;

public class UIExceptionTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/solution";


  public String getSolutionPath() {
      return SOLUTION_PATH;  
  } 
  public void testUIException1() {
    UIException uie = new UIException();
    System.out.println("UIException :" +uie); //$NON-NLS-1$
    assertTrue(true);
  }
  

  public void testUIException2() {
    UIException uie1 = new UIException("A test UI Exception has been thrown"); //$NON-NLS-1$
    System.out.println("UIException :" +uie1); //$NON-NLS-1$    
    assertTrue(true);
  }

  public void testUIException3() {
    UIException uie2 = new UIException(new Throwable("This is a throwable exception")); //$NON-NLS-1$
    System.out.println("UIException :" +uie2); //$NON-NLS-1$    
    assertTrue(true);
  }

  public void testUIException4() {
    UIException uie3 = new UIException("A test UI Exception has been thrown", new Throwable());//$NON-NLS-1$
    System.out.println("UIException :" +uie3); //$NON-NLS-1$    
    assertTrue(true);
   }

  public static void main(String[] args) {
    UIExceptionTest test = new UIExceptionTest();
    try {
       test.testUIException1();
      test.testUIException2();
      test.testUIException3();
      test.testUIException4();
    } finally {
      BaseTest.shutdown();
    }
  }
}
