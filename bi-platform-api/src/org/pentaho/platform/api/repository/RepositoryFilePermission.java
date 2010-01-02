package org.pentaho.platform.api.repository;

/**
 * Repository file permission enumeration. These are the permission "bits."
 * 
 * @author mlowery
 */
public enum RepositoryFilePermission {
  READ, WRITE, EXECUTE, DELETE, APPEND, DELETE_CHILD, READ_ACL, WRITE_ACL, ALL;
}
