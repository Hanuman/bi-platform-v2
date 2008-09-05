package org.pentaho.test.platform.engine.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.solution.CustomSettingsParameterProvider;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

public class SystemParameterProviderTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";
	  public String getSolutionPath() {
	       return SOLUTION_PATH;  
	  }
		public void testSystemParameter() {
		    startTest();
	        IPentahoSession session = new StandaloneSession("joe");
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine( session );
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "services/params1.xaction" );
	            Map<String,IParameterProvider> paramProviders = new HashMap<String,IParameterProvider>();
	            SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();
	            paramProviders.put( "system-settings" , provider);
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	            		xactionStr, "params1.xaction", "param test", false, true, null, false, paramProviders, null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "Action sequence execution failed", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS );
	    	    IActionParameter param = runtimeContext.getOutputParameter( "output1" );
	            assertNotNull( "Ouptut is null", param );
	    	    assertEquals( "Output is not correct", "server.logserver.log", param.getStringValue() );
	        } catch (Exception e) {
	        	// we should not get here
	        	e.printStackTrace();
	        	assertTrue( e.getMessage(), false );
	        }
	        finishTest();
		}
		
		public void testCustomParameter() {
		    startTest();
	        IPentahoSession session = new StandaloneSession("joe");
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine( session );
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "services/params2.xaction" );
	            Map<String,IParameterProvider> paramProviders = new HashMap<String,IParameterProvider>();
	            CustomSettingsParameterProvider provider = new CustomSettingsParameterProvider();
	            provider.setSession( session );
	            paramProviders.put( "custom-settings" , provider);
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	            		xactionStr, "params2.xaction", "param test", false, true, null, false, paramProviders, null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "Action sequence execution failed", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS );
	    	    IActionParameter param = runtimeContext.getOutputParameter( "output1" );
	            assertNotNull( "Ouptut is null", param );
	    	    assertEquals( "Output is not correct", "value1value1", param.getStringValue() );
	        } catch (Exception e) {
	        	// we should not get here
	        	e.printStackTrace();
	        	assertTrue( e.getMessage(), false );
	        }
	        finishTest();
		}
}
