package org.pentaho.test.platform.plugin.pluginmgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class BadSolutionRepoTest extends BaseTest {
  private static final String SOLUTION_PATH = "plugin-mgr/test-res/solution2-no-repo"; //$NON-NLS-1$
  private static final String ALT_SOLUTION_PATH = "test-res/solution2-no-repo"; //$NON-NLS-1$
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml"; //$NON-NLS-1$

  @Override 
  public String getSolutionPath() {
      File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
      if(file.exists()) {
        return SOLUTION_PATH;  
      } else {
        return ALT_SOLUTION_PATH;
      }
  }

  public void testBadSolutionRepo() {
	    startTest();
	    
	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
	    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, session ); 
	    assertNotNull( pluginManager );
	    
	    List<String> messages = new ArrayList<String>();
	    boolean result = pluginManager.updatePluginSettings(session, messages);
	    assertFalse( "Plugin update should fail", result ); //$NON-NLS-1$
	    assertEquals( "Update failure is for wrong reason", Messages.getString("PluginManager.ERROR_0008_CANNOT_GET_REPOSITORY"), messages.get(0) ); //$NON-NLS-1$ //$NON-NLS-2$
	    
	    finishTest();
	  }

}
