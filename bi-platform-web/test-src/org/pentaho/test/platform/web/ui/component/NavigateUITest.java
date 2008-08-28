package org.pentaho.test.platform.web.ui.component;
import java.io.File;

import org.pentaho.test.platform.web.ui.BaseUITest;




public class NavigateUITest extends BaseUITest {
  
  private static final String SOLUTION_PATH = "projects/portlet/test-src/solution";
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
	public void testLogin() {
		
		runUrl( "j_acegi_security_check?j_username=suzy&j_password=password", "NavigateUITest.testLogin" ); //$NON-NLS-1$ //$NON-NLS-2$
		
	}

	public void testHomePage() {
		
		runUrl( "Home", "NavigateUITest.testHomePage" ); //$NON-NLS-1$ //$NON-NLS-2$
		
	}
	public void testNavigatePage() {
		
		runUrl( "Navigate", "NavigateUITest.testNavigatePage" ); //$NON-NLS-1$ //$NON-NLS-2$
		
	}

}
