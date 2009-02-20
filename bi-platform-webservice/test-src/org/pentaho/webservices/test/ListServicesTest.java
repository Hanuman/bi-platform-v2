package org.pentaho.webservices.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SimpleSystemSettings;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemStartupSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory.Scope;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.plugin.services.webservices.content.WebServiceConst;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.webservice.content.ListServices;
import org.pentaho.platform.webservice.plugin.WebServicesInitializer;
import org.pentaho.platform.webservice.plugin.messages.Messages;

@SuppressWarnings({"all"})
public class ListServicesTest extends TestCase {

    public ListServicesTest() {
      super( );
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
    
	  public void testRender() throws Exception {
		  
	    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

      WebServicesInitializer initializer = new WebServicesInitializer();
      initializer.loaded();
    
	    ListServices contentGenerator = new ListServices();
      assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
	    
	    	assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
	    	
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
	    	IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
	    	
	    	String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
	    	Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
	    	SimpleParameterProvider requestParams = new SimpleParameterProvider();
	    	parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
	        SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl+"?" ); //$NON-NLS-1$
	    	List<String> messages = new ArrayList<String>();
	    	contentGenerator.setOutputHandler(outputHandler);
        MimeTypeListener mimeTypeListener = new MimeTypeListener();
	    	outputHandler.setMimeTypeListener(mimeTypeListener);
	    	contentGenerator.setMessagesList(messages);
	    	contentGenerator.setParameterProviders(parameterProviders);
	    	contentGenerator.setSession(session);
	    	contentGenerator.setUrlFactory(urlFactory);
	    	try {
	        	contentGenerator.createContent();
	        	String content = new String( out.toByteArray() );
          System.out.println( content );

          assertTrue( "style is missing", content.indexOf( ".h1" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "style is missing", content.indexOf( "text/css" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          
          assertTrue( "WSDL URL is missing", content.indexOf( WebServiceConst.getWsdlUrl() ) != -1 ); //$NON-NLS-1$
          assertTrue( "WSDL URL is missing", content.indexOf( WebServiceConst.getWsdlUrl()+"/DatasourceService" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

          assertTrue( "Run URL is missing", content.indexOf( WebServiceConst.getExecuteUrl()+"/DatasourceService" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

          assertTrue( "Title is missing", content.indexOf( Messages.getString( "DatasourceServiceWrapper.USER_DATASOURCE_SERVICE_TITLE" ) ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

	    	} catch (Exception e) {
	    		assertTrue( "Exception occurred", false ); //$NON-NLS-1$
	    	}
	  }

}
