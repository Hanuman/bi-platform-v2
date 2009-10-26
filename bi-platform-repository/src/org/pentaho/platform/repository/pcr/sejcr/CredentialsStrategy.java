package org.pentaho.platform.repository.pcr.sejcr;

import javax.jcr.Credentials;

/**
 * Determines the credentials passed to session.login().
 * 
 * @author mlowery
 */
public interface CredentialsStrategy {
  Credentials getCredentials();
}
