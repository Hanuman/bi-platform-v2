package org.pentaho.webservices.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.junit.Test;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.webservices.AxisConfig;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.webservice.content.RunJsonService;
import org.pentaho.test.platform.engine.core.BaseTest;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;

public class Run1Test extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = "test-src/solution"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml"; //$NON-NLS-1$

  public Run1Test() {
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

  @Test
  public void testListModels() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession(session);
    AxisConfig config =  AxisConfig.getInstance( setup );

    /*
    // create a test transport so we can catch the output
    config.setTransportOut( "http" ); //$NON-NLS-1$
    
    assertEquals( "Transport is wrong", "http", config.getTransportOut() ); //$NON-NLS-1$ //$NON-NLS-2$
*/
    TransportInDescription tIn = new TransportInDescription( "http" ); //$NON-NLS-1$
    StubTransportListener receiver = new StubTransportListener();
    tIn.setReceiver(receiver);
    config.getAxisConfigurator().getAxisConfiguration().addTransportIn(tIn);
    
    TransportOutDescription tOut = new TransportOutDescription( "http" ); //$NON-NLS-1$
    StubTransportSender sender = new StubTransportSender();
    tOut.setSender(sender);
    config.getAxisConfigurator().getAxisConfiguration().addTransportOut(tOut);

    LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(config.getAxisConfigurator().getAxisConfiguration());
    
    RunJsonService contentGenerator = new RunJsonService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/ModelService/listModels");  //$NON-NLS-1$//$NON-NLS-2$
      pathParams.setParameter( "remoteaddr" , "http:test");  //$NON-NLS-1$//$NON-NLS-2$
      parameterProviders.put( "path", pathParams ); //$NON-NLS-1$
        SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl+"?" ); //$NON-NLS-1$
      List<String> messages = new ArrayList<String>();
      contentGenerator.setOutputHandler(outputHandler);
      MimeTypeListener mimeTypeListener = new MimeTypeListener();
      outputHandler.setMimeTypeListener(mimeTypeListener);
      contentGenerator.setMessagesList(messages);
      contentGenerator.setParameterProviders(parameterProviders);
      contentGenerator.setSession(session);
      contentGenerator.setUrlFactory(urlFactory);
      
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();
      
      MockServletConfig servletConfig = new MockServletConfig();
      MockServletContext servletContext = new MockServletContext();
      servletConfig.setServletContext( servletContext );
      
      pathParams.setParameter("servletconfig", servletConfig); //$NON-NLS-1$

      request.setPathInfo( "/ws-run/ModelService" ); //$NON-NLS-1$
      request.setContentType( "application/x-www-form-urlencoded;action=:abc-listModels:" ); //$NON-NLS-1$
      request.setMethod( "GET" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-run/ModelService" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/ModelService/listModels" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      
      try {
        
        IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", "", null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( "content type is wrong", null, contentItem.getMimeType() ); //$NON-NLS-1$
        contentGenerator.setContentType( "text/xml" ); //$NON-NLS-1$
        contentItem = outputHandler.getOutputContentItem( "response", "content", "", null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( "content type is wrong", "text/xml", contentItem.getMimeType() ); //$NON-NLS-1$ //$NON-NLS-2$
        
        StubTransportSender.transportOutStr = null;
          contentGenerator.createContent();

          String content = StubTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<ns:listModelsResponse xmlns:ns=\"http://server.metadata.services.webservice.platform.pentaho.org\" xmlns:ax22=\"http://envelope.v3.schema.pms.pentaho.org/xsd\" xmlns:ax21=\"http://model.v3.schema.pms.pentaho.org/xsd\"><return type=\"org.pentaho.pms.schema.v3.model.ModelEnvelope\"><description>Orders</description><id>Orders</id><name>Orders</name><domain>models</domain></return></ns:listModelsResponse>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }
/*
  @Test
  public void testListModelsJson() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession(session);
    AxisConfig config =  AxisConfig.getInstance( setup );

    TransportInDescription tIn = new TransportInDescription( "http" ); //$NON-NLS-1$
    StubTransportListener receiver = new StubTransportListener();
    tIn.setReceiver(receiver);
    config.getAxisConfigurator().getAxisConfiguration().addTransportIn(tIn);
    
    TransportOutDescription tOut = new TransportOutDescription( "http" ); //$NON-NLS-1$
    StubTransportSender sender = new StubTransportSender();
    tOut.setSender(sender);
    config.getAxisConfigurator().getAxisConfiguration().addTransportOut(tOut);

    LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(config.getAxisConfigurator().getAxisConfiguration());
    
    RunJsonService contentGenerator = new RunJsonService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/ModelService/listModels?{}");  //$NON-NLS-1$//$NON-NLS-2$
      pathParams.setParameter( "remoteaddr" , "http:test");  //$NON-NLS-1$//$NON-NLS-2$
      parameterProviders.put( "path", pathParams ); //$NON-NLS-1$
        SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl+"?" ); //$NON-NLS-1$
      List<String> messages = new ArrayList<String>();
      contentGenerator.setOutputHandler(outputHandler);
      MimeTypeListener mimeTypeListener = new MimeTypeListener();
      outputHandler.setMimeTypeListener(mimeTypeListener);
      contentGenerator.setMessagesList(messages);
      contentGenerator.setParameterProviders(parameterProviders);
      contentGenerator.setSession(session);
      contentGenerator.setUrlFactory(urlFactory);
      
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();
      
      MockServletConfig servletConfig = new MockServletConfig();
      MockServletContext servletContext = new MockServletContext();
      servletConfig.setServletContext( servletContext );
      
      pathParams.setParameter("servletconfig", servletConfig); //$NON-NLS-1$

      request.setPathInfo( "/ws-json1/ModelService" ); //$NON-NLS-1$
      request.setContentType( "application/x-www-form-urlencoded;action=:abc-listModels:" ); //$NON-NLS-1$
      request.setMethod( "POST" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-json1/ModelService" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-json1/ModelService/listModels" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      request.setBodyContent("\"ignore\" : {}"); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      
      try {
        
        IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", "", null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( "content type is wrong", null, contentItem.getMimeType() ); //$NON-NLS-1$
        contentGenerator.setContentType( "text/xml" ); //$NON-NLS-1$
        contentItem = outputHandler.getOutputContentItem( "response", "content", "", null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( "content type is wrong", "text/xml", contentItem.getMimeType() ); //$NON-NLS-1$ //$NON-NLS-2$
        
        StubTransportSender.transportOutStr = null;
          contentGenerator.createContent();

          String content = StubTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<ns:listModelsResponse xmlns:ns=\"http://server.metadata.services.webservice.platform.pentaho.org\" xmlns:ax22=\"http://envelope.v3.schema.pms.pentaho.org/xsd\" xmlns:ax21=\"http://model.v3.schema.pms.pentaho.org/xsd\"><return type=\"org.pentaho.pms.schema.v3.model.ModelEnvelope\"><description>Orders</description><id>Orders</id><name>Orders</name><domain>models</domain></return></ns:listModelsResponse>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  @Test
  public void testGetModelJson() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession(session);
    AxisConfig config =  AxisConfig.getInstance( setup );

    TransportInDescription tIn = new TransportInDescription( "http" ); //$NON-NLS-1$
    StubTransportListener receiver = new StubTransportListener();
    tIn.setReceiver(receiver);
    config.getAxisConfigurator().getAxisConfiguration().addTransportIn(tIn);
    
    TransportOutDescription tOut = new TransportOutDescription( "http" ); //$NON-NLS-1$
    StubTransportSender sender = new StubTransportSender();
    tOut.setSender(sender);
    config.getAxisConfigurator().getAxisConfiguration().addTransportOut(tOut);

    LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(config.getAxisConfigurator().getAxisConfiguration());
    
    RunJsonService contentGenerator = new RunJsonService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/ModelService/getModel?{domain : 'steel-wheels', id='BV_ORDERS', deep : false }");  //$NON-NLS-1$//$NON-NLS-2$
      pathParams.setParameter( "remoteaddr" , "http:test");  //$NON-NLS-1$//$NON-NLS-2$
      parameterProviders.put( "path", pathParams ); //$NON-NLS-1$
        SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl+"?" ); //$NON-NLS-1$
      List<String> messages = new ArrayList<String>();
      contentGenerator.setOutputHandler(outputHandler);
      MimeTypeListener mimeTypeListener = new MimeTypeListener();
      outputHandler.setMimeTypeListener(mimeTypeListener);
      contentGenerator.setMessagesList(messages);
      contentGenerator.setParameterProviders(parameterProviders);
      contentGenerator.setSession(session);
      contentGenerator.setUrlFactory(urlFactory);
      
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();
      
      MockServletConfig servletConfig = new MockServletConfig();
      MockServletContext servletContext = new MockServletContext();
      servletConfig.setServletContext( servletContext );
      
      pathParams.setParameter("servletconfig", servletConfig); //$NON-NLS-1$

      request.setPathInfo( "/ws-json1/ModelService" ); //$NON-NLS-1$
      request.setContentType( "application/x-www-form-urlencoded;action=:abc-getModel:" ); //$NON-NLS-1$
      request.setMethod( "POST" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-json1/getModel" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-json1/ModelService/getModel" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      request.setBodyContent("{\"server.metadata.services.webservice.platform.pentaho.org\":{\"data\":\"my json string\"}}"); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      
      try {
        
        IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", "", null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( "content type is wrong", null, contentItem.getMimeType() ); //$NON-NLS-1$
        contentGenerator.setContentType( "text/xml" ); //$NON-NLS-1$
        contentItem = outputHandler.getOutputContentItem( "response", "content", "", null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( "content type is wrong", "text/xml", contentItem.getMimeType() ); //$NON-NLS-1$ //$NON-NLS-2$
        
        StubTransportSender.transportOutStr = null;
          contentGenerator.createContent();

          String content = StubTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<ns:getModelResponse xmlns:ns=\"http://server.metadata.services.webservice.platform.pentaho.org\" xmlns:ax22=\"http://envelope.v3.schema.pms.pentaho.org/xsd\" xmlns:ax21=\"http://model.v3.schema.pms.pentaho.org/xsd\"><return type=\"org.pentaho.pms.schema.v3.model.ModelEnvelope\"><description>Orders</description><id>Orders</id><name>Orders</name><domain>models</domain></return></ns:listModelsResponse>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }
*/
}
