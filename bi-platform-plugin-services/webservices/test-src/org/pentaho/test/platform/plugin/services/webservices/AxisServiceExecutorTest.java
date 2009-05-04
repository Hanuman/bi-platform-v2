/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.plugin.services.webservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.junit.Test;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.AxisWebServiceManager;
import org.pentaho.platform.plugin.services.webservices.content.AxisServiceExecutor;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;

public class AxisServiceExecutorTest {

  public AxisServiceExecutorTest() {
  }

  @Test
  public void testRunGet1() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession(session);

    /*
    // create a test transport so we can catch the output
    config.setTransportOut( "http" ); //$NON-NLS-1$
    
    assertEquals( "Transport is wrong", "http", config.getTransportOut() ); //$NON-NLS-1$ //$NON-NLS-2$
*/
    AxisConfiguration axisConfig = AxisWebServiceManager.currentAxisConfiguration;
    
    TransportInDescription tIn = new TransportInDescription( "http" ); //$NON-NLS-1$
    StubTransportListener receiver = new StubTransportListener();
    tIn.setReceiver(receiver);
    axisConfig.addTransportIn(tIn);
    
    TransportOutDescription tOut = new TransportOutDescription( "http" ); //$NON-NLS-1$
    StubTransportSender sender = new StubTransportSender();
    tOut.setSender(sender);
    axisConfig.addTransportOut(tOut);

    LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(axisConfig);
    
    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/StubService/getString");  //$NON-NLS-1$//$NON-NLS-2$
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
      request.setRequestURI( "/pentaho/content/ws-run/StubService/getString" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/getString" ); //$NON-NLS-1$
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
        StubService.getStringCalled = false;
          contentGenerator.createContent();

          assertTrue( StubService.getStringCalled );
          String content = StubTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<ns:getStringResponse xmlns:ns=\"http://webservices.services.plugin.platform.test.pentaho.org\"><return>test result</return></ns:getStringResponse>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  @Test
  public void testRunGet2() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/StubService/setString?str=testinput");  //$NON-NLS-1$//$NON-NLS-2$
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
      request.setRequestURI( "/pentaho/content/ws-run/StubService/setString" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/getString" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      request.setQueryString("str=testinput"); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$

      try {
        StubService.setStringCalled = false;
        StubTransportSender.transportOutStr = null;
          contentGenerator.createContent();
          assertTrue( StubService.setStringCalled );
          assertEquals( "testinput", StubService.str ); //$NON-NLS-1$
          String content = StubTransportSender.transportOutStr;
        System.out.println( content );
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
      
  }

  @Test
  public void testRunGet3() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/StubService/throwsError1");  //$NON-NLS-1$//$NON-NLS-2$
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
      request.setRequestURI( "/pentaho/content/ws-run/StubService/throwsError1" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/throwsError1" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      try {
        StubService.throwsError1Called = false;
        StubTransportSender.transportOutStr = null;
          contentGenerator.createContent();
          assertTrue( StubService.throwsError1Called );
          String content = StubTransportSender.transportOutStr;
          assertEquals( "Content should be empty", null, content ); //$NON-NLS-1$
        System.out.println( content );
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
      
  }

  @Test
  public void testRunGet4() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/StubService/throwsError2");  //$NON-NLS-1$//$NON-NLS-2$
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
      request.setRequestURI( "/pentaho/content/ws-run/StubService/throwsError2" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/throwsError2" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$

      try {
        StubService.throwsError2Called = false;
        StubTransportSender.transportOutStr = null;
          contentGenerator.createContent();
          assertTrue( StubService.throwsError2Called );
          String content = StubTransportSender.transportOutStr;
          
          assertTrue( "results are wrong", content.indexOf( "soapenv:Fault" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "results are wrong", content.indexOf( "test error 2" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
          
        System.out.println( content );
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  @Test
  public void testRunGet5() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/StubService/bogus");  //$NON-NLS-1$//$NON-NLS-2$
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
      request.setRequestURI( "/pentaho/content/ws-run/StubService/bogus" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/bogus" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$

      try {
        StubTransportSender.transportOutStr = null;
          contentGenerator.createContent();
          String content = StubTransportSender.transportOutStr;
          System.out.println( content );
          
          assertTrue( "results are wrong", content.indexOf( "soapenv:Fault" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "results are wrong", content.indexOf( "AxisServletHooks" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
          
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  @Test
  public void testRunPost1() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
        
    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/StubService");  //$NON-NLS-1$//$NON-NLS-2$
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
      request.setRequestURI( "/pentaho/content/ws-run/StubService" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      request.setContentType( "application/soap+xml; charset=UTF-8; action=\"urn:getString\"" ); //$NON-NLS-1$
      String xml = "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns2:getString xmlns:ns2=\"http://webservice.pentaho.com\"></ns2:getString></soapenv:Body></soapenv:Envelope>"; //$NON-NLS-1$
      request.setBodyContent( xml );
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      
      try {
        StubTransportSender.transportOutStr = null;
        StubService.getStringCalled = false;
          contentGenerator.createContent();

          assertTrue( StubService.getStringCalled );
          String content = StubTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns:getStringResponse xmlns:ns=\"http://webservices.services.plugin.platform.test.pentaho.org\"><return>test result</return></ns:getStringResponse></soapenv:Body></soapenv:Envelope>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

  @Test
  public void testRunPut1() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
        
    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();
    
    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
      assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
      
      String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
      Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
      SimpleParameterProvider requestParams = new SimpleParameterProvider();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "path" , "/StubService");  //$NON-NLS-1$//$NON-NLS-2$
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
      request.setRequestURI( "/pentaho/content/ws-run/StubService" ); //$NON-NLS-1$
      request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService" ); //$NON-NLS-1$
      request.setRemoteAddr( "127.0.0.1" ); //$NON-NLS-1$
      request.setContentType( "application/soap+xml; charset=UTF-8; action=\"urn:getString\"" ); //$NON-NLS-1$
      String xml = "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns2:getString xmlns:ns2=\"http://webservice.pentaho.com\"></ns2:getString></soapenv:Body></soapenv:Envelope>"; //$NON-NLS-1$
      request.setBodyContent( xml );
      
      pathParams.setParameter( "httprequest" , request ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse" , response ); //$NON-NLS-1$
      
      try {
        StubTransportSender.transportOutStr = null;
        StubService.getStringCalled = false;
          contentGenerator.createContent();

          assertTrue( StubService.getStringCalled );
          String content = StubTransportSender.transportOutStr;
          assertEquals( "result are wrong", "<ns:getStringResponse xmlns:ns=\"http://webservices.services.plugin.platform.test.pentaho.org\"><return>test result</return></ns:getStringResponse>", content );  //$NON-NLS-1$//$NON-NLS-2$
        System.out.println( content );
        
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue( "Exception occurred", false ); //$NON-NLS-1$
      }
  }

}
