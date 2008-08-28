package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class ObjectFactoryException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = 420L;

  public ObjectFactoryException() {
    super();
  }

  public ObjectFactoryException(final String message) {
    super(message);
  }

  public ObjectFactoryException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public ObjectFactoryException(final Throwable reas) {
    super(reas);
  }

}
