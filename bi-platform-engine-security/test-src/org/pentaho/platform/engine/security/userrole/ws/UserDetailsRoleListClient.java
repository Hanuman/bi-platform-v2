package org.pentaho.platform.engine.security.userrole.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;



public class UserDetailsRoleListClient {

    private IUserDetailsRoleListService userDetailsRoleListService;

    @Before
    public void setUp() throws Exception {
      Service service = Service.create(new URL("http://localhost:8080/pentaho/webservices/userDetailsRoleListService?wsdl"), new QName(
          "http://www.pentaho.org/ws/1.0", "DefaultUserDetailsRoleListWebServiceService"));

      IUserDetailsRoleListWebService userDetailsRoleListWebService = service.getPort(IUserDetailsRoleListWebService.class);

      userDetailsRoleListService = new UserDetailsRoleListServiceToWebServiceAdapter(userDetailsRoleListWebService);
      cleanup();
    }

    protected void cleanup() throws Exception {
    }

    @Test
    public void testUserDetails() {
      
      List allRoles = userDetailsRoleListService.getAllRoles();
      assertNotNull(allRoles);
      assertEquals(allRoles.size(), 7); // Should have exactly 7 roles
      assertEquals(allRoles.get(0), "ROLE_DEV");//$NON-NLS-1$
      assertEquals(allRoles.get(6), "ROLE_IS"); //$NON-NLS-1$

      List allUsers = userDetailsRoleListService.getAllUsers();
      assertNotNull(allUsers);
      assertEquals(allUsers.size(), 4);
      assertEquals(allUsers.get(0), "pat");//$NON-NLS-1$
      assertEquals(allUsers.get(3), "suzy");//$NON-NLS-1$

      List allUsersInRole = userDetailsRoleListService.getAllUsersInRole("ROLE_DEV");//$NON-NLS-1$
      assertNotNull(allUsersInRole);
      assertEquals(allUsersInRole.size(), 2);
      assertEquals(allUsersInRole.get(0), "pat");//$NON-NLS-1$
      assertEquals(allUsersInRole.get(1), "tiffany");//$NON-NLS-1$
    }
}
