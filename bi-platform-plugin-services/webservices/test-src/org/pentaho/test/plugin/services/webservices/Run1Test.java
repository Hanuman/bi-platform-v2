package org.pentaho.test.plugin.services.webservices;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.webservices.AxisConfig;
import org.pentaho.platform.plugin.services.webservices.content.RunService;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;

public class Run1Test extends TestCase {

  public Run1Test() {
  }

  public void testRunGet1() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    TestServiceSetup setup = new TestServiceSetup();
    setup.setSession(session);
    AxisConfig config =  AxisConfig.getInstance( setup );

    /*
    // create a test transport so we can catch the output
    config.setTransportOut( "http" ); //$NON-NLS-1$
    
    assertEquals( "Transport is wrong", "http", config.getTransportOut() ); //$NON-NLS-1$ //$NON-NLS-2$
*/
    TransportInDescription tIn = new TransportInDescription( "http" ); //$NON-NLS-1$
    TestTransportListener receiver = new TestTransportListener();
    tIn.setReceiver(receiver);
    config.getAxisConfigurator().getAxisConfiguration().addTransportIn(tIn);
    
    TransportOutDescription tOut = new TransportOutDescription( "http" ); //$NON-NLS-1$
    TestTransportSender sender = new TestTransportSender();
    tOut.setSender(sender);
    config.getAxisConfigurator().getAxisConfiguration().addTransportOut(tOut);

    LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(config.getAxisConfigurator().getAxisConfiguration());
    
    RunService contentGenerator = new RunService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/TestService/getString");  //$NON-NLS-1$//$NON-NLS-2$
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

      request.setMethod( "GET" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-run/TestService/getString" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/TestService/getString" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      
      try {
        
        IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", "", null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( "content type is wrong", null, contentItem.getMimeType() ); //$NON-NLS-1$
        contentGenerator.setContentType( "text/xml" ); //$NON-NLS-1$
        contentItem = outputHandler.getOutputContentItem( "response", "content", "", null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( "content type is wrong", "text/xml", contentItem.getMimeType() ); //$NON-NLS-1$ //$NON-NLS-2$
        
        TestTransportSender.transportOutStr = null;
        TestService.getStringCalled = false;
          contentGenerator.createContent();

          assertTrue( TestService.getStringCalled );
          String content = TestTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<ns:getStringResponse xmlns:ns=\"http://core.webservice.test.pentaho.org\"><return>test result</return></ns:getStringResponse>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  public void testRunGet2() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    RunService contentGenerator = new RunService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/TestService/setString?str=testinput");  //$NON-NLS-1$//$NON-NLS-2$
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

      request.setMethod( "GET" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-run/TestService/setString" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/TestService/getString" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      request.setQueryString("str=testinput"); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$

      try {
        TestService.setStringCalled = false;
        TestTransportSender.transportOutStr = null;
          contentGenerator.createContent();
          assertTrue( TestService.setStringCalled );
          assertEquals( "testinput", TestService.str ); //$NON-NLS-1$
          String content = TestTransportSender.transportOutStr;
        System.out.println( content );
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
      
  }

  public void testRunGet3() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    RunService contentGenerator = new RunService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/TestService/throwsError1");  //$NON-NLS-1$//$NON-NLS-2$
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

      request.setMethod( "GET" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-run/TestService/throwsError1" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/TestService/throwsError1" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      try {
        TestService.throwsError1Called = false;
        TestTransportSender.transportOutStr = null;
          contentGenerator.createContent();
          assertTrue( TestService.throwsError1Called );
          String content = TestTransportSender.transportOutStr;
          assertEquals( "Content should be empty", null, content ); //$NON-NLS-1$
        System.out.println( content );
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
      
  }

  public void testRunGet4() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    RunService contentGenerator = new RunService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/TestService/throwsError2");  //$NON-NLS-1$//$NON-NLS-2$
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

      request.setMethod( "GET" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-run/TestService/throwsError2" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/TestService/throwsError2" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$

      try {
        TestService.throwsError2Called = false;
        TestTransportSender.transportOutStr = null;
          contentGenerator.createContent();
          assertTrue( TestService.throwsError2Called );
          String content = TestTransportSender.transportOutStr;
          
          assertTrue( "results are wrong", content.indexOf( "soapenv:Fault" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "results are wrong", content.indexOf( "test error 2" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
          
        System.out.println( content );
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  public void testRunGet5() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    RunService contentGenerator = new RunService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/TestService/bogus");  //$NON-NLS-1$//$NON-NLS-2$
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

      request.setMethod( "GET" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-run/TestService/bogus" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/TestService/bogus" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$

      try {
        TestTransportSender.transportOutStr = null;
          contentGenerator.createContent();
          String content = TestTransportSender.transportOutStr;
          System.out.println( content );
          
          assertTrue( "results are wrong", content.indexOf( "soapenv:Fault" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "results are wrong", content.indexOf( "AxisServletHooks" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
          
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  public void testRunPost1() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
        
    RunService contentGenerator = new RunService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/TestService");  //$NON-NLS-1$//$NON-NLS-2$
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

      request.setMethod( "POST" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-run/TestService" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/TestService" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      request.setContentType( "application/soap+xml; charset=UTF-8; action=\"urn:getString\"" ); //$NON-NLS-1$
      String xml = "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns2:getString xmlns:ns2=\"http://webservice.pentaho.com\"></ns2:getString></soapenv:Body></soapenv:Envelope>"; //$NON-NLS-1$
      request.setBodyContent( xml );
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      
      try {
        TestTransportSender.transportOutStr = null;
        TestService.getStringCalled = false;
          contentGenerator.createContent();

          assertTrue( TestService.getStringCalled );
          String content = TestTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns:getStringResponse xmlns:ns=\"http://core.webservice.test.pentaho.org\"><return>test result</return></ns:getStringResponse></soapenv:Body></soapenv:Envelope>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  public void testRunPut1() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
        
    RunService contentGenerator = new RunService();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/TestService");  //$NON-NLS-1$//$NON-NLS-2$
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

      request.setMethod( "PUT" ); //$NON-NLS-1$
      request.setRequestURI( "/pentaho/content/ws-run/TestService" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/TestService" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      request.setContentType( "application/soap+xml; charset=UTF-8; action=\"urn:getString\"" ); //$NON-NLS-1$
      String xml = "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns2:getString xmlns:ns2=\"http://webservice.pentaho.com\"></ns2:getString></soapenv:Body></soapenv:Envelope>"; //$NON-NLS-1$
      request.setBodyContent( xml );
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      
      try {
        TestTransportSender.transportOutStr = null;
        TestService.getStringCalled = false;
          contentGenerator.createContent();

          assertTrue( TestService.getStringCalled );
          String content = TestTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<ns:getStringResponse xmlns:ns=\"http://core.webservice.test.pentaho.org\"><return>test result</return></ns:getStringResponse>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

}
