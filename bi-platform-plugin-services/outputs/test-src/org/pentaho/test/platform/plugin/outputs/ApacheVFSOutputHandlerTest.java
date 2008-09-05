package org.pentaho.test.platform.plugin.outputs;



import java.io.File;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.plugin.outputs.ApacheVFSOutputHandler;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ApacheVFSOutputHandlerTest extends BaseTest {
  private static final String SOLUTION_PATH = "outputs/test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml";

  @Override 
  public String getSolutionPath() {
      File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
      if(file.exists()) {
        return SOLUTION_PATH;  
      } else {
        return ALT_SOLUTION_PATH;
      }
  }
  public void testAudit() {
    startTest();
    
    ApacheVFSOutputHandler handler = new ApacheVFSOutputHandler(); 
    IContentItem contentItem = handler.getFileOutputContentItem();
    System.out.println("Content Item for VFS" + contentItem);  //$NON-NLS-1$  
    
    
    
    assertTrue(true);
    finishTest();
  }

  public static void main(String[] args) {
    ApacheVFSOutputHandlerTest test = new ApacheVFSOutputHandlerTest();
    test.setUp();
    test.testAudit();
    try {

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
