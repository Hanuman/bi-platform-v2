/**
 * 
 */
package org.pentaho.platform.engine.services.metadata;

public class DomainAlreadyExistsException extends Exception {
  private static final long serialVersionUID = -8381261699174809443L;
  public DomainAlreadyExistsException(String str) {
    super(str);
  }
}