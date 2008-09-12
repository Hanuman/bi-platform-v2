package org.pentaho.test.platform.plugin.pluginmgr;

import java.io.File;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.PluginManager;
import org.pentaho.test.platform.engine.core.BaseTest;

public class PluginManagerTest extends BaseTest {
  private static final String SOLUTION_PATH = "plugin-mgr/test-res/solution4-content-gen"; //$NON-NLS-1$
  private static final String ALT_SOLUTION_PATH = "test-res/solution4-content-gen"; //$NON-NLS-1$
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

  public void testPluginManagerViaPublish() throws Exception {
	    startTest();
	    
	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$

	    String str = PentahoSystem.publish( session, "org.pentaho.platform.plugin.services.pluginmgr.PluginManager"); //$NON-NLS-1$
	    assertTrue( str.indexOf( "Updating plugin") > 0 ); //$NON-NLS-1$
	    finishTest();
	  }

  @SuppressWarnings("cast")
  public void testPluginManagerViaPublisherAPI() throws Exception {
  	    startTest();
  	    
  	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$

  	    PluginManager mgr = new PluginManager();
  	    assertTrue( mgr instanceof IPentahoPublisher );
  	    IPentahoPublisher publisher = (IPentahoPublisher) mgr;
  	    
  	    assertEquals( Messages.getString("PluginManager.USER_PLUGIN_MANAGER"), publisher.getName() ); //$NON-NLS-1$
  	    assertNotSame( "!PluginManager.USER_PLUGIN_MANAGER!", publisher.getName() ); //$NON-NLS-1$
  	    
  	    assertEquals( Messages.getString("PluginManager.USER_REFRESH_PLUGINS"), publisher.getDescription() ); //$NON-NLS-1$
  	    assertNotSame( "!PluginManager.USER_REFRESH_PLUGINS!", publisher.getName() ); //$NON-NLS-1$
  	    
  	    String str = publisher.publish(session, ILogger.DEBUG);
  	    assertTrue( str.indexOf( "Updating plugin") > 0 ); //$NON-NLS-1$
  	    finishTest();
  	  }

  @SuppressWarnings("cast")
  public void testPluginManagerViaSystemListenerAPI() throws Exception {
  	    startTest();
  	    
  	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$

  	    PluginManager mgr = new PluginManager();
  	    assertTrue( mgr instanceof IPentahoSystemListener );
  	    
  	    IPentahoSystemListener listener = (IPentahoSystemListener) mgr;
  	    
  	    assertTrue( listener.startup(session) );

  	    // this does not do anything but it shouldn't error
  	    listener.shutdown();
  	    
  	    finishTest();
  	  }

  
}
