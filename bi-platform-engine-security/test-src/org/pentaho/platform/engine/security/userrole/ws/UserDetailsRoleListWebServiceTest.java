package org.pentaho.platform.engine.security.userrole.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class UserDetailsRoleListWebServiceTest {
  private MicroPlatform microPlatform;

  @Before
  public void init0() {
    microPlatform = new MicroPlatform();
    microPlatform.define(IUserDetailsRoleListService.class, MockUserDetailsRoleListService.class);
  }
  
  public IUserDetailsRoleListWebService getUserDetailsRoleListWebService() {
    return new DefaultUserDetailsRoleListWebService();
  }
  @Test
  public void testGetAllRoles()  throws Exception {
    IUserDetailsRoleListWebService service = getUserDetailsRoleListWebService();

    try {
      List<String> allRoles = service.getAllRoles();
      assertNotNull(allRoles);
      assertEquals(allRoles.size(), 7); // Should have exactly 7 roles
      assertEquals(allRoles.get(0), "ROLE_DEV");//$NON-NLS-1$
      assertEquals(allRoles.get(6), "ROLE_IS"); //$NON-NLS-1$

    } catch (Exception e) {
      Assert.fail();
    }
  }
  @Test
  public void testGetAllUsers() throws Exception {
    IUserDetailsRoleListWebService service = getUserDetailsRoleListWebService();

    try {
      List<String> allUsers = service.getAllUsers();
      assertNotNull(allUsers);
      assertEquals(allUsers.size(), 4);
      assertEquals(allUsers.get(0), "pat");//$NON-NLS-1$
      assertEquals(allUsers.get(3), "suzy");//$NON-NLS-1$

    } catch (Exception e) {
      Assert.fail();
    }
  }
  @Test
  public void testGetAllUsersInRole() throws Exception {
    IUserDetailsRoleListWebService service = getUserDetailsRoleListWebService();

    try {
    
    List<String> allUsersInRole = service.getAllUsersInRole("ROLE_DEV");//$NON-NLS-1$
    assertNotNull(allUsersInRole);
    assertEquals(allUsersInRole.size(), 2);
    assertEquals(allUsersInRole.get(0), "pat");//$NON-NLS-1$
    assertEquals(allUsersInRole.get(1), "tiffany");//$NON-NLS-1$
    } catch (Exception e) {
      Assert.fail();
    }
  }
  @Test
  public void testGetRolesForUser() throws Exception {

    IUserDetailsRoleListWebService service = getUserDetailsRoleListWebService();

    try {
    
    List<String> rolesForUser = service.getRolesForUser("joe");//$NON-NLS-1$
    assertNotNull(rolesForUser);
    assertEquals(rolesForUser.size(), 2);
    assertEquals(rolesForUser.get(0), "ROLE_ADMIN");//$NON-NLS-1$
    assertEquals(rolesForUser.get(1), "ROLE_CEO");//$NON-NLS-1$
    } catch (Exception e) {
      Assert.fail();
    }
  }
}
