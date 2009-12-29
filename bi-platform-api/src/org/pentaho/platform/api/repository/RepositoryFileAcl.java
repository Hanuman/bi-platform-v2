package org.pentaho.platform.api.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simplified and GWT-compatible version of {@code org.springframework.security.acls.Acl}.
 * 
 * @author mlowery
 */
public class RepositoryFileAcl {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(RepositoryFileAcl.class);

  // ~ Instance fields =================================================================================================

  private List<Ace> aces = new ArrayList<Ace>();

  private Serializable fileId;

  private RepositoryFileSid owner;

  private boolean entriesInheriting = true;

  // ~ Constructors ====================================================================================================

  public RepositoryFileAcl(final Serializable fileId, final RepositoryFileSid owner) {
    super();
    this.fileId = fileId;
    this.owner = owner;
  }

  // ~ Methods =========================================================================================================

  public List<Ace> getAces() {
    return Collections.unmodifiableList(aces);
  }

  public Serializable getFileId() {
    return fileId;
  }

  public RepositoryFileSid getOwner() {
    return owner;
  }

  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }

  // ~ Inner classes ===================================================================================================

  public static class Ace {
    private RepositoryFileSid recipient;

    private EnumSet<Permission> permissions;

    public Ace(final RepositoryFileSid recipient, final Permission first, final Permission... rest) {
      this(recipient, EnumSet.of(first, rest));
    }

    public Ace(final RepositoryFileSid recipient, final EnumSet<Permission> permissions) {
      super();
      this.recipient = recipient;
      this.permissions = permissions;
    }

    public RepositoryFileSid getSid() {
      return recipient;
    }

    public EnumSet<Permission> getPermissions() {
      return permissions;
    }

  }

  public static class Builder {
    private List<Ace> aces = new ArrayList<Ace>();

    private Serializable fileId;

    private RepositoryFileSid owner;

    private boolean entriesInheriting = true;

    public Builder(final Serializable fileId, final RepositoryFileSid owner) {
      this.fileId = fileId;
      this.owner = owner;
    }

    public Builder(final Serializable fileId, final String name, final RepositoryFileSid.Type type) {
      this(fileId, new RepositoryFileSid(name, type));
    }

    public Builder(final RepositoryFileAcl other) {
      this(other.fileId, other.owner);
      this.entriesInheriting(other.entriesInheriting);
      for (Ace ace : other.aces) {
        this.ace(ace);
      }
    }

    public RepositoryFileAcl build() {
      RepositoryFileAcl result = new RepositoryFileAcl(fileId, owner);
      result.aces = this.aces;
      result.entriesInheriting = this.entriesInheriting;
      return result;
    }

    public Builder entriesInheriting(final boolean entriesInheriting) {
      this.entriesInheriting = entriesInheriting;
      return this;
    }

    public Builder ace(final Ace ace) {
      this.aces.add(ace);
      return this;
    }

    public Builder ace(final RepositoryFileSid recipient, final Permission first, final Permission... rest) {
      return ace(recipient, EnumSet.of(first, rest));
    }

    public Builder ace(final RepositoryFileSid recipient, final EnumSet<Permission> permissions) {
      this.aces.add(new Ace(recipient, permissions));
      return this;
    }

    public Builder ace(final String name, final RepositoryFileSid.Type type, final Permission first, final Permission... rest) {
      return ace(new RepositoryFileSid(name, type), first, rest);
    }

    public Builder ace(final String name, final RepositoryFileSid.Type type, final EnumSet<Permission> permissions) {
      return ace(new RepositoryFileSid(name, type), permissions);
    }
  }
}
