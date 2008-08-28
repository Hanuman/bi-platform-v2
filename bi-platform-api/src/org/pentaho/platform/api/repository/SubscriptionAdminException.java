package org.pentaho.platform.api.repository;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class SubscriptionAdminException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = 666L;

  public SubscriptionAdminException() {
    super();
  }

  public SubscriptionAdminException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public SubscriptionAdminException(final String message) {
    super(message);
  }

  public SubscriptionAdminException(final Throwable reas) {
    super(reas);
  }
}
