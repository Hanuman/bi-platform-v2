package org.pentaho.platform.api.repository;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Immutable repository file. Use the {@link Builder} to create instances.
 * 
 * <p>
 * This class should use only GWT-emulated types.
 * </p>
 * 
 * @author mlowery
 */
public class RepositoryFile implements Comparable<RepositoryFile> {

  // ~ Static fields/initializers ======================================================================================

  public static final String SEPARATOR = "/"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  private String name;

  private Serializable id;

  private Serializable parentId;

  /**
   * Read-only.
   */
  private Date createdDate;

  /**
   * Read-only.
   */
  private Date lastModifiedDate;

  /**
   * Read-only. (Determined by {@code IRepositoryFileContent} associated with this file.)
   */
  private String contentType;

  private boolean folder;

  /**
   * Read-only.
   */
  private String absolutePath;

  private boolean hidden;

  private boolean versioned;

  /**
   * The version name or number. Read-only.
   */
  private Serializable versionId;

  /**
   * Locked status. Read-only.
   */
  private boolean locked;

  /**
   * Username of the owner of the lock. Read-only. {@code null} if file not locked.
   */
  private String lockOwner;

  /**
   * Message left by the owner when he locked the file. Read-only. {@code null} if file not locked.
   */
  private String lockMessage;

  /**
   * The date that this lock was created. Read-only. {@code null} if file not locked.
   */
  private Date lockDate;

  // ~ Constructors ====================================================================================================

  public RepositoryFile(final String name) {
    super();
    this.name = name;
  }

  public RepositoryFile(final String name, final Serializable id, final Serializable parentId) {
    super();
    this.name = name;
    this.id = id;
    this.parentId = parentId;
  }

  // ~ Methods =========================================================================================================

  public String getName() {
    return name;
  }

  public Serializable getId() {
    return id;
  }

  public Serializable getParentId() {
    return parentId;
  }

  public Date getCreatedDate() {
    // defensive copy
    return (createdDate != null ? new Date(createdDate.getTime()) : null);
  }

  public Date getLastModifiedDate() {
    // defensive copy
    return (lastModifiedDate != null ? new Date(lastModifiedDate.getTime()) : null);
  }

  public String getContentType() {
    return contentType;
  }

  public boolean isFolder() {
    return folder;
  }

  public String getAbsolutePath() {
    return absolutePath;
  }

  public boolean isHidden() {
    return hidden;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public Serializable getVersionId() {
    return versionId;
  }

  public boolean isLocked() {
    return locked;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public String getLockMessage() {
    return lockMessage;
  }

  public Date getLockDate() {
    // defensive copy
    return (lockDate != null ? new Date(lockDate.getTime()) : null);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public static class Builder {
    private String name;

    private Serializable id;

    private Serializable parentId;

    private Date createdDate;

    private Date lastModifiedDate;

    private String contentType;

    private boolean folder;

    private String absolutePath;

    private boolean hidden;

    private boolean versioned;

    private Serializable versionId;

    private boolean locked;

    private String lockOwner;

    private String lockMessage;

    private Date lockDate;

    public Builder(final String name) {
      assertHasText(name);
      this.name = name;
    }

    public Builder(final String name, final Serializable id, final Serializable parentId) {
      assertHasText(name);
      assertNotNull(id);
      this.name = name;
      this.id = id;
      this.parentId = parentId;
    }

    public Builder(final RepositoryFile other) {
      this(other.name, other.id, other.parentId);
      this.absolutePath(other.absolutePath).createdDate(other.createdDate).folder(other.folder).lastModificationDate(
          other.lastModifiedDate).contentType(other.contentType).versioned(other.versioned).hidden(other.hidden)
          .versionId(other.versionId).locked(other.locked).lockDate(other.lockDate).lockOwner(other.lockOwner)
          .lockMessage(other.lockMessage);
    }

    public RepositoryFile build() {
      RepositoryFile result = new RepositoryFile(name, id, parentId);
      result.createdDate = this.createdDate;
      result.lastModifiedDate = this.lastModifiedDate;
      result.contentType = this.contentType;
      result.folder = this.folder;
      result.absolutePath = this.absolutePath;
      result.hidden = this.hidden;
      result.versioned = this.versioned;
      result.versionId = this.versionId;
      result.locked = this.locked;
      result.lockOwner = this.lockOwner;
      result.lockMessage = this.lockMessage;
      result.lockDate = this.lockDate;
      return result;
    }

    public Builder contentType(final String contentType) {
      this.contentType = contentType;
      return this;
    }

    public Builder createdDate(final Date createdDate) {
      // defensive copy
      this.createdDate = (createdDate != null ? new Date(createdDate.getTime()) : null);
      return this;
    }

    public Builder lastModificationDate(final Date lastModifiedDate) {
      // defensive copy
      this.lastModifiedDate = (lastModifiedDate != null ? new Date(lastModifiedDate.getTime()) : null);
      return this;
    }

    public Builder folder(final boolean folder) {
      this.folder = folder;
      return this;
    }

    public Builder absolutePath(final String absolutePath) {
      this.absolutePath = absolutePath;
      return this;
    }

    public Builder hidden(final boolean hidden) {
      this.hidden = hidden;
      return this;
    }

    public Builder versioned(final boolean versioned) {
      this.versioned = versioned;
      return this;
    }

    public Builder versionId(final Serializable versionId) {
      this.versionId = versionId;
      return this;
    }

    public Builder locked(final boolean locked) {
      this.locked = locked;
      return this;
    }

    public Builder lockOwner(final String lockOwner) {
      this.lockOwner = lockOwner;
      return this;
    }

    public Builder lockMessage(final String lockMessage) {
      this.lockMessage = lockMessage;
      return this;
    }

    public Builder lockDate(final Date lockDate) {
      // defensive copy
      this.lockDate = (lockDate != null ? new Date(lockDate.getTime()) : null);
      return this;
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
    protected void assertTrue(final boolean expression) {
      if (!expression) {
        throw new IllegalArgumentException("[Assertion failed] - this expression must be true");
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

  }

  public int compareTo(final RepositoryFile other) {
    if (other == null) {
      throw new NullPointerException(); // per Comparable contract
    }
    if (equals(other)) {
      return 0;
    }
    // either this or other has a null id; fall back on name
    return name.compareTo(other.name);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
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
    RepositoryFile other = (RepositoryFile) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (versionId == null) {
      if (other.versionId != null)
        return false;
    } else if (!versionId.equals(other.versionId))
      return false;
    return true;
  }

}
