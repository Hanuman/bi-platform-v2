/**
 * 
 */
package org.pentaho.platform.engine.services.metadata;

public class DomainStorageException extends Exception {
  private static final long serialVersionUID = -8381261699174809443L;
  public DomainStorageException(String str, Exception e) {
    super(str, e);
  }
}