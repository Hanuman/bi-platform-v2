package org.pentaho.test.platform.plugin;


import java.io.File;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class SQLLookupRuleTest extends BaseTest {
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

  public void testSQLLookupSingleStatement() {
    startTest();
    info("Expected: Successful lookup with one row of data"); //$NON-NLS-1$
    IRuntimeContext context = run("test", "rules", "query_rule1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter("rule-result");//$NON-NLS-1$
    assertNotNull(rtn);
    IPentahoResultSet resultset = (IPentahoResultSet)rtn.getValue();
    assertEquals( resultset.getRowCount(), 1 );
    assertEquals("Expected first row to contain a 1 in the first column.", resultset.getValueAt(0, 0), new Integer(1)); //$NON-NLS-1$
    
    finishTest();    
  }
  
 
  public static void main(String[] args) {
    SQLLookupRuleTest test = new SQLLookupRuleTest();
    try {
      test.setUp();
      test.testSQLLookupSingleStatement();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
