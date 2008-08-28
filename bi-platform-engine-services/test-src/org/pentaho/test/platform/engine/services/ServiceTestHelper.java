package org.pentaho.test.platform.engine.services;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;

public class ServiceTestHelper {

	public static String getXAction( String solutionRoot, String actionPath ) {
		String xactionStr = null;
        try {
            File file = new File( solutionRoot+"/"+actionPath );
            StringBuilder str = new StringBuilder();
           	Reader reader = new FileReader( file );
           	char buffer[] = new char[4096];
           	int n = reader.read( buffer );
           	while( n != -1 ) {
           		str.append( buffer, 0, n );
           		n = reader.read( buffer );
           	}
            xactionStr = str.toString();
        } catch (Exception e) {
        	// we should not get here
        	e.printStackTrace();
        }
        return xactionStr;
	}
	
	public static void init(String solutionRoot) {
		if( !PentahoSystem.getInitializedOK() ) {
	        StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(solutionRoot, ""); //$NON-NLS-1$
	        PentahoSystem.init(applicationContext );
		}
	}
	
	public static ISolutionEngine getSolutionEngine() {
        IPentahoSession session = new StandaloneSession("system");
        return getSolutionEngine(session);
		
	}

	public static ISolutionEngine getSolutionEngine( IPentahoSession session ) {
        ISolutionEngine solutionEngine = PentahoSystem.getSolutionEngineInstance(session);
        solutionEngine.setLoggingLevel(ILogger.ERROR);
        solutionEngine.init(session);
        return solutionEngine;
	}
}
