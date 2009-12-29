package org.pentaho.platform.repository.pcr;

import java.util.EnumSet;

import org.pentaho.platform.api.repository.Permission;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.repository.pcr.PentahoContentRepository.IAclConverter;
import org.pentaho.platform.repository.pcr.springsecurity.RepositoryFilePermission;
import org.springframework.security.acls.AccessControlEntry;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.util.Assert;

/**
 * Default {@code IAclConverter} implementation.
 * 
 * @author mlowery
 */
public class DefaultAclConverter implements IAclConverter {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public DefaultAclConverter() {
    super();
  }

  // ~ Methods =========================================================================================================

 
  public Acl pentahoAclToSsAcl(final RepositoryFileAcl pentahoAcl) {
    // TODO Auto-generated method stub 
    return null;
  }

  public RepositoryFileAcl ssAclToPentahoAcl(final Acl ssAcl) {
    Assert.notNull(ssAcl);

    String sidName = null;
    RepositoryFileSid.Type sidType = null;
    org.springframework.security.acls.sid.Sid sid = ssAcl.getOwner();
    if (sid instanceof PrincipalSid) {
      sidName = ((PrincipalSid) sid).getPrincipal();
      sidType = RepositoryFileSid.Type.USER;
    } else if (sid instanceof GrantedAuthoritySid) {
      sidName = ((GrantedAuthoritySid) sid).getGrantedAuthority();
      sidType = RepositoryFileSid.Type.ROLE;
    } else {
      throw new RuntimeException("unrecognized sid type");
    }

    RepositoryFileAcl.Builder builder = new RepositoryFileAcl.Builder(ssAcl.getObjectIdentity().getIdentifier(),
        sidName, sidType);

    AccessControlEntry[] aces = ssAcl.getEntries();
    for (int i = 0; i < aces.length; i++) {
      RepositoryFileSid recipient = springSecuritySidToRepositoryFileSid(aces[i].getSid());
      EnumSet<Permission> permissions = ssPermissionToPentahoPermissions(aces[i].getPermission());
      builder.ace(recipient, permissions);
    }

    return builder.build();
  }

  private org.springframework.security.acls.sid.Sid repositoryFileSidToSpringSecuritySid(
      final RepositoryFileSid pentahoSid) {
    return null;

  }

  private RepositoryFileSid springSecuritySidToRepositoryFileSid(final org.springframework.security.acls.sid.Sid ssSid) {
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

  private EnumSet<Permission> ssPermissionToPentahoPermissions(
      final org.springframework.security.acls.Permission ssPermission) {
    int mask = ssPermission.getMask();
    EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);

    if (mask == 0) {
      return permissions;
    }
    if (mask == -1) {
      permissions.add(Permission.ALL);
      return permissions;
    }
    if ((mask & RepositoryFilePermission.READ.getMask()) == RepositoryFilePermission.READ.getMask()) {
      permissions.add(Permission.READ);
      mask &= ~RepositoryFilePermission.READ.getMask();
    }
    if ((mask & RepositoryFilePermission.APPEND.getMask()) == RepositoryFilePermission.APPEND.getMask()) {
      permissions.add(Permission.APPEND);
      mask &= ~RepositoryFilePermission.APPEND.getMask();
    }
    if ((mask & RepositoryFilePermission.DELETE.getMask()) == RepositoryFilePermission.DELETE.getMask()) {
      permissions.add(Permission.DELETE);
      mask &= ~RepositoryFilePermission.DELETE.getMask();
    }
    if ((mask & RepositoryFilePermission.DELETE_CHILD.getMask()) == RepositoryFilePermission.DELETE_CHILD.getMask()) {
      permissions.add(Permission.DELETE_CHILD);
      mask &= ~RepositoryFilePermission.DELETE_CHILD.getMask();
    }
    if ((mask & RepositoryFilePermission.EXECUTE.getMask()) == RepositoryFilePermission.EXECUTE.getMask()) {
      permissions.add(Permission.EXECUTE);
      mask &= ~RepositoryFilePermission.EXECUTE.getMask();
    }
    if ((mask & RepositoryFilePermission.READ_ACL.getMask()) == RepositoryFilePermission.READ_ACL.getMask()) {
      permissions.add(Permission.READ_ACL);
      mask &= ~RepositoryFilePermission.READ_ACL.getMask();
    }
    if ((mask & RepositoryFilePermission.WRITE.getMask()) == RepositoryFilePermission.WRITE.getMask()) {
      permissions.add(Permission.WRITE);
      mask &= ~RepositoryFilePermission.WRITE.getMask();
    }
    if ((mask & RepositoryFilePermission.WRITE_ACL.getMask()) == RepositoryFilePermission.WRITE_ACL.getMask()) {
      permissions.add(Permission.WRITE_ACL);
      mask &= ~RepositoryFilePermission.WRITE_ACL.getMask();
    }
    if (mask != 0) {
      throw new RuntimeException("unrecognized bits in cumulative permission mask=" + ssPermission.getMask()
          + "; leftover unrecognized bit mask=" + mask);
    }
    return permissions;
  }

}
