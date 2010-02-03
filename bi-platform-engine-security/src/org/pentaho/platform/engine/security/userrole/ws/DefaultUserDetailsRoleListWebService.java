package org.pentaho.platform.engine.security.userrole.ws;

import java.util.List;

import javax.jws.WebService;

import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Implementation of {@link IUserDetailsRoleListWebService} that delegates to an {@link IUserDetailsRoleListService} instance.
 * 
 * @author rmansoor
 */
@WebService(endpointInterface = "org.pentaho.platform.engine.security.userrole.ws.IUserDetailsRoleListWebService", name = "UserDetailsRoleListWebService", portName = "UserDetailsRoleListWebServicePort", targetNamespace = "http://www.pentaho.org/ws/1.0")

public class DefaultUserDetailsRoleListWebService implements IUserDetailsRoleListWebService{

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private IUserDetailsRoleListService userDetailsRoleListService;

  // ~ Constructors ====================================================================================================

  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultUserDetailsRoleListWebService() {
    super();
    userDetailsRoleListService = PentahoSystem.get(IUserDetailsRoleListService.class);
    if (userDetailsRoleListService == null) {
      throw new IllegalStateException("no IUserDetailsRoleListService implementation");
    }
  }

  public DefaultUserDetailsRoleListWebService(final IUserDetailsRoleListService userDetailsRoleListService) {
    super();
    this.userDetailsRoleListService = userDetailsRoleListService;
  }

  // ~ Methods =========================================================================================================

  

  public List<String> getAllRoles() {
    return userDetailsRoleListService.getAllRoles();
  }

  public List<String> getAllUsers() {
    return userDetailsRoleListService.getAllUsers();
  }

  public List<String> getAllUsersInRole(String role) {
    return userDetailsRoleListService.getAllUsersInRole(role);
  }

  public List<String> getRolesForUser(String userName) {
    return userDetailsRoleListService.getRolesForUser(userName);
  }

}
