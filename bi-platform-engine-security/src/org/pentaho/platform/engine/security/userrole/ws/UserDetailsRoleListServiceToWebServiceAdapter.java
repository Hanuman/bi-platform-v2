package org.pentaho.platform.engine.security.userrole.ws;

import java.util.List;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.IUserRoleListService;

/**
 * Converts calls to {@link IUserDetailsRoleListService} into {@link IUserDetailsRoleListWebService}. This is how client code 
 * remains unaware of server code location.
 * 
 * @author rmansoor
 */

public class UserDetailsRoleListServiceToWebServiceAdapter implements IUserDetailsRoleListService{
  
  private IUserDetailsRoleListWebService userDetailsRoleListWebService;
  

  public UserDetailsRoleListServiceToWebServiceAdapter(IUserDetailsRoleListWebService userDetailsRoleListWebService) {
    super();
    this.userDetailsRoleListWebService = userDetailsRoleListWebService;
  }

  public void afterPropertiesSet() throws Exception {
  }

  public List<String> getAllRoles() {
    return userDetailsRoleListWebService.getAllRoles();
  }
  
  public List<String> getAllUsers() {
    // TODO Auto-generated method stub
    return userDetailsRoleListWebService.getAllUsers();
  }

  public List<String> getAllUsersInRole(String role) {
    // TODO Auto-generated method stub
    return userDetailsRoleListWebService.getAllUsersInRole(role);
  }

  public IPentahoSession getEffectiveUserSession(String userName, IParameterProvider sessionParameters) {
    return null;
  }

  public List<String> getRolesForUser(String userName) {
    // TODO Auto-generated method stub
    return userDetailsRoleListWebService.getRolesForUser(userName);
  }

  public IUserRoleListService getUserRoleListService() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setUserRoleListService(IUserRoleListService value) {
    // TODO Auto-generated method stub    
  }

}
