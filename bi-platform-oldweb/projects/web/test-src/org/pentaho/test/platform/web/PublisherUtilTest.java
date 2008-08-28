package org.pentaho.test.platform.web;


import java.io.File;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.util.client.PublisherUtil;
import org.pentaho.test.platform.engine.core.BaseTest;

public class PublisherUtilTest extends BaseTest {
	private static final String SOLUTION_PATH = "test-src/solution";
	
public void testPublisherUtil() {
      startTest();

      File[] listOfFiles = new File[2];
      File file1 = new File(PentahoSystem.getApplicationContext().getSolutionPath("test/analysis/index.xml")); //$NON-NLS-1$
      File file2 = new File(PentahoSystem.getApplicationContext().getSolutionPath("test/datasources/books.xml")); //$NON-NLS-1$
      listOfFiles[0] = file1;
      listOfFiles[1] = file2;
      int result = PublisherUtil.publish("http://localhost:9876/pentaho", "test", listOfFiles, "password", "serverUserid", "serverPassword", true);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      assertEquals("The result of the Publish was " + result, result, 200); //$NON-NLS-1$      
      String  key = PublisherUtil.getPasswordKey("password");//$NON-NLS-1$
      assertTrue("Publisher Key was " + key, key != null ); //$NON-NLS-1$      

      finishTest();
  }

  public void setUp() {
    super.setUp();
    TinyHTTPd.startServer(getSolutionPath(), 9876); // Start server on port 8080
    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(
    		getSolutionPath(), ""); //$NON-NLS-1$
    PentahoSystem.init(applicationContext, getRequiredListeners());
    
  }
  
  public void tearDown() {
    super.tearDown();
    TinyHTTPd.stopServer();    
  }

 
    public static void main(String[] args) {
        PublisherUtilTest test = new PublisherUtilTest();
        test.setUp();
        try {
          test.testPublisherUtil();
        } finally {
            test.tearDown();
            BaseTest.shutdown();
        }
    }
    
    @Override
	public String getSolutionPath() {
		return SOLUTION_PATH;
	}
}
