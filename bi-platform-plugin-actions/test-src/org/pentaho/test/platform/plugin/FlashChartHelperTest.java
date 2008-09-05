package org.pentaho.test.platform.plugin;

import java.io.OutputStream;
import java.util.ArrayList;

import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.uifoundation.chart.FlashChartHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

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
