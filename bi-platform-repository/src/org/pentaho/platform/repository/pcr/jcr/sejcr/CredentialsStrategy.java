package org.pentaho.platform.repository.pcr.jcr.sejcr;

import javax.jcr.Credentials;

/**
 * Determines the credentials passed to session.login().
 * 
 * @author mlowery
 */
public interface CredentialsStrategy {
  Credentials getCredentials();
}
