package org.pentaho.test.plugin.services.webservices;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.webservices.AxisConfig;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.plugin.services.webservices.content.ListServices;
import org.pentaho.plugin.services.webservices.messages.Messages;

public class ListServices2Test extends TestCase {

  public ListServices2Test() {
  }
  
	  public void testRender() throws Exception {
		  
	    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
	    
	    TestServiceSetup2 setup = new TestServiceSetup2();
	    setup.setSession(session);
	    AxisConfig.getInstance( setup );
	    
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
          
          assertTrue( "Messages is missing", content.indexOf( Messages.getString("ListServices.USER_NO_SERVICES") ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
	    	} catch (Exception e) {
	    		assertTrue( "Exception occurred", false ); //$NON-NLS-1$
	    	}
	  }

}
