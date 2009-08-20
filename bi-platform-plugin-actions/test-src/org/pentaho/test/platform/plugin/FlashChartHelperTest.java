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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.plugin;

import java.io.OutputStream;
import java.util.ArrayList;

import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.uifoundation.chart.FlashChartHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class FlashChartHelperTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testFlashChart() {
    startTest();
    ArrayList messages = new ArrayList();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    parameters.setParameter("drill-url", "SampleDashboard.jsp?region={REGION}"); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter("inner-param", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter("image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter("image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
    StringBuffer content = new StringBuffer();
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

    FlashChartHelper.doFlashChart(
        "samples", "dashboard", "regions.widget.xml", parameters, content, session, messages, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    try {
      OutputStream os = getOutputStream("ChartTest.testFlashChart", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
      os.write(content.toString().getBytes());
    } catch (Exception e) {

    }
    finishTest();
  }

  public void testDialChart() {
    startTest();
    ArrayList messages = new ArrayList();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    parameters.setParameter("drill-url", "SampleDashboard.jsp?region={REGION}"); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter("inner-param", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter("image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter("image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
    StringBuffer content = new StringBuffer();
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

    FlashChartHelper.doFlashDial(
        "samples", "dashboard", "regions.widget.xml", parameters, content, session, messages, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    try {
      OutputStream os = getOutputStream("ChartTest.testDialChart", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
      os.write(content.toString().getBytes());
    } catch (Exception e) {

    }
    finishTest();
  }

  public static void main(String[] args) {
    FlashChartHelperTest test = new FlashChartHelperTest();
    test.setUp();
    try {
      test.testFlashChart();
      test.testDialChart();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
