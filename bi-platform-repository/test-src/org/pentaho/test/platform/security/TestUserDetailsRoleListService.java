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
import java.util.List;

import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.security.userrole.UserDetailsRoleListService;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class TestUserDetailsRoleListService extends BaseTest {
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
	  
  //
  // This test case is specifically not extending BaseSecurityTest
  // so that it can always test the output of the class.
  //
  public static void main(String[] args) {
    junit.textui.TestRunner.run(TestUserDetailsRoleListService.class);
    System.exit(0);
  }

  public void testUserDetails() {
    // Setup the environment to test the class...
    IUserRoleListService mock = (IUserRoleListService) new MockUserRoleListService();
    IUserDetailsRoleListService service = new UserDetailsRoleListService();
    try {
      service.afterPropertiesSet();
      fail("Should have thrown an exception.");//$NON-NLS-1$
    } catch (Exception ignored) {
    }
    service.setUserRoleListService(mock);
    try {
      service.afterPropertiesSet();
    } catch (Exception ex) {
      fail("Should not have thrown an exception."); //$NON-NLS-1$
    }

    List allRoles = service.getAllRoles();
    assertNotNull(allRoles);
    assertEquals(allRoles.size(), 7); // Should have exactly 7 roles
    assertEquals(allRoles.get(0), "ROLE_DEV");//$NON-NLS-1$
    assertEquals(allRoles.get(6), "ROLE_IS"); //$NON-NLS-1$

    List allUsers = service.getAllUsers();
    assertNotNull(allUsers);
    assertEquals(allUsers.size(), 4);
    assertEquals(allUsers.get(0), "pat");//$NON-NLS-1$
    assertEquals(allUsers.get(3), "suzy");//$NON-NLS-1$

    List allUsersInRole = service.getAllUsersInRole("ROLE_DEV");//$NON-NLS-1$
    assertNotNull(allUsersInRole);
    assertEquals(allUsersInRole.size(), 2);
    assertEquals(allUsersInRole.get(0), "pat");//$NON-NLS-1$
    assertEquals(allUsersInRole.get(1), "tiffany");//$NON-NLS-1$

  }

}
