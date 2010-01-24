package org.pentaho.platform.repository.pcr.ws;

import javax.xml.ws.Endpoint;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;

public class RepoServer {

  public static void main(final String[] args) {
    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {
        "classpath:/sample-repository.spring.xml", "classpath:/sample-repository-test-override.spring.xml" });
    IUnifiedRepository repo = (IUnifiedRepository) ctx.getBean("unifiedRepository");
    repo.getRepositoryLifecycleManager().startup();
    System.out.println("Starting server...");
    String address = "http://localhost:9000/repo";
    hack();
    repo.getRepositoryLifecycleManager().newTenant();
    repo.getRepositoryLifecycleManager().newUser();
    Endpoint.publish(address, new DefaultUnifiedRepositoryWebService(repo));
  }

  private static void hack() {
    final String username = "suzy";
    StandaloneSession pentahoSession = new StandaloneSession(username);
    pentahoSession.setAuthenticated(username);
    final GrantedAuthority[] authorities = new GrantedAuthority[2];
    authorities[0] = new GrantedAuthorityImpl("Authenticated");
    authorities[1] = new GrantedAuthorityImpl("acme_Authenticated");
    final String password = "ignored";
    UserDetails userDetails = new User(username, password, true, true, true, true, authorities);
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, password, authorities);
    SecurityHelper.setPrincipal(authentication, pentahoSession);
    // for PentahoSessionCredentialsStrategy
    PentahoSessionHolder.setSession(pentahoSession);
    // for Spring Security method security on IUnifiedRepository
    pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, "acme");
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

}
