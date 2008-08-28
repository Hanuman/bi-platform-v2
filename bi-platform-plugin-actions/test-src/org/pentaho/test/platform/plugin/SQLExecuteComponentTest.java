package org.pentaho.test.platform.plugin;


import java.io.File;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class SQLExecuteComponentTest extends BaseTest {

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
  public void testSQLExecuteComponentSingleStatement() {
    startTest();
    info("Expected: Successful execution with one row of data"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "rules", "sqlexecute_rule1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter("rule-result");//$NON-NLS-1$
    assertNotNull(rtn);
    IPentahoResultSet resultset = (IPentahoResultSet)rtn.getValue();
    
    assertEquals( resultset.getRowCount(), 1 );
    
    assertEquals("Expected first row to contain a 1 in the first column.", resultset.getValueAt(0, 0), new Integer(1)); //$NON-NLS-1$
    
    finishTest();    
  }
  
  public void testSQLExecuteComponentMultipleStatements() {
    startTest();
    info("Expected: Successful execution with 5 rows of data in the result. First row should have a negative #"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "rules", "sqlexecute_rule2.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter("rule-result");//$NON-NLS-1$
    assertNotNull(rtn);
    IPentahoResultSet resultset = (IPentahoResultSet)rtn.getValue();

    assertEquals( resultset.getRowCount(), 5 );
    
    Integer rsVal = (Integer)resultset.getValueAt(0, 0);
    assertTrue("Expected first row to contain a negative number in the first column.", (rsVal.intValue() < 0) ); //$NON-NLS-1$
    
    finishTest();
  }
  
  public void testSQLExecuteForceSingle() {
    startTest();
    info("Expected: Successful execution with 1 row of data in the result - expected value in column is 0"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "rules", "sqlexecute_rule3.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter("rule-result");//$NON-NLS-1$
    assertNotNull(rtn);
    IPentahoResultSet resultset = (IPentahoResultSet)rtn.getValue();
    
    assertEquals( resultset.getRowCount(), 1 );
    
    Integer rsVal = (Integer)resultset.getValueAt(0, 0);
    assertTrue("Expected first row to contain a zero first column.", (rsVal.intValue() == 0) ); //$NON-NLS-1$
    
    finishTest();
  }
  
  public void testSQLExecuteComponentMultipleStatementsNewSeparator() {
    startTest();
    info("Expected: Successful execution with 5 rows of data in the result. First row should have a negative #"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "rules", "sqlexecute_rule4.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter("rule-result");//$NON-NLS-1$
    assertNotNull(rtn);
    IPentahoResultSet resultset = (IPentahoResultSet)rtn.getValue();

    assertEquals( resultset.getRowCount(), 5 );
    
    Integer rsVal = (Integer)resultset.getValueAt(0, 0);
    assertTrue("Expected first row to contain a negative number in the first column.", (rsVal.intValue() < 0) ); //$NON-NLS-1$
    
    finishTest();
  }
  
  public void testSQLExecuteErrorDropTable() {
    startTest();
    info("Expected: Failed execution - should error out at the drop table statement"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "rules", "sqlexecute_error1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
  }
  
  public void testSQLExecuteErrorSQLWithSemiColons() {
    startTest();
    info("Expected: Failed execution - should error out because of the semi-colon"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "rules", "sqlexecute_error2.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
  }
  
  public void testSQLExecuteErrorDropTableSingleStatement() {
    startTest();
    info("Expected: Failed execution - should error out at the drop table statement"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "rules", "sqlexecute_error3.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
  }
  
  public static void main(String[] args) {
    SQLExecuteComponentTest test = new SQLExecuteComponentTest();
    try {
      test.setUp();
      test.testSQLExecuteComponentSingleStatement();
      test.testSQLExecuteComponentMultipleStatements();
      test.testSQLExecuteForceSingle();
      test.testSQLExecuteComponentMultipleStatementsNewSeparator();
      test.testSQLExecuteErrorDropTable();
      test.testSQLExecuteErrorSQLWithSemiColons();
      test.testSQLExecuteErrorDropTableSingleStatement();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
