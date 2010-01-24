package org.pentaho.platform.repository.pcr.ws;

import java.util.Date;

/**
 * JAXB-safe version of {@code RepositoryFile}. ({@code RepositoryFile} has no zero-arg constructor and no public 
 * mutators.)
 * 
 * @see RepositoryFileAdapter
 * 
 * @author mlowery
 */
public class JaxbSafeRepositoryFile {

  public JaxbSafeRepositoryFile() {
    super();
  }

  public String name;
  public String id;
  public Date createdDate;
  public Date lastModifiedDate;
  public boolean folder;
  public String absolutePath;
  public boolean hidden;
  public boolean versioned;
  public String versionId;
  public boolean locked;
  public String lockOwner;
  public String lockMessage;
  public Date lockDate;
//  public RepositoryFileSid owner;
  public String title;
  public String description;  
  public String locale;
}
