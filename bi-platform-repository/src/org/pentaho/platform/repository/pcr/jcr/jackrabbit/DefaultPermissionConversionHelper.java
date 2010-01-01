package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.pentaho.platform.repository.pcr.jcr.jackrabbit.JackrabbitMutableAclService.IPermissionConversionHelper;
import org.pentaho.platform.repository.pcr.springsecurity.SpringSecurityRepositoryFilePermission;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.util.Assert;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Default {@link IPermissionConversionHelper} implementation.
 * 
 * @author mlowery
 */
public class DefaultPermissionConversionHelper implements IPermissionConversionHelper {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultPermissionConversionHelper.class);

  // ~ Instance fields =================================================================================================

  protected Multimap<Integer, String> permissionIntegerToPrivilegeNamesMap;

  protected Multimap<String, Integer> privilegeNameToPermissionIntegersMap;

  // ~ Constructors ====================================================================================================

  public DefaultPermissionConversionHelper() {
    super();
    initMaps();

  }

  // ~ Methods =========================================================================================================

  public Privilege[] permissionToPrivileges(final SessionImpl jrSession, final Permission permission)
      throws RepositoryException {
    Assert.notNull(jrSession);
    Assert.notNull(permission);
    PrivilegeRegistry privilegeRegistry = new PrivilegeRegistry(jrSession);

    Set<Privilege> privileges = new HashSet<Privilege>();
    Set<Permission> permissions = new HashSet<Permission>();

    // flatten perms
    if (permission instanceof CumulativePermission) {
      // if we're here, then we have a "cumulative" permission with multiple bits set
      permissions.addAll(breakDownCumulativePermission((CumulativePermission) permission));
    } else {
      permissions.add(permission);
    }

    for (Permission currentPermission : permissions) {
      if (permissionIntegerToPrivilegeNamesMap.containsKey(currentPermission.getMask())) {
        Collection<String> privNames = permissionIntegerToPrivilegeNamesMap.get(currentPermission.getMask());
        for (String privName : privNames) {
          privileges.add(privilegeRegistry.getPrivilege(privName));
        }
      } else {
        logger.debug("skipping permission with mask=" + currentPermission.getMask()
            + " as it doesn't have any corresponding privileges");
      }
    }

    Assert.isTrue(!privileges.isEmpty(), "no privileges; see previous 'skipping permission' messages");

    return privileges.toArray(new Privilege[0]);
  }

  public Permission privilegesToPermission(final SessionImpl jrSession, final Privilege[] privileges)
      throws RepositoryException {
    Assert.notNull(jrSession);
    Assert.notNull(privileges);

    Set<Permission> permissions = new HashSet<Permission>();

    for (Privilege privilege : privileges) {
      // this privilege name is of the format xyz:blah where xyz is the namespace prefix;
      // convert it to match the Privilege.JCR_* string constants from Jackrabbit
      String extendedPrivilegeName = privilege.getName();
      String privilegeName = privilege.getName();
      int colonIndex = privilegeName.indexOf(":"); //$NON-NLS-1$
      if (colonIndex > -1) {
        String namespaceUri = jrSession.getNamespaceURI(privilegeName.substring(0, colonIndex));
        extendedPrivilegeName = "{" + namespaceUri + "}" + privilegeName.substring(colonIndex + 1); //$NON-NLS-1$ //$NON-NLS-2$
      }

      if (privilegeNameToPermissionIntegersMap.containsKey(extendedPrivilegeName)) {
        Collection<Integer> permIntegers = privilegeNameToPermissionIntegersMap.get(extendedPrivilegeName);
        for (Integer permInteger : permIntegers) {
          Permission permission = SpringSecurityRepositoryFilePermission.buildFromMask(permInteger);
          Assert.isTrue(!(permission instanceof CumulativePermission));
          permissions.add(permission);
        }
      } else {
        logger.debug("skipping privilege with name=" + extendedPrivilegeName
            + " as it doesn't have any corresponding permissions");
      }
    }

    Assert.isTrue(!permissions.isEmpty(), "no permissions; see previous 'skipping privilege' messages");

    if (permissions.size() == 1) {
      return permissions.iterator().next();
    }

    // reduce to a single permission
    CumulativePermission finalPerm = new CumulativePermission();
    for (Permission permission : permissions) {
      finalPerm.set(permission);
    }

    return finalPerm;
  }

  protected void initMaps() {
    permissionIntegerToPrivilegeNamesMap = HashMultimap.create();

    // READ
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.READ.getMask(), Privilege.JCR_READ);
    // WRITE
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.WRITE.getMask(), PrivilegeRegistry.REP_WRITE);
    permissionIntegerToPrivilegeNamesMap
        .put(SpringSecurityRepositoryFilePermission.WRITE.getMask(), Privilege.JCR_VERSION_MANAGEMENT);
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.WRITE.getMask(), Privilege.JCR_LOCK_MANAGEMENT);
    // DELETE
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.DELETE.getMask(), Privilege.JCR_REMOVE_NODE);
    // APPEND
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.APPEND.getMask(), PrivilegeRegistry.REP_WRITE);
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.APPEND.getMask(),
        Privilege.JCR_VERSION_MANAGEMENT);
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.APPEND.getMask(), Privilege.JCR_LOCK_MANAGEMENT);
    // DELETE_CHILD
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.DELETE_CHILD.getMask(),
        Privilege.JCR_REMOVE_CHILD_NODES);
    // READ_ACL
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.READ_ACL.getMask(),
        Privilege.JCR_READ_ACCESS_CONTROL);
    // WRITE_ACL
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.WRITE_ACL.getMask(),
        Privilege.JCR_MODIFY_ACCESS_CONTROL);
    // ALL
    permissionIntegerToPrivilegeNamesMap.put(SpringSecurityRepositoryFilePermission.ALL.getMask(), Privilege.JCR_ALL);

    // None of the following translate into a Privilege:
    // RepositoryFilePermission.EXECUTE (READ is used for both READ and EXECUTE)

    privilegeNameToPermissionIntegersMap = HashMultimap.create();
    // JCR_READ
    privilegeNameToPermissionIntegersMap.put(Privilege.JCR_READ, SpringSecurityRepositoryFilePermission.READ.getMask());
    // JCR_WRITE
    privilegeNameToPermissionIntegersMap.put(Privilege.JCR_WRITE, SpringSecurityRepositoryFilePermission.WRITE.getMask());
    privilegeNameToPermissionIntegersMap.put(Privilege.JCR_WRITE, SpringSecurityRepositoryFilePermission.APPEND.getMask());
    // REP_WRITE (Jackrabbit's combination of Privilege.JCR_WRITE and Privilege.JCR_NODE_TYPE_MNGMT
    privilegeNameToPermissionIntegersMap.put(PrivilegeRegistry.REP_WRITE, SpringSecurityRepositoryFilePermission.WRITE.getMask());
    privilegeNameToPermissionIntegersMap.put(PrivilegeRegistry.REP_WRITE, SpringSecurityRepositoryFilePermission.APPEND.getMask());
    // JCR_REMOVE_NODE
    privilegeNameToPermissionIntegersMap.put(Privilege.JCR_REMOVE_NODE, SpringSecurityRepositoryFilePermission.DELETE.getMask());
    // JCR_REMOVE_CHILD_NODES
    privilegeNameToPermissionIntegersMap.put(Privilege.JCR_REMOVE_CHILD_NODES, SpringSecurityRepositoryFilePermission.DELETE_CHILD
        .getMask());
    // JCR_READ_ACCESS_CONTROL
    privilegeNameToPermissionIntegersMap.put(Privilege.JCR_READ_ACCESS_CONTROL, SpringSecurityRepositoryFilePermission.READ_ACL
        .getMask());
    // JCR_MODIFY_ACCESS_CONTROL
    privilegeNameToPermissionIntegersMap.put(Privilege.JCR_MODIFY_ACCESS_CONTROL, SpringSecurityRepositoryFilePermission.WRITE_ACL
        .getMask());
    // JCR_ALL
    privilegeNameToPermissionIntegersMap.put(Privilege.JCR_ALL, SpringSecurityRepositoryFilePermission.ALL.getMask());

    // None of the following translate into a RepositoryFilePermission:
    // JCR_NODE_TYPE_MANAGEMENT
    // JCR_VERSION_MANAGEMENT
    // JCR_LOCK_MANAGEMENT
  }

  protected List<Permission> breakDownCumulativePermission(final CumulativePermission permission) {
    int mask = permission.getMask();
    List<Permission> permissions = new ArrayList<Permission>();

    if (mask == 0) {
      return permissions;
    }
    if (mask == -1) {
      permissions.add(SpringSecurityRepositoryFilePermission.ALL);
      return permissions;
    }
    if ((mask & SpringSecurityRepositoryFilePermission.READ.getMask()) == SpringSecurityRepositoryFilePermission.READ.getMask()) {
      permissions.add(SpringSecurityRepositoryFilePermission.READ);
      mask &= ~SpringSecurityRepositoryFilePermission.READ.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.APPEND.getMask()) == SpringSecurityRepositoryFilePermission.APPEND.getMask()) {
      permissions.add(SpringSecurityRepositoryFilePermission.APPEND);
      mask &= ~SpringSecurityRepositoryFilePermission.APPEND.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.DELETE.getMask()) == SpringSecurityRepositoryFilePermission.DELETE.getMask()) {
      permissions.add(SpringSecurityRepositoryFilePermission.DELETE);
      mask &= ~SpringSecurityRepositoryFilePermission.DELETE.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.DELETE_CHILD.getMask()) == SpringSecurityRepositoryFilePermission.DELETE_CHILD.getMask()) {
      permissions.add(SpringSecurityRepositoryFilePermission.DELETE_CHILD);
      mask &= ~SpringSecurityRepositoryFilePermission.DELETE_CHILD.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.EXECUTE.getMask()) == SpringSecurityRepositoryFilePermission.EXECUTE.getMask()) {
      permissions.add(SpringSecurityRepositoryFilePermission.EXECUTE);
      mask &= ~SpringSecurityRepositoryFilePermission.EXECUTE.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.READ_ACL.getMask()) == SpringSecurityRepositoryFilePermission.READ_ACL.getMask()) {
      permissions.add(SpringSecurityRepositoryFilePermission.READ_ACL);
      mask &= ~SpringSecurityRepositoryFilePermission.READ_ACL.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.WRITE.getMask()) == SpringSecurityRepositoryFilePermission.WRITE.getMask()) {
      permissions.add(SpringSecurityRepositoryFilePermission.WRITE);
      mask &= ~SpringSecurityRepositoryFilePermission.WRITE.getMask();
    }
    if ((mask & SpringSecurityRepositoryFilePermission.WRITE_ACL.getMask()) == SpringSecurityRepositoryFilePermission.WRITE_ACL.getMask()) {
      permissions.add(SpringSecurityRepositoryFilePermission.WRITE_ACL);
      mask &= ~SpringSecurityRepositoryFilePermission.WRITE_ACL.getMask();
    }
    if (mask != 0) {
      throw new RuntimeException("unrecognized bits in cumulative permission mask=" + permission.getMask()
          + "; leftover unrecognized bit mask=" + mask);
    }
    return permissions;
  }
}
