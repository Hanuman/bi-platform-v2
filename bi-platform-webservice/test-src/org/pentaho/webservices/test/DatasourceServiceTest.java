package org.pentaho.webservices.test;

import java.io.File;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.webservices.SessionHandler;
import org.pentaho.platform.webservice.services.datasource.DatasourceService;
import org.pentaho.platform.webservice.services.datasource.WSDataSource;
import org.pentaho.test.platform.engine.core.BaseTest;

public class DatasourceServiceTest extends BaseTest {

	  private static final String SOLUTION_PATH = "test-src/solution"; //$NON-NLS-1$

	  private static final String ALT_SOLUTION_PATH = "test-src/solution"; //$NON-NLS-1$

	  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml"; //$NON-NLS-1$

	  public DatasourceServiceTest() {
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

	  public void testDatasoucreService1() throws Exception {
		  
      IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
	    SessionHandler.setSession(session);
	    DatasourceService svc = new DatasourceService();

	    List<WSDataSource> sources = svc.getDataSources();

	    assertNotNull( "Datasources is null", sources ); //$NON-NLS-1$
	    
	    assertEquals( "Wrong number of datasources", 2, sources.size() ); //$NON-NLS-1$
      assertEquals( "Wrong datasource name", "testdatasource1", sources.get(0).getName() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals( "Wrong datasource name", "testdatasource2", sources.get(1).getName() ); //$NON-NLS-1$ //$NON-NLS-2$

	    
	  }

}
