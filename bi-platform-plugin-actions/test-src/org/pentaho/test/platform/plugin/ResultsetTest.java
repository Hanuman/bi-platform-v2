package org.pentaho.test.platform.plugin;


import java.io.File;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ResultsetTest extends BaseTest {
  private static final String SOLUTION_PATH = "projects/actions/test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);
      return ALT_SOLUTION_PATH;
    }

  }
  public void testResultSet() {
    startTest();
    IRuntimeContext context = run("test", "rules", "ResultSetTest.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public void testJavaScriptResultSet() {
    startTest();
    IRuntimeContext context = run("test", "rules", "JavaScriptResultSetTest.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }
  
  public void testResultSetWithoutColumnHeader() {
    startTest();
    IRuntimeContext context = run("test", "rules", "ResultSetTest_without_columnheader.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }
  
  public void testResultSetWithoutRowValue() {
    startTest();
    IRuntimeContext context = run("test", "rules", "ResultSetTest_without_rowvalue.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }  
  public void testResultSet_NullColumnHeader() {
    startTest();
    IRuntimeContext context = run("test", "rules", "ResultSetTest_NullColumnHeader.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE,context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }
  public void testResultSetTestWithDifferentDataTypes() {
    startTest();
    IRuntimeContext context = run("test", "rules", "ResultSetTestWithDifferentDataTypes.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }  
  public static void main(String[] args) {
    ResultsetTest test = new ResultsetTest();
    try {
      test.setUp();
      test.testResultSet();
      test.testJavaScriptResultSet();
      test.testResultSetWithoutColumnHeader();
      test.testResultSetWithoutRowValue();
      test.testResultSet_NullColumnHeader();
      test.testResultSetTestWithDifferentDataTypes();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
