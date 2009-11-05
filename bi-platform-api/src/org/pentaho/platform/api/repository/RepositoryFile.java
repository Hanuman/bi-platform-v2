package org.pentaho.platform.api.repository;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Immutable repository file. Use the {@link Builder} to create instances.
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

  private String encoding;

  private String mimeType;

  private boolean folder;

  private String absolutePath;

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

  public String getEncoding() {
    return encoding;
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
  
//  /**
//   * Length, in bytes, of the file or 0L if the file is a folder.
//   * @return length in bytes
//   */
//  public long getLength() {
//    return length;
//  }

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

    private String encoding;

    private String mimeType;

    private boolean folder;

    private String absolutePath;
    
    public Builder(final String name) {
      this.name = name;
    }
    
    public Builder(final String name, final Serializable id, final Serializable parentId) {
      this.name = name;
      this.id = id;
      this.parentId = parentId;
    }
    
    public Builder(final RepositoryFile other) {
      this(other.name, other.id, other.parentId);
      this.absolutePath(other.absolutePath).createdDate(
          other.createdDate).encoding(other.encoding).folder(other.folder).lastModificationDate(
          other.lastModifiedDate).mimeType(other.mimeType);
    }

    public RepositoryFile build() {
      RepositoryFile result = new RepositoryFile(name, id, parentId);
      result.createdDate = this.createdDate;
      result.lastModifiedDate = this.lastModifiedDate;
      result.encoding = this.encoding;
      result.mimeType = this.mimeType;
      result.folder = this.folder;
      result.absolutePath = this.absolutePath;
      return result;
    }

    public Builder encoding(final String encoding) {
      this.encoding = encoding;
      return this;
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
