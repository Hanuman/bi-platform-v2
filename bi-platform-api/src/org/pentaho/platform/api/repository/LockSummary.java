package org.pentaho.platform.api.repository;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Immutable lock summary.
 * 
 * @author mlowery
 */
public class LockSummary {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(LockSummary.class);

  // ~ Instance fields =================================================================================================

  /**
   * Username of the owner of the lock.
   */
  private String owner;

  /**
   * Message left by the owner when he locked the file.
   */
  private String message;

  /**
   * The date that this lock was created.
   */
  private Date date;

  // ~ Constructors ====================================================================================================

  public LockSummary(final String owner, final Date date, final String message) {
    super();
    this.owner = owner;
    this.date = date;
    this.message = message;
  }

  // ~ Methods =========================================================================================================

  public String getOwner() {
    return owner;
  }

  public String getMessage() {
    return message;
  }

  public Date getDate() {
    return date;
  }
}
