package org.pentaho.platform.engine.security.userrole.ws;

import javax.xml.ws.Endpoint;

import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.IUserRoleListService;

public class UserDetailsRoleListServer {

  public static void main(final String[] args) {
    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    System.out.println("Starting server...");
    String address = "http://localhost:9000/userDetailsRoleListService";
    IUserRoleListService  userRoleListService = new MockUserRoleListService();
    IUserDetailsRoleListService  userDetailsRoleListService = new MockUserDetailsRoleListService();
    userDetailsRoleListService.setUserRoleListService(userRoleListService);
    Endpoint.publish(address, new DefaultUserDetailsRoleListWebService(userDetailsRoleListService));
  }
}
