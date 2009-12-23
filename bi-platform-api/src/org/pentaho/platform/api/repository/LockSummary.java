package org.pentaho.platform.api.repository;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Immutable lock summary for a {@code RepositoryFile}.
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

  /**
   * The ID of the locked file.
   */
  private Serializable lockedFileId;

  // ~ Constructors ====================================================================================================

  public LockSummary(final Serializable lockedFileId, final String owner, final Date date, final String message) {
    super();
    assertNotNull(lockedFileId);
    assertNotNull(owner);
    assertNotNull(date);
    this.lockedFileId = lockedFileId;
    this.owner = owner;
    this.date = new Date(date.getTime());
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
    return new Date(date.getTime());
  }

  public Serializable getLockedFileId() {
    return lockedFileId;
  }

  /**
   * Implemented here to maintain GWT-compatibility.
   */
  private void assertNotNull(final Object in) {
    if (in == null) {
      throw new IllegalArgumentException("[Assertion failed] - this argument is required; it must not be null");
    }
  }
}
