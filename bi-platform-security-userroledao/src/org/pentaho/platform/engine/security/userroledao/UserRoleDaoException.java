package org.pentaho.platform.engine.security.userroledao;

/**
 * Superclass of all exception types thrown by {@link IUserRoleDao} implementations.
 * 
 * @author mlowery
 */
public abstract class UserRoleDaoException extends RuntimeException {

  private static final long serialVersionUID = -80813880351536263L;

  public UserRoleDaoException(final String msg) {
    super(msg);
  }

  public UserRoleDaoException(final String msg, final Throwable t) {
    super(msg, t);
  }

}
