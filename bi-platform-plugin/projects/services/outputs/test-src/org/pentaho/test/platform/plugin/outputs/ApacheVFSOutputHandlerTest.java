package org.pentaho.test.platform.plugin.outputs;



import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.plugin.outputs.ApacheVFSOutputHandler;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ApacheVFSOutputHandlerTest extends BaseTest {

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
