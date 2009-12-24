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
   * The ID of this version, such as a version number like 1.3.
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((author == null) ? 0 : author.hashCode());
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((labels == null) ? 0 : labels.hashCode());
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((versionedFileId == null) ? 0 : versionedFileId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    VersionSummary other = (VersionSummary) obj;
    if (author == null) {
      if (other.author != null)
        return false;
    } else if (!author.equals(other.author))
      return false;
    if (date == null) {
      if (other.date != null)
        return false;
    } else if (!date.equals(other.date))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (labels == null) {
      if (other.labels != null)
        return false;
    } else if (!labels.equals(other.labels))
      return false;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    if (versionedFileId == null) {
      if (other.versionedFileId != null)
        return false;
    } else if (!versionedFileId.equals(other.versionedFileId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "VersionSummary [author=" + author + ", date=" + date + ", id=" + id + ", labels=" + labels + ", message="
        + message + ", versionedFileId=" + versionedFileId + "]";
  }
  
  

}
