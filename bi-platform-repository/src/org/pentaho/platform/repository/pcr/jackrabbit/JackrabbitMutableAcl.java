package org.pentaho.platform.repository.pcr.jackrabbit;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

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
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.sid.Sid;
import org.springframework.util.Assert;

public class JackrabbitMutableAcl implements MutableAcl {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JackrabbitMutableAcl.class);

  // ~ Instance fields =================================================================================================

  private Acl parentAcl;

  private List aces = new Vector();

  private ObjectIdentity objectIdentity;

  private Serializable id;

  private Sid[] loadedSids = null; // includes all SIDs the WHERE clause covered, even if there was no ACE for a SID

  // ~ Constructors ====================================================================================================

  public JackrabbitMutableAcl() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void deleteAce(int aceIndex) throws NotFoundException {
    verifyAceIndexExists(aceIndex);

    synchronized (aces) {
      this.aces.remove(aceIndex);
    }
  }

  public Serializable getId() {
    return id;
  }

  public void insertAce(int atIndexLocation, Permission permission, Sid sid, boolean granting) throws NotFoundException {
    Assert.notNull(permission, "Permission required");
    Assert.notNull(sid, "Sid required");
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

  public void setEntriesInheriting(boolean entriesInheriting) {
  }

  public void setOwner(Sid newOwner) {
  }

  public void setParent(Acl newParent) {
    // TODO Auto-generated method stub 
  }

  public void updateAce(int aceIndex, Permission permission) throws NotFoundException {
    verifyAceIndexExists(aceIndex);
    
    synchronized (aces) {
        AccessControlEntryImpl ace = (AccessControlEntryImpl) aces.get(aceIndex);
//        ace.setPermission(permission);
    }
  }

  public AccessControlEntry[] getEntries() {
    return (AccessControlEntry[]) aces.toArray(new AccessControlEntry[] {});
  }

  public ObjectIdentity getObjectIdentity() {
    return objectIdentity;
  }

  public Sid getOwner() {
    return null;
  }

  public Acl getParentAcl() {

    // TODO Auto-generated method stub 
    return null;

  }

  public boolean isEntriesInheriting() {
    return true;
  }

  public boolean isGranted(Permission[] permission, Sid[] sids, boolean administrativeMode) throws NotFoundException,
      UnloadedSidException {
    // TODO Auto-generated method stub 
    return false;
  }

  public boolean isSidLoaded(Sid[] sids) {
    // TODO Auto-generated method stub 
    return false;
  }

  private void verifyAceIndexExists(int aceIndex) {
    if (aceIndex < 0) {
      throw new NotFoundException("aceIndex must be greater than or equal to zero");
    }
    if (aceIndex > this.aces.size()) {
      throw new NotFoundException("aceIndex must correctly refer to an index of the AccessControlEntry collection");
    }
  }

}
