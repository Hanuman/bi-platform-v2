package org.pentaho.platform.engine.security.userroledao;

/**
 * Thrown when an object already exists with the given identifier.
 * 
 * @author mlowery
 */
public class AlreadyExistsException extends UserRoleDaoException {

  private static final long serialVersionUID = -2371295088329118369L;

  public AlreadyExistsException(final String msg) {
    super(msg);
  }

  public AlreadyExistsException(final String msg, final Throwable t) {
    super(msg, t);
  }

}
