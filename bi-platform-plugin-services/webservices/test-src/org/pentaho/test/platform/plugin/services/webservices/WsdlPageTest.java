package org.pentaho.test.platform.plugin.services.webservices;

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
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.webservices.AxisConfig;
import org.pentaho.platform.plugin.services.webservices.content.ServiceWsdl;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;
import org.pentaho.platform.util.web.SimpleUrlFactory;

public class WsdlPageTest extends TestCase {

  public WsdlPageTest() {
  }

	  public void testRender() throws Exception {
		  
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

      ServiceWsdl contentGenerator = new ServiceWsdl();
	    
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
	    	try {
	        	contentGenerator.createContent();
	        	String content = new String( out.toByteArray() );
          System.out.println( content );
          
          assertTrue( "wsdl:definitions is missing", content.indexOf( "wsdl:definitions" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "targetNamespace is missing", content.indexOf( "targetNamespace=\"http://webservice.pentaho.com\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

          assertTrue( "<xs:complexType name=\"ComplexType\">", content.indexOf( "<xs:complexType name=\"ComplexType\">" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "name=\"address\"", content.indexOf( "name=\"address\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "name=\"age\"", content.indexOf( "name=\"age\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "name=\"name\"", content.indexOf( "name=\"name\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

          assertTrue( "setStringRequest", content.indexOf( "setStringRequest" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "getStringResponse", content.indexOf( "getStringResponse" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "urn:setString", content.indexOf( "urn:setString" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "urn:getString", content.indexOf( "urn:getString" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "urn:getStringResponse", content.indexOf( "urn:getStringResponse" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "TestServiceSoap11Binding", content.indexOf( "TestServiceSoap11Binding" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "<wsdl:operation name=\"setString\">", content.indexOf( "<wsdl:operation name=\"setString\">" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "<wsdl:binding name=\"TestServiceHttpBinding\"", content.indexOf( "<wsdl:binding name=\"TestServiceHttpBinding\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "<wsdl:operation name=\"getString\">", content.indexOf( "<wsdl:operation name=\"getString\">" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "TestServiceSoap12Binding", content.indexOf( "TestServiceSoap12Binding" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "<wsdl:service name=\"TestService\">", content.indexOf( "<wsdl:service name=\"TestService\">" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
          assertTrue( "http://testhost:8080/testcontext/content/ws-run/TestService", content.indexOf( "http://testhost:8080/testcontext/content/ws-run/TestService" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

	    	} catch (Exception e) {
	    		assertTrue( "Exception occurred", false ); //$NON-NLS-1$
	    	}
	  }

	   public void testMissingPathParamProvider() throws Exception {
	      
	      StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
	      
	      StubServiceSetup setup = new StubServiceSetup();
	      setup.setSession(session);
	      AxisConfig.getInstance( setup );
	      
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
	            assertTrue( content.indexOf( Messages.getErrorString("WebServiceContentGenerator.ERROR_0004_PATH_PARAMS_IS_MISSING") ) != -1 ); //$NON-NLS-1$
	          System.out.println( content );
	          
	        } catch (Exception e) {
	          assertTrue( "Exception occurred", false ); //$NON-NLS-1$
	        }
	    }

	    public void testMissingServiceName() throws Exception {
	      
	      StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
	      
	      StubServiceSetup setup = new StubServiceSetup();
	      setup.setSession(session);
	      AxisConfig.getInstance( setup );
	      
	      ServiceWsdl contentGenerator = new ServiceWsdl();
	      
	      assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
	        assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
	        
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
	        
	        String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
	        Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
	        SimpleParameterProvider requestParams = new SimpleParameterProvider();
	        parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
	        SimpleParameterProvider pathParams = new SimpleParameterProvider();
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
	        try {
	            contentGenerator.createContent();
	            String content = new String( out.toByteArray() );
	          System.out.println( content );
            assertTrue( content.indexOf( Messages.getErrorString("WebServiceContentGenerator.ERROR_0005_SERVICE_NAME_IS_MISSING") ) != -1 ); //$NON-NLS-1$
	          
	        } catch (Exception e) {
	          assertTrue( "Exception occurred", false ); //$NON-NLS-1$
	        }
	    }

	    public void testBadServiceName() throws Exception {
	      
	      StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
	      
	      StubServiceSetup setup = new StubServiceSetup();
	      setup.setSession(session);
	      AxisConfig.getInstance( setup );
	      
	      ServiceWsdl contentGenerator = new ServiceWsdl();
	      
	      assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
	        assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$
	        
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
	        
	        String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
	        Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
	        SimpleParameterProvider requestParams = new SimpleParameterProvider();
	        parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
	        SimpleParameterProvider pathParams = new SimpleParameterProvider();
	        pathParams.setParameter( "path" , "/bogus");  //$NON-NLS-1$//$NON-NLS-2$
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
	        try {
	            contentGenerator.createContent();
	            String content = new String( out.toByteArray() );
	            System.out.println( content );
	            assertTrue( content.indexOf( Messages.getErrorString("WebServiceContentGenerator.ERROR_0006_SERVICE_IS_INVALID", "bogus") ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
	        } catch (Exception e) {
	          assertTrue( "Exception occurred", false ); //$NON-NLS-1$
	        }
	    }

}
