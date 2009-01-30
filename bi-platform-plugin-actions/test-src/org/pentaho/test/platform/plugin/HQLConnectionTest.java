package org.pentaho.test.platform.plugin;

import java.io.File;
import java.io.OutputStream;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;

public class HQLConnectionTest extends BaseTestCase {
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
  
  public void testHQLConnection() {
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    OutputStream outputStream = getOutputStream(SOLUTION_PATH, "Chart_Bubble", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    IRuntimeContext context = run(getSolutionPath() + "/test/datasource/", "HQL_Datasource.xaction", parameters, outputHandler); //$NON-NLS-1$
      assertEquals(
          Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
  }
  


  public static void main(String[] args) {
    HQLConnectionTest test = new HQLConnectionTest();
    try {
      test.testHQLConnection();
    } finally {
        BaseTest.shutdown();
    }
}
}
