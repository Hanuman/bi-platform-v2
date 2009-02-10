package org.pentaho.test.platform.plugin;

import java.io.File;
import java.io.OutputStream;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;

public class SQLConnectionTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/solution";
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
  
/*  public void testSQLConnection() {
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    OutputStream outputStream = getOutputStream(SOLUTION_PATH, "Chart_Bubble", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    IRuntimeContext context = run(getSolutionPath() + "/test/datasource/", "SQL_Datasource.xaction", parameters, outputHandler); //$NON-NLS-1$
      assertEquals(
          Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
  }
  
*/
    public void testDummyTest() {
      //have to have at least one test method to make JUnit happy
    }

  public static void main(String[] args) {
//    SQLConnectionTest test = new SQLConnectionTest();
    try {
//      test.testSQLConnection();
    } finally {
        BaseTest.shutdown();
    }
}
}
