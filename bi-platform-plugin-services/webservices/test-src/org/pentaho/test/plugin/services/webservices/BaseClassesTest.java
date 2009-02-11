package org.pentaho.test.plugin.services.webservices;

import junit.framework.TestCase;

import org.apache.axis2.description.AxisService;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.webservices.AxisConfig;
import org.pentaho.platform.plugin.webservices.IWebServiceConfigurator;
import org.pentaho.platform.plugin.webservices.IWebServiceWrapper;
import org.pentaho.platform.plugin.webservices.SessionHandler;
import org.pentaho.plugin.services.webservices.content.WebServiceConst;


public class BaseClassesTest extends TestCase {

  public void testInit() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    TestServiceSetup setup = new TestServiceSetup();
    setup.setSession(session);
    AxisConfig config = AxisConfig.getInstance( setup );
    assertNotNull( "AxisConfig is null", config ); //$NON-NLS-1$
    
    IWebServiceConfigurator axisConfigurator = config.getAxisConfigurator();

    assertNotNull( "axisConfigurator is null", axisConfigurator ); //$NON-NLS-1$
    
    axisConfigurator.setSession( session );
    
    AxisService service = config.getConfigurationContext().getAxisConfiguration().getService( "TestService" ); //$NON-NLS-1$
    assertNotNull( "test service is missing", service ); //$NON-NLS-1$
    
    IWebServiceWrapper wrapper = axisConfigurator.getServiceWrapper( "TestService" ); //$NON-NLS-1$
    assertNotNull( "wrapper is null", wrapper ); //$NON-NLS-1$

    assertEquals( service, wrapper.getService() );
    config.reset();
    
    service = config.getConfigurationContext().getAxisConfiguration().getService( "TestService" ); //$NON-NLS-1$
    assertNotNull( "test service is missing after reset", service ); //$NON-NLS-1$
    
    axisConfigurator.unloadServices();
    
    service = config.getConfigurationContext().getAxisConfiguration().getService( "TestService" ); //$NON-NLS-1$
    assertNull( "test service is still there", service ); //$NON-NLS-1$
    
    axisConfigurator.reloadServices();

    service = config.getConfigurationContext().getAxisConfiguration().getService( "TestService" ); //$NON-NLS-1$
    assertNotNull( "test service is missing after reset", service ); //$NON-NLS-1$

    axisConfigurator.cleanup();
    
  }
  
  public void testSessionHandler() {

    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    assertNull( SessionHandler.getSession() );
    
    new SessionHandler();
    
    SessionHandler.setSession(session);
    assertNotNull( SessionHandler.getSession() );
    assertEquals( session, SessionHandler.getSession() );
    
    SessionHandler.removeSession();
    assertNull( SessionHandler.getSession() );
  }
  
  public void testPluginConst() {
    
    new WebServiceConst();
    
    assertEquals( "admin url is wrong", "http://testhost:8080/testcontext/content/ws-services", WebServiceConst.getDiscoveryUrl() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "admin url is wrong", "http://testhost:8080/testcontext/content/ws-run", WebServiceConst.getExecuteUrl() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "admin url is wrong", "http://testhost:8080/testcontext/content/ws-wsdl", WebServiceConst.getWsdlUrl() ); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
}
