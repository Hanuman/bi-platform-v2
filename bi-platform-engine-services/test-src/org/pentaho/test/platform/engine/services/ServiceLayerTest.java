package org.pentaho.test.platform.engine.services;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IObjectFactoryCreator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.SpringObjectFactoryCreator;
import org.pentaho.platform.util.web.SimpleUrlFactory;

public class ServiceLayerTest extends TestCase {

  final String SYSTEM_FOLDER = "/system";

  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testEmptyActionSequence() {
    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(getSolutionPath(), ""); //$NON-NLS-1$
    IObjectFactoryCreator facCreator;
    String objectFactoryCreatorCfgFile = getSolutionPath() + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$
    try {
      facCreator = new SpringObjectFactoryCreator();
      facCreator.configure(objectFactoryCreatorCfgFile);
    } catch (Exception e) {
      //Logger.fatal( SolutionContextListener.class.getName(), e.getMessage() );
      throw new RuntimeException("Failed to configure the Pentaho Object Factory.", e);
    }
    IPentahoObjectFactory pentahoObjectFactory = facCreator.getFactory();
    PentahoSystem.setObjectFactory(pentahoObjectFactory);
    PentahoSystem.init(applicationContext);
    List messages = new ArrayList();
    String instanceId = null;
    IPentahoSession session = new StandaloneSession("system");
    ISolutionEngine solutionEngine = PentahoSystem.getSolutionEngineInstance(session);
    solutionEngine.setLoggingLevel(ILogger.ERROR);
    solutionEngine.init(session);
    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    HashMap parameterProviderMap = new HashMap();
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(baseUrl);

    try {
      File file = new File(getSolutionPath() + "/services_layer/test1.xaction");
      StringBuilder str = new StringBuilder();
      Reader reader = new FileReader(file);
      char buffer[] = new char[4096];
      int n = reader.read(buffer);
      while (n != -1) {
        str.append(buffer, 0, n);
        n = reader.read(buffer);
      }
      String xactionStr = str.toString();

      solutionEngine.setSession(session);
      IRuntimeContext runtimeContext = solutionEngine
          .execute(
              xactionStr,
              "test1.xaction", "empty action sequence test", false, true, instanceId, false, parameterProviderMap, null, null, urlFactory, messages); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull("RuntimeContext is null", runtimeContext);
      assertEquals("Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS);
    } catch (Exception e) {
      // we should not get here
      e.printStackTrace();
      assertTrue(e.getMessage(), false);
    }

  }

}
