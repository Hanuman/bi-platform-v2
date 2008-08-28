package org.pentaho.test.platform.web.ui.component;


import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.uifoundation.chart.FlashChartComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;


public class FlashChartTests extends BaseTest {
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
    public void testBarChart() {
		
		String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

		String thisUrl = baseUrl + "Chart?"; //$NON-NLS-1$

		SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
		ArrayList messages = new ArrayList();

		String chartDefinitionPath = SOLUTION_PATH + "/test/dashboard/flashdial.widget.xml"; //$NON-NLS-1$

        StandaloneSession userSession = new StandaloneSession("BaseTest.DEBUG_JUNIT_SESSION"); //$NON-NLS-1$

		FlashChartComponent chart = new FlashChartComponent( chartDefinitionPath, 600, 400, urlFactory, messages ); 
		chart.validate( userSession, null );
		chart.setDataAction(chartDefinitionPath);

		String content = chart.getContent( "text/html" ); //$NON-NLS-1$

		OutputStream output = getOutputStream( "FlashChartTests.testBarChart", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
		
		try {
			output.write( content.getBytes() );
		} catch (Exception e) {
			// this will get caught during conent comparison
		}
		compare( "FlashChartTests.testBarChart", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
		
	}
	
    public static void main(String[] args) {
    		FlashChartTests test = new FlashChartTests();
        test.setUp();
        try {
            test.testBarChart();
        } finally {
            test.tearDown();
            BaseTest.shutdown();
        }
    }
	
}
