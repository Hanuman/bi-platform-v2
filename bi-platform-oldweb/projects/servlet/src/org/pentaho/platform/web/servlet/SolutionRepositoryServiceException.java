package org.pentaho.platform.web.servlet;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class SolutionRepositoryServiceException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -1842098457110711029L;

  /**
   * 
   */
  public SolutionRepositoryServiceException() {
    super();
  }

  /**
   * @param message
   */
  public SolutionRepositoryServiceException(final String message) {
    super(message);
  }

  /**
   * @param message
   * @param reas
   */
  public SolutionRepositoryServiceException(final String message, final Throwable reas) {
    super(message, reas);
  }

  /**
   * @param reas
   */
  public SolutionRepositoryServiceException(final Throwable reas) {
    super(reas);
  }

}
