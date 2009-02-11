package org.pentaho.test.platform.plugin.services.webservices;

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
import org.pentaho.platform.plugin.services.webservices.AxisConfig;
import org.pentaho.platform.plugin.services.webservices.content.ServiceWsdl;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;
import org.pentaho.platform.util.web.SimpleUrlFactory;


public class BaseClassesError1Test extends TestCase {
  
  public void testBadInit2() throws Exception {
    
    AxisConfig config = AxisConfig.getInstance( );
    assertNull( config );
        
  }
  
  public void testBadInit1() throws Exception {
    
    try {
      AxisConfig.getInstance( null );
      assertTrue( "Exception expected", false ); //$NON-NLS-1$
    } catch (NullPointerException e) {
      assertTrue( "Exception expected", true ); //$NON-NLS-1$
    }
    
  }
  
  public void testBadInit3() {
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    ServiceWsdl contentGenerator = new ServiceWsdl();
    
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
          assertTrue( content.indexOf( Messages.getErrorString("WebServiceContentGenerator.ERROR_0001_AXIS_CONFIG_IS_NULL") ) != -1 ); //$NON-NLS-1$ 
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }
  
}
