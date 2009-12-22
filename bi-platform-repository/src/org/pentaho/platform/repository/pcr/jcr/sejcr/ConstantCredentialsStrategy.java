package org.pentaho.platform.repository.pcr.jcr.sejcr;

import javax.jcr.Credentials;

/**
 * Uses hard-coded credentials.
 * 
 * @author mlowery
 */
public class ConstantCredentialsStrategy implements CredentialsStrategy {

  private Credentials credentials;

  /**
   * Null credentials.
   */
  public ConstantCredentialsStrategy() {
    super();
  }
  
  public ConstantCredentialsStrategy(final Credentials credentials) {
    super();
    this.credentials = credentials;
  }

  public Credentials getCredentials() {
    return credentials;
  }
}