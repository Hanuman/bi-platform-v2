package org.pentaho.test.platform.plugin.services.connections.javascript;



import java.io.File;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class JavaScriptResultSetTest extends BaseTest {
  private static final String SOLUTION_PATH = "connections/test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if(file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;  
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);      
      return ALT_SOLUTION_PATH;
    }
    
  }
//  public void testRSCompareOK() {
//    startTest();
//    IRuntimeContext context = run("samples", "rules", "ResultSetTest.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
//    IActionParameter rtn = context.getOutputParameter("COMPARERESULT");//$NON-NLS-1$
//    assertNotNull(rtn);
//    String compareResult = rtn.getStringValue();
//    assertEquals(compareResult, "No Mismatches"); //$NON-NLS-1$
//    finishTest();
//  }
  
//  public void testRSCompareNotOK1() {
//    startTest();
//    IRuntimeContext context = run("samples", "rules", "ResultSetCompareTest_error1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
//    finishTest();
//  }
  
//  public void testRSCompareNotOK2() {
//    startTest();
//    IRuntimeContext context = run("samples", "rules", "ResultSetCompareTest_error2.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//    assertEquals(context.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE);
//
//    finishTest();
//  }
  
  public void testRSCompareNotOK3()
  {
    startTest();
    IRuntimeContext context = run("samples", "rules", "ResultSetCompareTest_error3.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(context.getStatus(), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL);

    finishTest();
    
  }
  
  public void testRSCompareNotOK4()
  {
    startTest();
    IRuntimeContext context = run("samples", "rules", "ResultSetCompareTest_error4.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(context.getStatus(), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL);

    finishTest();
    
  }

//  public void testRSCompareNotOK5()
//  {
//    startTest();
//    IRuntimeContext context = run("samples", "rules", "ResultSetCompareTest_error5.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//    assertEquals(context.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE);
//
//    finishTest();
//    
//  }  
  public static void main(String[] args) {
    JavaScriptResultSetTest test = new JavaScriptResultSetTest();
    try {
      test.setUp();
//      test.testRSCompareOK();
//      test.testRSCompareNotOK1();
//      test.testRSCompareNotOK2();
      test.testRSCompareNotOK3();
      test.testRSCompareNotOK4();
//      test.testRSCompareNotOK5();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
