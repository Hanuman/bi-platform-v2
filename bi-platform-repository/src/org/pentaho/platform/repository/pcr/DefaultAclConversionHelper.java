package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.repository.pcr.PentahoContentRepository.IAclConversionHelper;
import org.pentaho.platform.repository.pcr.springsecurity.PentahoMutableAcl;
import org.pentaho.platform.repository.pcr.springsecurity.SpringSecurityRepositoryFilePermission;
import org.springframework.security.acls.AccessControlEntry;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.AclService;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;
import org.springframework.util.Assert;

/**
 * Default {@code IAclConverter} implementation.
 * 
 * @author mlowery
 */
public class DefaultAclConversionHelper implements IAclConversionHelper {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  /**
   * Needed to lookup parent ACLs.
   */
  private AclService aclService;

  // ~ Constructors ====================================================================================================

  public DefaultAclConversionHelper(final AclService aclService) {
    super();
    this.aclService = aclService;
  }

  // ~ Methods =========================================================================================================

  public MutableAcl pentahoAclToSsAcl(final RepositoryFileAcl pentahoAcl) {
    Assert.notNull(pentahoAcl);

    ObjectIdentity objectIdentity = new ObjectIdentityImpl(RepositoryFile.class, pentahoAcl.getId());

    Acl parentAcl = pentahoAcl.getParentId() != null ? aclService.readAclById(new ObjectIdentityImpl(
        RepositoryFile.class, pentahoAcl.getParentId())) : null;

    PentahoMutableAcl ssAcl = new PentahoMutableAcl(objectIdentity, parentAcl, pentahoAcl.isEntriesInheriting(),
        pentahoSidToSsSid(pentahoAcl.getOwner()));

    List<RepositoryFileAcl.Ace> aces = pentahoAcl.getAces();
    for (int i = 0; i < aces.size(); i++) {
      RepositoryFileAcl.Ace ace = aces.get(i);
      // RepositoryFileAcl.Ace is always granting
      ssAcl.insertAce(i, pentahoPermissionsToSsPermission(ace.getPermissions()), pentahoSidToSsSid(ace.getSid()), true);
    }

    return ssAcl;
  }

  public RepositoryFileAcl ssAclToPentahoAcl(final MutableAcl ssAcl) {
    Assert.notNull(ssAcl);

    Serializable parentId = ssAcl.getParentAcl() != null ? ssAcl.getParentAcl().getObjectIdentity().getIdentifier()
        : null;

    RepositoryFileAcl.Builder builder = new RepositoryFileAcl.Builder(ssAcl.getObjectIdentity().getIdentifier(),
        parentId, ssSidToPentahoSid(ssAcl.getOwner()));

    builder.entriesInheriting(ssAcl.isEntriesInheriting());

    AccessControlEntry[] aces = ssAcl.getEntries();
    for (int i = 0; i < aces.length; i++) {
      RepositoryFileSid recipient = ssSidToPentahoSid(aces[i].getSid());
      EnumSet<RepositoryFilePermission> permissions = ssPermissionToPentahoPermissions(aces[i].getPermission());
      builder.ace(recipient, permissions);
    }

    return builder.build();
  }

  private Sid pentahoSidToSsSid(final RepositoryFileSid pentahoSid) {
    if (pentahoSid.getType() == RepositoryFileSid.Type.USER) {
      return new PrincipalSid(pentahoSid.getName());
    } else {
      return new GrantedAuthoritySid(pentahoSid.getName());
    }
  }

  private RepositoryFileSid ssSidToPentahoSid(final Sid ssSid) {
    String sidName = null;
    RepositoryFileSid.Type sidType = null;
    if (ssSid instanceof PrincipalSid) {
      sidName = ((PrincipalSid) ssSid).getPrincipal();
      sidType = RepositoryFileSid.Type.USER;
    } else if (ssSid instanceof GrantedAuthoritySid) {
      sidName = ((GrantedAuthoritySid) ssSid).getGrantedAuthority();
      sidType = RepositoryFileSid.Type.ROLE;
    } else {
      throw new RuntimeException("unrecognized sid type");
    }
    return new RepositoryFileSid(sidName, sidType);
  }

