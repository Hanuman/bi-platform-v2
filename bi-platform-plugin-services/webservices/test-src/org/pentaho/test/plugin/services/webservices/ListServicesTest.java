package org.pentaho.test.plugin.services.webservices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.webservices.AxisConfig;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.plugin.services.webservices.content.ListServices;
import org.pentaho.plugin.services.webservices.content.WebServiceConst;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ListServicesTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = "test-src/solution"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml"; //$NON-NLS-1$

  public ListServicesTest() {
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

	  public void testRender() throws Exception {
		  
	    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

	     TestServiceSetup setup = new TestServiceSetup();
	      setup.setSession(session);
	      AxisConfig config =  AxisConfig.getInstance( setup );

	     config.getConfigurationContext().getAxisConfiguration().getService( "TestService3" ).setActive( false ); //$NON-NLS-1$


//	    AxisConfig config = AxisConfig.getInstance();

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
          assertTrue( "WSDL URL is missing", content.indexOf( WebServiceConst.getWsdlUrl() ) != -1 ); //$NON-NLS-1$
          assertTrue( "Test Service URL is missing", content.indexOf( WebServiceConst.getWsdlUrl()+"/TestService" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "setString is missing", content.indexOf( "setString" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "getString is missing", content.indexOf( "getString" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

	    	} catch (Exception e) {
	    		assertTrue( "Exception occurred", false ); //$NON-NLS-1$
	    	}
	  }

}
