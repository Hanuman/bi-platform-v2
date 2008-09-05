package org.pentaho.test.platform.plugin;

import java.io.OutputStream;
import java.util.Map;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.services.SoapHelper;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ETLTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put("kettle", "kettle"); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void testKettleTransform1() {

    startTest();
    IRuntimeContext context = run("test", "etl", "ETLTransform1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    assertEquals(context.getOutputNames().contains("rule-result"), true); //$NON-NLS-1$
    IActionParameter param = context.getOutputParameter("rule-result"); //$NON-NLS-1$

    OutputStream os = getOutputStream("DataTest.testKettleTransform1", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
    IPentahoResultSet resultSet = param.getValueAsResultSet();
    String soapString = SoapHelper.toSOAP("ETL Result", resultSet); //$NON-NLS-1$
    try {
      os.write(soapString.getBytes());
    } catch (Exception e) {

    }
    finishTest();

  }

  public void testKettleJob1() {

    startTest();
    // this writes a XML file directly into test/tmp/DataTest.testKettleJob1.xml
    IRuntimeContext context = run("test", "etl", "ETLJob1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$

    finishTest();

  }

  public static void main(String[] args) {
    ETLTest test = new ETLTest();
    test.setUp();
    try {
      test.testKettleTransform1();
      test.testKettleJob1();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
