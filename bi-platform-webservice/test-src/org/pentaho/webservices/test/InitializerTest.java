package org.pentaho.webservices.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SimpleSystemSettings;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemStartupSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory.Scope;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.webservice.plugin.WebServicesInitializer;
import org.pentaho.webservice.core.AxisConfig;
import org.pentaho.webservice.core.IWebServiceConfigurator;

@SuppressWarnings({"all"})
public class InitializerTest extends TestCase {

  public InitializerTest() {
    String path="./test-src/solution/system";
    File resourceDir = new File( path );
    if( !resourceDir.exists() ) {
      System.err.println( "The file system resources cannot be found" );
    }
    System.out.println( "File system resources located: "+ resourceDir.getAbsolutePath() );

    // create an object factory
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    // specify the objects we will use
    factory.defineObject( ISolutionEngine.class.getSimpleName(), SolutionEngine.class.getName(), Scope.LOCAL );
    factory.defineObject( "systemStartupSession", SystemStartupSession.class.getName(), Scope.LOCAL );
    factory.defineObject( IPluginResourceLoader.class.getSimpleName(), PluginResourceLoader.class.getName(), Scope.GLOBAL );
    PentahoSystem.setObjectFactory( factory );

    // create a settings object.
    SimpleSystemSettings settings = new SimpleSystemSettings();
    settings.addSetting( "pentaho-system" , "" );
    PentahoSystem.setSystemSettingsService( settings );
    
    // specify the startup listeners
    List<IPentahoSystemListener> listeners = new ArrayList<IPentahoSystemListener>();
    PentahoSystem.setSystemListeners( listeners );

    StandaloneApplicationContext app = new StandaloneApplicationContext(path, "");
    app.setBaseUrl( "http://localhost:8080/pentaho/" );
    
    // initialize the system
    boolean initOk = PentahoSystem.init( app );

    ((PluginResourceLoader)PentahoSystem.get(IPluginResourceLoader.class, null)).setRootDir(new File("./test-src/solution/system/webservices"));
  }

    @SuppressWarnings({ "cast" })
	  public void testInit() throws Exception {
		  
      StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
  
      WebServicesInitializer initializer = new WebServicesInitializer();
      
      assertTrue( "WebServicesInitializer is wrong type", initializer instanceof IPentahoInitializer ); //$NON-NLS-1$
      
      initializer.createContent( null );
      
      initializer.getMimeType();
      
      assertNotNull( "logger is null", initializer.getLogger() ); //$NON-NLS-1$
      
      initializer.init(session);
    
      AxisConfig config = AxisConfig.getInstance( );

      assertNotNull( "AxisConfig is null", config ); //$NON-NLS-1$
      
      IWebServiceConfigurator setup = config.getAxisConfigurator();
      
      assertNotNull( "IWebServiceConfigurator is null", setup ); //$NON-NLS-1$

      assertNotNull( "logger is null", setup.getLogger() ); //$NON-NLS-1$
      
	  }

}
