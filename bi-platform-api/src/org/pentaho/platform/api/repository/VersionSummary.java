package org.pentaho.platform.api.repository;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Immutable version summary for a {@code RepositoryFile}. This summary represents a single version in a 
 * RepositoryFile's version history.
 * 
 * @author mlowery
 */
public class VersionSummary {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(VersionSummary.class);

  // ~ Instance fields =================================================================================================

  /**
   * The message the author left when he created this version.
   */
  private String message;

  /**
   * Date of creation for this version.
   */
  private Date date;

  /**
   * Username of the author of this version.
   */
  private String author;

  /**
   * The ID of this version.
   */
  private Serializable id;

  /**
   * The ID of the node that is versioned.
   */
  private Serializable versionedFileId;
  
  /**
   * List of labels applied to this version (never {@code null}).
   */
  private List<String> labels;

  // ~ Constructors ====================================================================================================

  public VersionSummary(final Serializable id, final Serializable versionedFileId, final Date date,
      final String author, final String message, final List<String> labels) {
    super();
    assertNotNull(id);
    assertNotNull(versionedFileId);
    assertNotNull(date);
    assertHasText(author);
    assertNotNull(labels);
    this.id = id;
    this.versionedFileId = versionedFileId;
    this.date = new Date(date.getTime());
    this.author = author;
    this.message = message;
    this.labels = Collections.unmodifiableList(labels);
  }

  // ~ Methods =========================================================================================================

  public String getMessage() {
    return message;
  }

  public Date getDate() {
    return new Date(date.getTime());
  }

  public String getAuthor() {
    return author;
  }

  public Serializable getId() {
    return id;
  }

  public Serializable getVersionedFileId() {
    return versionedFileId;
  }

  /**
   * Implemented here to maintain GWT-compatibility.
   */
  protected void assertHasText(final String in) {
    if (in == null || in.length() == 0 || in.trim().length() == 0) {
      throw new IllegalArgumentException(
          "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }
  }

  /**
   * Implemented here to maintain GWT-compatibility.
   */
  private void assertNotNull(final Object in) {
    if (in == null) {
      throw new IllegalArgumentException("[Assertion failed] - this argument is required; it must not be null");
    }
  }

  public List<String> getLabels() {
    return labels;
  }
}
