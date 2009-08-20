/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 3 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Apr 18, 2006
 *
 * @author mbatchel
 */
package org.pentaho.test.platform.security;

import java.io.File;
import java.security.Principal;

import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

@SuppressWarnings("nls")
public class TestSecurityUtils extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if(file.exists()) {
      return SOLUTION_PATH;  
    } else {      
      return ALT_SOLUTION_PATH;
    }
    
  }	
  public static void main(String[] args) {
    junit.textui.TestRunner.run(TestSecurityUtils.class);
    System.exit(0);
  }

  public void testUtils() {
    // First, an anonymous user...
    StandaloneSession session = new StandaloneSession(null);
    Authentication auth = SecurityHelper.getAuthentication(session, false);
    assertNull(auth);
    // Now, allow anonymous, check for role/user information
    auth = SecurityHelper.getAuthentication(session, true);
    assertNotNull(auth);
    assertEquals(auth.getName(), SecurityHelper.DefaultAnonymousUser);
    GrantedAuthority[] authList = auth.getAuthorities();
    assertEquals(authList.length, 1);
    assertEquals(authList[0].getAuthority(), SecurityHelper.DefaultAnonymousRole);

    // Mock up credentials
    MockSecurityUtility.createPat(session);
    // Now, we have a session, and an authenticated user bound to the session.
    auth = SecurityHelper.getAuthentication(session, false);
    assertNotNull(auth);
    assertEquals(auth.getName(), "pat"); //$NON-NLS-1$
    assertFalse(SecurityHelper.isPentahoAdministrator(session));
    assertTrue(SecurityHelper.isGranted(session, new GrantedAuthorityImpl("ROLE_DEV"))); //$NON-NLS-1$
    assertFalse(SecurityHelper.isGranted(session, new GrantedAuthorityImpl("ROLE_ADMIN"))); //$NON-NLS-1$

    // Principal Checking
    Principal principal = SecurityHelper.getPrincipal(session);
    assertNotNull(principal);
    assertEquals(principal.getName(), "pat"); //$NON-NLS-1$

    // Now, create an admin user and bind it to the session
    MockSecurityUtility.createJoe(session);
    assertTrue(SecurityHelper.isPentahoAdministrator(session));

  }

}
