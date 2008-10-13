package org.pentaho.platform.engine.security.userroledao;

/**
 * Represents some other, usually fatal, exception.
 * 
 * @author mlowery
 */
public class UncategorizedUserRoleDaoException extends UserRoleDaoException {

  private static final long serialVersionUID = 5992292759147780152L;

  public UncategorizedUserRoleDaoException(final String msg) {
    super(msg);
  }

  public UncategorizedUserRoleDaoException(final String msg, final Throwable t) {
    super(msg, t);
  }

}
