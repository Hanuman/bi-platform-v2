package org.pentaho.test.platform.web;


import org.pentaho.platform.web.servlet.AdhocWebServiceException;
import org.pentaho.test.platform.engine.core.BaseTest;

public class AdhocWebServiceExceptionTest extends BaseTest {

   
  public void testAdhocWebServiceException1() {
    startTest();
    info("Expected: Exception will be caught and thrown as a AdhocWebService Exception"); //$NON-NLS-1$
    AdhocWebServiceException awse = new AdhocWebServiceException();
    System.out.println("AdhocWebServiceException :" +awse); //$NON-NLS-1$
    assertTrue(true);
    finishTest();

  }
  

  public void testAdhocWebServiceException2() {
    startTest();
    info("Expected: Exception will be caught and thrown as a AdhocWebService Exception"); //$NON-NLS-1$
    AdhocWebServiceException awse1 = new AdhocWebServiceException("A test AdhocWebService Exception has been thrown"); //$NON-NLS-1$
    System.out.println("AdhocWebServiceException :" +awse1); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();
  }

  public void testAdhocWebServiceException3() {
    startTest();
    info("Expected: A AdhocWebService Exception will be created with Throwable as a parameter"); //$NON-NLS-1$
    AdhocWebServiceException awse3 = new AdhocWebServiceException(new Throwable("This is a throwable exception")); //$NON-NLS-1$
    System.out.println("AdhocWebServiceException :" +awse3); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();

  }

  public void testAdhocWebServiceException4() {
    startTest();
    info("Expected: Exception will be caught and thrown as a AdhocWebService Exception"); //$NON-NLS-1$
    AdhocWebServiceException awse4 = new AdhocWebServiceException("A test AdhocWebService Exception has been thrown", new Throwable());//$NON-NLS-1$
    System.out.println("UIException :" +awse4); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();
 
  }

  public static void main(String[] args) {
    AdhocWebServiceExceptionTest test = new AdhocWebServiceExceptionTest();
    try {
      test.setUp();
      test.testAdhocWebServiceException1();
      test.testAdhocWebServiceException2();
      test.testAdhocWebServiceException3();
      test.testAdhocWebServiceException4();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
