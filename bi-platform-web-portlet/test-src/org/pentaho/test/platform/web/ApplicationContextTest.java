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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Aug 15, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.web;

import java.io.File;

import org.apache.commons.logging.Log;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SettingsPublisher;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.web.http.context.WebApplicationContext;
import org.pentaho.platform.web.portal.PortletApplicationContext;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ApplicationContextTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }
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
