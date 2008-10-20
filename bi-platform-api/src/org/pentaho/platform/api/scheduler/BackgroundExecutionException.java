package org.pentaho.platform.api.scheduler;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class BackgroundExecutionException extends PentahoCheckedChainedException {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public BackgroundExecutionException() {
    super();
  }

  public BackgroundExecutionException(String message) {
    super(message);
  }

  public BackgroundExecutionException(Throwable cause)
  {
    super(cause);
  }

  public BackgroundExecutionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
