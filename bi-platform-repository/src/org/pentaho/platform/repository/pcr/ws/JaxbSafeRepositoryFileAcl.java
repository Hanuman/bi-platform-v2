package org.pentaho.platform.repository.pcr.ws;

public class JaxbSafeRepositoryFileAcl {

  public JaxbSafeRepositoryFileAclAce[] aces = new JaxbSafeRepositoryFileAclAce[0];

  public String id;

  public String owner;

  public boolean user;

  public boolean entriesInheriting = true;

}