  private Permission pentahoPermissionsToSsPermission(final EnumSet<RepositoryFilePermission> pentahoPermissions) {
    CumulativePermission ssPermission = new CumulativePermission();

    for (RepositoryFilePermission pentahoPermission : pentahoPermissions) {
      switch (pentahoPermission) {
        case ALL:
          return SpringSecurityRepositoryFilePermission.ALL;
        case APPEND:
          ssPermission.set(SpringSecurityRepositoryFilePermission.APPEND);
          break;
        case DELETE:
          ssPermission.set(SpringSecurityRepositoryFilePermission.DELETE);
          break;
        case DELETE_CHILD:
          ssPermission.set(SpringSecurityRepositoryFilePermission.DELETE_CHILD);
          break;
        case EXECUTE:
          ssPermission.set(SpringSecurityRepositoryFilePermission.EXECUTE);
          break;
        case READ:
          ssPermission.set(SpringSecurityRepositoryFilePermission.READ);
          break;
        case READ_ACL:
          ssPermission.set(SpringSecurityRepositoryFilePermission.READ_ACL);
          break;
        case WRITE:
          ssPermission.set(SpringSecurityRepositoryFilePermission.WRITE);
          break;
        case WRITE_ACL:
          ssPermission.set(SpringSecurityRepositoryFilePermission.WRITE_ACL);
          break;
        default:
          throw new RuntimeException();
      }
    }
    return ssPermission;
  }

  private EnumSet<RepositoryFilePermission> ssPermissionToPentahoPermissions(final Permission ssPermission) {
    int mask = ssPermission.getMask();
    EnumSet<RepositoryFilePermission> permissions = EnumSet.noneOf(RepositoryFilePermission.class);

    if (mask == 0) {
      return permissions;
    }
    if (mask == -1) {
      permissions.add(RepositoryFilePermission.ALL);
      return permissions;
    }
    if ((mask & SpringSecurityRepositoryFilePermission.READ.getMask()) == SpringSecurityRepositoryFilePermission.READ
        .getMask()) {
      permissions.add(RepositoryFilePermission.READ);
      mask &= ~SpringSecurityRepositoryFilePermission.READ.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.APPEND.getMask()) == SpringSecurityRepositoryFilePermission.APPEND
        .getMask()) {
      permissions.add(RepositoryFilePermission.APPEND);
      mask &= ~SpringSecurityRepositoryFilePermission.APPEND.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.DELETE.getMask()) == SpringSecurityRepositoryFilePermission.DELETE
        .getMask()) {
      permissions.add(RepositoryFilePermission.DELETE);
      mask &= ~SpringSecurityRepositoryFilePermission.DELETE.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.DELETE_CHILD.getMask()) == SpringSecurityRepositoryFilePermission.DELETE_CHILD
        .getMask()) {
      permissions.add(RepositoryFilePermission.DELETE_CHILD);
      mask &= ~SpringSecurityRepositoryFilePermission.DELETE_CHILD.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.EXECUTE.getMask()) == SpringSecurityRepositoryFilePermission.EXECUTE
        .getMask()) {
      permissions.add(RepositoryFilePermission.EXECUTE);
      mask &= ~SpringSecurityRepositoryFilePermission.EXECUTE.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.READ_ACL.getMask()) == SpringSecurityRepositoryFilePermission.READ_ACL
        .getMask()) {
      permissions.add(RepositoryFilePermission.READ_ACL);
      mask &= ~SpringSecurityRepositoryFilePermission.READ_ACL.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.WRITE.getMask()) == SpringSecurityRepositoryFilePermission.WRITE
        .getMask()) {
      permissions.add(RepositoryFilePermission.WRITE);
      mask &= ~SpringSecurityRepositoryFilePermission.WRITE.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.WRITE_ACL.getMask()) == SpringSecurityRepositoryFilePermission.WRITE_ACL
        .getMask()) {
      permissions.add(RepositoryFilePermission.WRITE_ACL);
      mask &= ~SpringSecurityRepositoryFilePermission.WRITE_ACL.getMask();
    }
    if (mask != 0) {
      throw new RuntimeException("unrecognized bits in cumulative permission mask=" + ssPermission.getMask()
          + "; leftover unrecognized bit mask=" + mask);
    }
    return permissions;
  }

}
