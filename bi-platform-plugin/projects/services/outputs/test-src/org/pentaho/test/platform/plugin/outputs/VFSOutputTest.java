package org.pentaho.test.platform.plugin.outputs;



import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class VFSOutputTest extends BaseTest {

  public void testFileOutput() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    IRuntimeContext context = run("test", "platform", "VFSOutputTest_file.xaction", parameterProvider, "VFSOutputTest.testFileOutput", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    finishTest();
  }
  
  public static void main(String[] args) {
    VFSOutputTest test = new VFSOutputTest();
    try {
      test.setUp();
      test.testFileOutput();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
