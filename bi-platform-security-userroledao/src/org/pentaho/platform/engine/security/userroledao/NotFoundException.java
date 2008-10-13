package org.pentaho.platform.engine.security.userroledao;

/**
 * Thrown when no object exists with the given identifier.
 * 
 * @author mlowery
 */
public class NotFoundException extends UserRoleDaoException {

  private static final long serialVersionUID = -818189401946835492L;

  public NotFoundException(final String msg) {
    super(msg);
  }

  public NotFoundException(final String msg, final Throwable t) {
    super(msg, t);
  }

}
