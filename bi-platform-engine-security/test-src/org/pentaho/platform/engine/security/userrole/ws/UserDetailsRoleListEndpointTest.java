package org.pentaho.platform.engine.security.userrole.ws;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import org.junit.Before;
import org.junit.Ignore;

/**
 * This test wraps the regular unit test with a webservices endpoint, verifying the client conversion.
 * 
 * This can't be used in a live environment until metro 2.0 jars are available to test with.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 *
 */
@SuppressWarnings("nls")
@Ignore
public class UserDetailsRoleListEndpointTest extends UserDetailsRoleListWebServiceTest {
  
  IUserDetailsRoleListWebService userDetailsRoleListWebService;
  
  public UserDetailsRoleListEndpointTest() {
    Endpoint.publish("http://localhost:8891/userrolelisttest", new DefaultUserDetailsRoleListWebService()); //$NON-NLS-1$ 
  }
  
  public static void main(String args[]) throws Exception {
    
    // test against a live server, dev use only
    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    Service service = Service.create(new URL("http://localhost:8080/pentaho/webservices/userDetailsRoleListService?wsdl"), new QName(
    "http://www.pentaho.org/ws/1.0", "DefaultUserDetailsRoleListWebServiceService"));
    IUserDetailsRoleListWebService userDetailsRoleListWebService = service.getPort(IUserDetailsRoleListWebService.class);
    ((BindingProvider) userDetailsRoleListWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "joe");
    ((BindingProvider) userDetailsRoleListWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
  }
  
  @Before
  public void setUp() throws Exception {
    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    Service service = Service.create(new URL("http://localhost:8891/userrolelisttest?wsdl"), new QName(
    "http://www.pentaho.org/ws/1.0", "DefaultUserDetailsRoleListWebServiceService"));
    userDetailsRoleListWebService = service.getPort(IUserDetailsRoleListWebService.class);
  }
  
  @Override
  public IUserDetailsRoleListWebService getUserDetailsRoleListWebService() {
    return userDetailsRoleListWebService;
  }

}
