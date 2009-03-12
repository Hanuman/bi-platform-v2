/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
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
  private static final String SOLUTION_PATH = "test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      return SOLUTION_PATH;
    } else {
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
