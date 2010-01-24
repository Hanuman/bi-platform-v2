package org.pentaho.platform.api.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Immutable repository file access control list (ACL). Use the {@link Builder} to create instances.
 * 
 * <p>
 * Same abstraction as {@code org.springframework.security.acls.Acl} except it contains no logic and is 
 * GWT-compatible.
 * </p>
 * 
 * @author mlowery
 */
public class RepositoryFileAcl /*implements Serializable*/{

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = 6661340152568187033L;

  // ~ Instance fields =================================================================================================

  private List<RepositoryFileAce> aces = new ArrayList<RepositoryFileAce>();

  private Serializable id;

  private RepositoryFileSid owner;

  private boolean entriesInheriting = true;

  // ~ Constructors ====================================================================================================

  public RepositoryFileAcl(final Serializable id, final RepositoryFileSid owner) {
    super();
    this.id = id;
    this.owner = owner;
  }

  // ~ Methods =========================================================================================================

  public List<RepositoryFileAce> getAces() {
    return Collections.unmodifiableList(aces);
  }

  public Serializable getId() {
    return id;
  }

  public RepositoryFileSid getOwner() {
    return owner;
  }

  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aces == null) ? 0 : aces.hashCode());
    result = prime * result + (entriesInheriting ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
    RepositoryFileAcl other = (RepositoryFileAcl) obj;
    if (aces == null) {
      if (other.aces != null)
        return false;
    } else if (!aces.equals(other.aces))
      return false;
    if (entriesInheriting != other.entriesInheriting)
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (owner == null) {
      if (other.owner != null)
        return false;
    } else if (!owner.equals(other.owner))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "RepositoryFileAcl [id=" + id + ", owner=" + owner + ", entriesInheriting=" + entriesInheriting + ", aces="
        + aces + "]";
  }

  // ~ Inner classes ===================================================================================================

  public static class Builder {
    private List<RepositoryFileAce> aces = new ArrayList<RepositoryFileAce>();

    private Serializable id;

    private RepositoryFileSid owner;

    private boolean entriesInheriting = true;

    public Builder(final Serializable id, final RepositoryFileSid owner) {
      this.id = id;
      this.owner = owner;
    }

    public Builder(final Serializable id, final String name, final RepositoryFileSid.Type type) {
      this(id, new RepositoryFileSid(name, type));
    }

    public Builder(final RepositoryFileAcl other) {
      this(other.id, other.owner);
      this.entriesInheriting(other.entriesInheriting);
      for (RepositoryFileAce ace : other.aces) {
        this.ace(ace);
      }
    }

    public RepositoryFileAcl build() {
      RepositoryFileAcl result = new RepositoryFileAcl(id, owner);
      result.aces = this.aces;
      result.entriesInheriting = this.entriesInheriting;
      return result;
    }

    public Builder entriesInheriting(final boolean entriesInheriting) {
      this.entriesInheriting = entriesInheriting;
      return this;
    }

    public Builder owner(final RepositoryFileSid owner) {
      this.owner = owner;
      return this;
    }

    public Builder ace(final RepositoryFileAce ace) {
      this.aces.add(ace);
      return this;
    }

    public Builder ace(final RepositoryFileSid recipient, final RepositoryFilePermission first,
        final RepositoryFilePermission... rest) {
      return ace(recipient, EnumSet.of(first, rest));
    }

    public Builder ace(final RepositoryFileSid recipient, final EnumSet<RepositoryFilePermission> permissions) {
      this.aces.add(new RepositoryFileAce(recipient, permissions));
      return this;
    }

    public Builder ace(final String name, final RepositoryFileSid.Type type, final RepositoryFilePermission first,
        final RepositoryFilePermission... rest) {
      return ace(new RepositoryFileSid(name, type), first, rest);
    }

    public Builder ace(final String name, final RepositoryFileSid.Type type,
        final EnumSet<RepositoryFilePermission> permissions) {
      return ace(new RepositoryFileSid(name, type), permissions);
    }

    public Builder clearAces() {
      this.aces.clear();
      return this;
    }
  }

}
