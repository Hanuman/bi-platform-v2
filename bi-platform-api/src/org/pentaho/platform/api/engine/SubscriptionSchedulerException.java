package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class SubscriptionSchedulerException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -420L;

  /**
   * 
   */
  public SubscriptionSchedulerException() {
    super();
  }

  /**
   * @param message
   */
  public SubscriptionSchedulerException(final String message) {
    super(message);
  }

  /**
   * @param message
   * @param reas
   */
  public SubscriptionSchedulerException(final String message, final Throwable reas) {
    super(message, reas);
  }

  /**
   * @param reas
   */
  public SubscriptionSchedulerException(final Throwable reas) {
    super(reas);
  }
}
