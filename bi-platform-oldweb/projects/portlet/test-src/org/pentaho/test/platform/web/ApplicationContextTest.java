/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Aug 15, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.web;

import org.apache.commons.logging.Log;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SettingsPublisher;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.web.http.context.WebApplicationContext;
import org.pentaho.platform.web.portal.PortletApplicationContext;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ApplicationContextTest extends BaseTest {

  public void testPortletApplicationContext() {
    startTest();
    
    PortletApplicationContext pac = new PortletApplicationContext(PentahoSystem.getApplicationContext().getSolutionPath("test"), "http://localhost:8080/pentaho", "http://localhost:8080/pentaho"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    assertEquals(pac.getBaseUrl(), "http://localhost:8080/pentaho/"); //$NON-NLS-1$    
    assertEquals(pac.getSolutionRootPath(), PentahoSystem.getApplicationContext().getSolutionPath("test")); //$NON-NLS-1$

    finishTest();
  }

  public void testStandaloneApplicationContext() {
    startTest();
    
    StandaloneApplicationContext sac = new StandaloneApplicationContext(PentahoSystem.getApplicationContext().getSolutionPath("test"), "http://localhost:8080/pentaho/"); //$NON-NLS-1$ //$NON-NLS-2$
    sac.setBaseUrl("http://localhost:8080/pentaho");
    SettingsPublisher publisher = new SettingsPublisher();
    StandaloneSession session = new StandaloneSession("BaseTest.DEBUG_JUNIT_SESSION"); //$NON-NLS-1$
    publisher.publish(session);
    Log logger = publisher.getLogger();
    logger.debug("This is a basic test for the logger"); //$NON-NLS-1$

    assertEquals(sac.getBaseUrl(), "http://localhost:8080/pentaho"); //$NON-NLS-1$    
    assertEquals(sac.getSolutionRootPath(), PentahoSystem.getApplicationContext().getSolutionPath("test")); //$NON-NLS-1$

    finishTest();
  }
  
  public void testWebApplicationContext() {
    startTest();
    
    WebApplicationContext wac = new WebApplicationContext(PentahoSystem.getApplicationContext().getSolutionPath("test"), "http://localhost:8080/pentaho", "http://localhost:8080/pentaho",  new Object()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    wac.setBaseUrl("http://localhost:8080/pentaho");
    String url = wac.getBaseUrl();
    WebApplicationContext wac1 = new WebApplicationContext(PentahoSystem.getApplicationContext().getSolutionPath("test"), "http://localhost:8080/pentaho", "http://localhost:8080/pentaho"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$    
    wac1.setBaseUrl("http://localhost:8080/pentaho");

    assertEquals(wac.getBaseUrl(), "http://localhost:8080/pentaho/"); //$NON-NLS-1$    
    assertEquals(wac.getSolutionRootPath(), PentahoSystem.getApplicationContext().getSolutionPath("test")); //$NON-NLS-1$
      
    assertEquals(wac1.getBaseUrl(), "http://localhost:8080/pentaho/"); //$NON-NLS-1$    
    assertEquals(wac1.getSolutionRootPath(), PentahoSystem.getApplicationContext().getSolutionPath("test")); //$NON-NLS-1$
    
    finishTest();
  }
  
  public static void main(String[] args) {
    ApplicationContextTest test = new ApplicationContextTest();
    test.setUp();
    test.testPortletApplicationContext();
    test.testStandaloneApplicationContext();
    test.testWebApplicationContext();
    try {

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
