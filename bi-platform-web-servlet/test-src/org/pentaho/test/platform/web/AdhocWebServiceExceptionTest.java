package org.pentaho.test.platform.web;


import org.pentaho.platform.web.servlet.AdhocWebServiceException;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;

public class AdhocWebServiceExceptionTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/solution";
  public String getSolutionPath() {
      return SOLUTION_PATH;
  }
   
  public void testAdhocWebServiceException1() {
    AdhocWebServiceException awse = new AdhocWebServiceException();
    System.out.println("AdhocWebServiceException :" +awse); //$NON-NLS-1$
    assertTrue(true);
  }
  

  public void testAdhocWebServiceException2() {
    AdhocWebServiceException awse1 = new AdhocWebServiceException("A test AdhocWebService Exception has been thrown"); //$NON-NLS-1$
    System.out.println("AdhocWebServiceException :" +awse1); //$NON-NLS-1$    
    assertTrue(true);
   }

  public void testAdhocWebServiceException3() {
    AdhocWebServiceException awse3 = new AdhocWebServiceException(new Throwable("This is a throwable exception")); //$NON-NLS-1$
    System.out.println("AdhocWebServiceException :" +awse3); //$NON-NLS-1$    
    assertTrue(true);
  }

  public void testAdhocWebServiceException4() {
    AdhocWebServiceException awse4 = new AdhocWebServiceException("A test AdhocWebService Exception has been thrown", new Throwable());//$NON-NLS-1$
    System.out.println("UIException :" +awse4); //$NON-NLS-1$    
    assertTrue(true);
  }

  public static void main(String[] args) {
    AdhocWebServiceExceptionTest test = new AdhocWebServiceExceptionTest();
    try {
      test.testAdhocWebServiceException1();
      test.testAdhocWebServiceException2();
      test.testAdhocWebServiceException3();
      test.testAdhocWebServiceException4();
    } finally {
      BaseTest.shutdown();
    }
  }
}
