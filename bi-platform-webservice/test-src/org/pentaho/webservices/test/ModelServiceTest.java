package org.pentaho.webservices.test;

import java.io.File;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.webservices.SessionHandler;
import org.pentaho.platform.webservice.services.metadata.server.ModelService;
import org.pentaho.pms.schema.v3.model.ModelEnvelope;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ModelServiceTest extends BaseTest {

	  private static final String SOLUTION_PATH = "test-src/solution"; //$NON-NLS-1$

	  private static final String ALT_SOLUTION_PATH = "test-src/solution"; //$NON-NLS-1$

	  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml"; //$NON-NLS-1$

	  public ModelServiceTest() {
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

	  public void testListModels() throws Exception {
		  
      IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
	    SessionHandler.setSession(session);
	    ModelService svc = new ModelService();

	    ModelEnvelope[] models = svc.listModels();

	    assertNotNull( "models is null", models ); //$NON-NLS-1$
	    
	    assertEquals( "Wrong number of models", 1, models.length ); //$NON-NLS-1$
      assertEquals( "Wrong model name", "Orders", models[0].getName() ); //$NON-NLS-1$ //$NON-NLS-2$

	    
	  }

}
