package org.pentaho.platform.repository.pcr.springsecurity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.acls.AccessControlEntry;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.NotFoundException;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.UnloadedSidException;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.sid.Sid;
import org.springframework.util.Assert;

/**
 * A custom implemention of {@link MutableAcl}. Based on {@code AclImpl}. This implementation:
 * 
 * <ul>
 * <li>Has no auditing.</li>
 * <li>Has no {@link #isGranted(Permission[], Sid[], boolean)} implementation. (There is no access control in
 * PentahoContentRepository--it is all done in Jackrabbit.</li>
 * <li>Has no {@link AclAuthorizationStrategy}. Modifying the ACL is enforced by Jackrabbit--as opposed to the default
 * {@link JdbcMutableAclService} that is provided with Spring Security.</li>
 * </ul>
 * 
 * @author mlowery
 */
public class PentahoMutableAcl implements MutableAcl {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = 1262419494510052509L;

  private static final Log logger = LogFactory.getLog(PentahoMutableAcl.class);

  // ~ Instance fields =================================================================================================

  private Acl parentAcl;

  private List<AccessControlEntry> aces = Collections.synchronizedList(new ArrayList<AccessControlEntry>());

  private ObjectIdentity objectIdentity;

  private Serializable id;

  private Sid owner;

  private boolean entriesInheriting = true;

  // ~ Constructors ====================================================================================================

  public PentahoMutableAcl(final ObjectIdentity objectIdentity, final Acl parentAcl, final boolean entriesInheriting,
      final Sid owner) {
    super();
    Assert.notNull(objectIdentity);
    Assert.notNull(owner);
    this.objectIdentity = objectIdentity;
    this.parentAcl = parentAcl; // may be null
    this.entriesInheriting = entriesInheriting;
    this.owner = owner;
  }

  // ~ Methods =========================================================================================================

  private void verifyAceIndexExists(int aceIndex) {
    if (aceIndex < 0) {
      throw new NotFoundException("aceIndex must be greater than or equal to zero");
    }
    if (aceIndex > this.aces.size()) {
      throw new NotFoundException("aceIndex must correctly refer to an index of the AccessControlEntry collection");
    }
  }

  public void deleteAce(int aceIndex) throws NotFoundException {
    verifyAceIndexExists(aceIndex);

    synchronized (aces) {
      this.aces.remove(aceIndex);
    }
  }

  public AccessControlEntry[] getEntries() {
    // Can safely return AccessControlEntry directly, as they're immutable outside the ACL package
    return (AccessControlEntry[]) aces.toArray(new AccessControlEntry[] {});
  }

  /**
   * There is no id for the ACL as we can also find the ACL by knowing the ObjectIdentity.
   */
  public Serializable getId() {
    return null;
  }

  public ObjectIdentity getObjectIdentity() {
    return objectIdentity;
  }

  public Sid getOwner() {
    return this.owner;
  }

  public Acl getParentAcl() {
    return parentAcl;
  }

  public void insertAce(int atIndexLocation, Permission permission, Sid sid, boolean granting) throws NotFoundException {
    Assert.notNull(permission);
    Assert.notNull(sid);
    if (atIndexLocation < 0) {
      throw new NotFoundException("atIndexLocation must be greater than or equal to zero");
    }
    if (atIndexLocation > this.aces.size()) {
      throw new NotFoundException(
          "atIndexLocation must be less than or equal to the size of the AccessControlEntry collection");
    }

    AccessControlEntryImpl ace = new AccessControlEntryImpl(null, this, sid, permission, granting, false, false);

    synchronized (aces) {
      this.aces.add(atIndexLocation, ace);
    }
  }

  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }

  public void setEntriesInheriting(boolean entriesInheriting) {
    this.entriesInheriting = entriesInheriting;
  }

  public void setOwner(Sid newOwner) {
    Assert.notNull(newOwner);
    this.owner = newOwner;
  }

  public void setParent(Acl newParent) {
    Assert.isTrue(newParent == null || !newParent.equals(this), "Cannot be the parent of yourself");
    this.parentAcl = newParent;
  }

  public void updateAce(int aceIndex, Permission permission) throws NotFoundException {
    verifyAceIndexExists(aceIndex);

    synchronized (aces) {
      AccessControlEntryImpl oldAce = (AccessControlEntryImpl) aces.get(aceIndex);
      AccessControlEntryImpl updatedAce = new AccessControlEntryImpl(oldAce.getId(), this, oldAce.getSid(), permission,
          oldAce.isGranting(), oldAce.isAuditSuccess(), oldAce.isAuditFailure());
      aces.set(aceIndex, updatedAce);
    }
  }

  /**
   * Not implemented.
   */
  public boolean isGranted(Permission[] permission, Sid[] sids, boolean administrativeMode) throws NotFoundException,
      UnloadedSidException {
    throw new UnsupportedOperationException();
  }

  /**
   * Only class that uses this value is {@code org.springframework.security.acls.jdbc.BasicLookupStrategy} which is not
   * used.
   */
  public boolean isSidLoaded(Sid[] sids) {
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aces == null) ? 0 : aces.hashCode());
    result = prime * result + (entriesInheriting ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((objectIdentity == null) ? 0 : objectIdentity.hashCode());
    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
    result = prime * result + ((parentAcl == null) ? 0 : parentAcl.hashCode());
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
    PentahoMutableAcl other = (PentahoMutableAcl) obj;
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
    if (objectIdentity == null) {
      if (other.objectIdentity != null)
        return false;
    } else if (!objectIdentity.equals(other.objectIdentity))
      return false;
    if (owner == null) {
      if (other.owner != null)
        return false;
    } else if (!owner.equals(other.owner))
      return false;
    if (parentAcl == null) {
      if (other.parentAcl != null)
        return false;
    } else if (!parentAcl.equals(other.parentAcl))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PentahoMutableAcl [aces=" + aces + ", entriesInheriting=" + entriesInheriting + ", id=" + id
        + ", objectIdentity=" + objectIdentity + ", owner=" + owner + ", parentAcl=" + parentAcl + "]";
  }

}
