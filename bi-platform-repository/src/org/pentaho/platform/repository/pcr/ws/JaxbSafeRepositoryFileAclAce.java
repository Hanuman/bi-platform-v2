package org.pentaho.platform.repository.pcr.ws;

import org.pentaho.platform.api.repository.RepositoryFilePermission;

public class JaxbSafeRepositoryFileAclAce {
  public String recipient;

  public boolean user;

  public RepositoryFilePermission[] permissions;
}
