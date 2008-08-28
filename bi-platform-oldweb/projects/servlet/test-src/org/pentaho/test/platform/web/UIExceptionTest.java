package org.pentaho.test.platform.web;

import org.pentaho.platform.api.ui.UIException;
import org.pentaho.test.platform.engine.core.BaseTest;

public class UIExceptionTest extends BaseTest {

   
  public void testUIException1() {
    startTest();
    info("Expected: Exception will be caught and thrown as a UI Exception"); //$NON-NLS-1$
    UIException uie = new UIException();
    System.out.println("UIException :" +uie); //$NON-NLS-1$
    assertTrue(true);
    finishTest();

  }
  

  public void testUIException2() {
    startTest();
    info("Expected: Exception will be caught and thrown as a UI Exception"); //$NON-NLS-1$
    UIException uie1 = new UIException("A test UI Exception has been thrown"); //$NON-NLS-1$
    System.out.println("UIException :" +uie1); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();
  }

  public void testUIException3() {
    startTest();
    info("Expected: A UI Exception will be created with Throwable as a parameter"); //$NON-NLS-1$
    UIException uie2 = new UIException(new Throwable("This is a throwable exception")); //$NON-NLS-1$
    System.out.println("UIException :" +uie2); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();

  }

  public void testUIException4() {
    startTest();
    info("Expected: Exception will be caught and thrown as a UI Exception"); //$NON-NLS-1$
    UIException uie3 = new UIException("A test UI Exception has been thrown", new Throwable());//$NON-NLS-1$
    System.out.println("UIException :" +uie3); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();
 
  }

  public static void main(String[] args) {
    UIExceptionTest test = new UIExceptionTest();
    try {
      test.setUp();
      test.testUIException1();
      test.testUIException2();
      test.testUIException3();
      test.testUIException4();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
