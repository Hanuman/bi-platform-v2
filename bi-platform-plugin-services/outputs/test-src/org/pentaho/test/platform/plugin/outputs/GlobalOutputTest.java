package org.pentaho.test.platform.plugin.outputs;



import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.web.SimpleUrlFactory;


public class GlobalOutputTest extends TestCase {

	public static String SOLUTION_PATH = "test-src/solution";
	
	public void testEmptyActionSequence() {
        StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(SOLUTION_PATH, ""); //$NON-NLS-1$
        PentahoSystem.init(applicationContext );

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
            File file = new File( SOLUTION_PATH+"/samples/platform/SetGlobalOutputTest.xaction" );
            StringBuilder str = new StringBuilder();
           	Reader reader = new FileReader( file );
           	char buffer[] = new char[4096];
           	int n = reader.read( buffer );
           	while( n != -1 ) {
           		str.append( buffer, 0, n );
           		n = reader.read( buffer );
           	}
            String xactionStr = str.toString();
                    
            solutionEngine.setSession(session);
            IRuntimeContext runtimeContext = solutionEngine.execute( 
            		xactionStr, "SetGlobalOutputTest.xaction", "empty action sequence test", false, true, instanceId, false, parameterProviderMap, null, null, urlFactory, messages); //$NON-NLS-1$ //$NON-NLS-2$
            assertNotNull( "RuntimeContext is null", runtimeContext );
            assertEquals( "Action sequence execution failed", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS );
            IParameterProvider provider = PentahoSystem.getGlobalParameters();
            String parameter = provider.getStringParameter("GLOBAL_TEST", null); //$NON-NLS-1$
            assertNotNull(parameter);
            assertEquals("This is a test", parameter); //$NON-NLS-1$
            
        } catch (Exception e) {
        	// we should not get here
        	e.printStackTrace();
        	assertTrue( e.getMessage(), false );
        }
        
	}
	
}

