package org.pentaho.platform.repository.pcr.sejcr;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.util.Assert;

/**
 * A {@link CredentialsStrategy} that creates credentials from the current {@link IPentahoSession}.
 * 
 * @author mlowery
 */
public class PentahoSessionCredentialsStrategy implements CredentialsStrategy {

  private static final char[] PASSWORD = "ignored".toCharArray(); //$NON-NLS-1$
    
  public Credentials getCredentials() {
    String username = getUsername();
    return new SimpleCredentials(username, PASSWORD);
  }

  private String getUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null, "this method cannot be called with a null IPentahoSession");
    return pentahoSession.getName();
  }
}
