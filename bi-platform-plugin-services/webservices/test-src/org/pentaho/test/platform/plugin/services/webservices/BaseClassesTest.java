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

import junit.framework.TestCase;

import org.apache.axis2.description.AxisService;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.webservices.AxisConfig;
import org.pentaho.platform.plugin.services.webservices.IWebServiceConfigurator;
import org.pentaho.platform.plugin.services.webservices.IWebServiceWrapper;
import org.pentaho.platform.plugin.services.webservices.SessionHandler;
import org.pentaho.platform.plugin.services.webservices.content.WebServiceConst;


public class BaseClassesTest extends TestCase {

  public void testInit() throws Exception {
    
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$
    
    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession(session);
    AxisConfig config = AxisConfig.getInstance( setup );
    assertNotNull( "AxisConfig is null", config ); //$NON-NLS-1$
    
    IWebServiceConfigurator axisConfigurator = config.getAxisConfigurator();

    assertNotNull( "axisConfigurator is null", axisConfigurator ); //$NON-NLS-1$
    
    axisConfigurator.setSession( session );
    
    AxisService service = config.getConfigurationContext().getAxisConfiguration().getService( "StubService" ); //$NON-NLS-1$
    assertNotNull( "test service is missing", service ); //$NON-NLS-1$
    
    IWebServiceWrapper wrapper = axisConfigurator.getServiceWrapper( "StubService" ); //$NON-NLS-1$
    assertNotNull( "wrapper is null", wrapper ); //$NON-NLS-1$

    assertEquals( service, wrapper.getService() );
    config.reset();
    
    service = config.getConfigurationContext().getAxisConfiguration().getService( "StubService" ); //$NON-NLS-1$
    assertNotNull( "test service is missing after reset", service ); //$NON-NLS-1$
    
    axisConfigurator.unloadServices();
    
    service = config.getConfigurationContext().getAxisConfiguration().getService( "StubService" ); //$NON-NLS-1$
    assertNull( "test service is still there", service ); //$NON-NLS-1$
    
    axisConfigurator.reloadServices();

    service = config.getConfigurationContext().getAxisConfiguration().getService( "StubService" ); //$NON-NLS-1$
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
