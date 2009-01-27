package org.pentaho.webservices.test;

import java.io.File;

import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.webservice.plugin.WebServicesInitializer;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.webservice.core.AxisConfig;

public class BadInitialize1Test extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution2"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = "test-src/solution2"; //$NON-NLS-1$

	  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml"; //$NON-NLS-1$

	  public BadInitialize1Test() {
		  super( SOLUTION_PATH );
	  }
	  
	  public String getSolutionPath() {
	    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
	    if (file.exists()) {
	      System.out.println("File exist returning " + SOLUTION_PATH); //$NON-NLS-1$
	      return SOLUTION_PATH;
	    } else {
	      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH); //$NON-NLS-1$
	      return ALT_SOLUTION_PATH;
	    }
	  }

	  public void testInit() throws Exception {
		  
      StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
  
      WebServicesInitializer initializer = new WebServicesInitializer();
      
      initializer.init(session);
    
      AxisConfig config = AxisConfig.getInstance( );

      assertNull( "AxisConfig is null", config ); //$NON-NLS-1$
      
	  }

}
