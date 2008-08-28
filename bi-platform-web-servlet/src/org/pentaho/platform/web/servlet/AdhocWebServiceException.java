package org.pentaho.platform.web.servlet;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class AdhocWebServiceException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -1842098457110711029L;

  /**
   * 
   */
  public AdhocWebServiceException() {
    super();
  }

  /**
   * @param message
   */
  public AdhocWebServiceException(final String message) {
    super(message);
  }

  /**
   * @param message
   * @param reas
   */
  public AdhocWebServiceException(final String message, final Throwable reas) {
    super(message, reas);
  }

  /**
   * @param reas
   */
  public AdhocWebServiceException(final Throwable reas) {
    super(reas);
  }

}
