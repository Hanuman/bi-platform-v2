package org.pentaho.platform.scheduler;

import org.pentaho.platform.api.util.PentahoChainedException;

public class SchedulerException extends PentahoChainedException {
  public SchedulerException() {
    super();
  }

  public SchedulerException(String message) {
    super(message);
  }

  public SchedulerException(Exception e) {
    super(e);
  }

}
