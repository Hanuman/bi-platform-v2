package org.pentaho.platform.repository.pcr.sejcr;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

public class SpringSecurityCredentialsStrategy implements CredentialsStrategy {

  private static final char[] PASSWORD = "ignored".toCharArray();
    
  public Credentials getCredentials() {
    String username = getUsername();
    return new SimpleCredentials(username, PASSWORD);
  }

  private String getUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    Assert.notNull(auth, "this method cannot be called without a non-null authentication");

    if (auth.getPrincipal() instanceof UserDetails) {
      return ((UserDetails) auth.getPrincipal()).getUsername();
    } else {
      return auth.getPrincipal().toString();
    }
  }
}
