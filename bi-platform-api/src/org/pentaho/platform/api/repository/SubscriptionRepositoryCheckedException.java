package org.pentaho.platform.api.repository;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class SubscriptionRepositoryCheckedException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -420;

  /**
   * 
   */
  public SubscriptionRepositoryCheckedException() {
    super();
  }

  /**
   * @param message
   */
  public SubscriptionRepositoryCheckedException(final String message) {
    super(message);
  }

  /**
   * @param message
   * @param reas
   */
  public SubscriptionRepositoryCheckedException(final String message, final Throwable reas) {
    super(message, reas);
  }

  /**
   * @param reas
   */
  public SubscriptionRepositoryCheckedException(final Throwable reas) {
    super(reas);
  }

}
