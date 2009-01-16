package org.pentaho.test.platform.engine.services;

import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings({"all"})
public class MultiOutputTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testMultiOutput() {

    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    String xactionStr = ServiceTestHelper.getXAction(SOLUTION_PATH, "services/MultiOutputTest.xaction");
    IRuntimeContext runtimeContext = solutionEngine
        .execute(
            xactionStr,
            "test1a.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
    finishTest();

  }

}
