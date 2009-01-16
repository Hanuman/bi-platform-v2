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

@SuppressWarnings({"all"})
public class SystemParameterProviderTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";
	  public String getSolutionPath() {
	       return SOLUTION_PATH;  
	  }
		public void testSystemParameter() {
		    startTest();
        SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();
        assertEquals( "Output is not correct", "server.log", provider.getStringParameter("pentaho.xml{pentaho-system/log-file}", null) );
	        finishTest();
		}
		
		public void testCustomParameter() {
		    startTest();
	        IPentahoSession session = new StandaloneSession("joe");
          CustomSettingsParameterProvider provider = new CustomSettingsParameterProvider();
          provider.setSession( session );
          
          assertEquals( "Output is not correct", "value1", provider.getStringParameter("settings-{$user}.xml{personal-settings/setting1}", null ) );
	        finishTest();
		}
}
