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
 * <p>
 * If a user has traversal rights to the folder containing this file, then the user should be able to get this file
 * regardless of the user's permissions on this file. (The permission should be checked when the user attempts to get 
 * the {@link IRepositoryFileContent} associated with this file.)
 * </p>
 * 
 * <p>
 * For this reason, <strong>never</strong> create a field of this class that is of type {@link IRepositoryFileContent} 
 * since instances of this class are not subject to access control. Instead, require the user to go back to the service 
 * that issued this {@link RepositoryFile} instance.  
 * </p>
 * 
 * @author mlowery
 */
public class RepositoryFile implements Comparable<RepositoryFile> {

  // ~ Static fields/initializers ======================================================================================

  public static final String SEPARATOR = "/";

  // ~ Instance fields =================================================================================================

  private String name;

  private Serializable id;

  private Serializable parentId;

  private Date createdDate;

  private Date lastModifiedDate;

  private String mimeType;

  private boolean folder;

  private String absolutePath;

  private boolean hidden;

  private boolean versioned;

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
    // maintain immutability via defensive copy
    if (createdDate != null) {
      return new Date(createdDate.getTime());
    } else {
      return null;
    }
  }

  public Date getLastModifiedDate() {
    // maintain immutability via defensive copy
    if (lastModifiedDate != null) {
      return new Date(lastModifiedDate.getTime());
    } else {
      return null;
    }
  }

  public String getMimeType() {
    return mimeType;
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

    private String mimeType;

    private boolean folder;

    private String absolutePath;

    private boolean hidden;

    private boolean versioned;

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
          other.lastModifiedDate).mimeType(other.mimeType);
    }

    public RepositoryFile build() {
      // currently folder versioning is not supported
      assertTrue(!(folder && versioned));
      RepositoryFile result = new RepositoryFile(name, id, parentId);
      result.createdDate = this.createdDate;
      result.lastModifiedDate = this.lastModifiedDate;
      result.mimeType = this.mimeType;
      result.folder = this.folder;
      result.absolutePath = this.absolutePath;
      result.hidden = this.hidden;
      result.versioned = this.versioned;
      return result;
    }

    public Builder mimeType(final String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public Builder createdDate(final Date createdDate) {
      this.createdDate = createdDate;
      return this;
    }

    public Builder lastModificationDate(final Date lastModifiedDate) {
      this.lastModifiedDate = lastModifiedDate;
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
    return true;
  }

}
