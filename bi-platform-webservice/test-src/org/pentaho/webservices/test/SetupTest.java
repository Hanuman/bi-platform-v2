package org.pentaho.webservices.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

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
import org.pentaho.platform.plugin.services.webservices.AxisConfig;
import org.pentaho.platform.plugin.services.webservices.IWebServiceConfigurator;
import org.pentaho.platform.webservice.plugin.WebServicesInitializer;

@SuppressWarnings({"all"})
public class SetupTest extends TestCase {

    public SetupTest() {
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
    
  public void testLoadServices() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    WebServicesInitializer initializer = new WebServicesInitializer();
    initializer.loaded();
  
    AxisConfig config = AxisConfig.getInstance( );
    assertNotNull( "AxisConfig is null", config ); //$NON-NLS-1$

    IWebServiceConfigurator setup = config.getAxisConfigurator();
//    IWebServiceConfigurator setup = config.getFactory().get( IWebServiceConfigurator.class, null );
    assertNotNull( "IWebServiceConfigurator is null", setup ); //$NON-NLS-1$
  
    assertNotNull( "DatasourceService is null", setup.getServiceWrapper( "DatasourceService" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
}
