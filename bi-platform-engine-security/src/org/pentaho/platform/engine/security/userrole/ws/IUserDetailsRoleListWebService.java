package org.pentaho.platform.engine.security.userrole.ws;

/**
 * JAX-WS-safe version of {@code IUserRoleListService}.
 * 
 * 
 * @author rmansoor
 */
import java.util.List;

import javax.jws.WebService;

@WebService
public interface IUserDetailsRoleListWebService {

  public List<String> getAllRoles();
  
  public List<String> getAllUsers();
 
  public List<String> getAllUsersInRole(String role);
  
  public List<String> getRolesForUser(String userName);

}
