package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.repository.pcr.jcr.jackrabbit.JackrabbitRepositoryFileAclDao.IPermissionConversionHelper;
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

  protected Multimap<RepositoryFilePermission, String> permissionEnumToPrivilegeNamesMap;

  protected Multimap<String, RepositoryFilePermission> privilegeNameToPermissionEnumsMap;

  // ~ Constructors ====================================================================================================

  public DefaultPermissionConversionHelper() {
    super();
    initMaps();

  }

  // ~ Methods =========================================================================================================

  public Privilege[] pentahoPermissionsToJackrabbitPrivileges(final SessionImpl jrSession,
      final EnumSet<RepositoryFilePermission> permissions) throws RepositoryException {
    Assert.notNull(jrSession);
    Assert.notNull(permissions);
    Assert.notEmpty(permissions);
    PrivilegeRegistry privilegeRegistry = new PrivilegeRegistry(jrSession);

    Set<Privilege> privileges = new HashSet<Privilege>();

    for (RepositoryFilePermission currentPermission : permissions) {
      if (permissionEnumToPrivilegeNamesMap.containsKey(currentPermission)) {
        Collection<String> privNames = permissionEnumToPrivilegeNamesMap.get(currentPermission);
        for (String privName : privNames) {
          privileges.add(privilegeRegistry.getPrivilege(privName));
        }
      } else {
        logger.debug("skipping permission=" + currentPermission + " as it doesn't have any corresponding privileges");
      }
    }

    Assert.isTrue(!privileges.isEmpty(), "no privileges; see previous 'skipping permission' messages");

    return privileges.toArray(new Privilege[0]);
  }

  public EnumSet<RepositoryFilePermission> jackrabbitPrivilegesToPentahoPermissions(final SessionImpl jrSession,
      final Privilege[] privileges) throws RepositoryException {
    Assert.notNull(jrSession);
    Assert.notNull(privileges);

    EnumSet<RepositoryFilePermission> permissions = EnumSet.noneOf(RepositoryFilePermission.class);

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

      if (privilegeNameToPermissionEnumsMap.containsKey(extendedPrivilegeName)) {
        Collection<RepositoryFilePermission> permEnums = privilegeNameToPermissionEnumsMap.get(extendedPrivilegeName);
        for (RepositoryFilePermission perm : permEnums) {
          permissions.add(perm);
        }
      } else {
        logger.debug("skipping privilege with name=" + extendedPrivilegeName
            + " as it doesn't have any corresponding permissions");
      }
    }

    Assert.isTrue(!permissions.isEmpty(), "no permissions; see previous 'skipping privilege' messages");

    return permissions;
  }

  protected void initMaps() {
    permissionEnumToPrivilegeNamesMap = HashMultimap.create();

    // READ
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.READ, Privilege.JCR_READ);
    // WRITE
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.WRITE, PrivilegeRegistry.REP_WRITE);
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.WRITE, Privilege.JCR_VERSION_MANAGEMENT);
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.WRITE, Privilege.JCR_LOCK_MANAGEMENT);
    // DELETE
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.DELETE, Privilege.JCR_REMOVE_NODE);
    // APPEND
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.APPEND, PrivilegeRegistry.REP_WRITE);
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.APPEND, Privilege.JCR_VERSION_MANAGEMENT);
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.APPEND, Privilege.JCR_LOCK_MANAGEMENT);
    // DELETE_CHILD
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.DELETE_CHILD, Privilege.JCR_REMOVE_CHILD_NODES);
    // READ_ACL
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.READ_ACL, Privilege.JCR_READ_ACCESS_CONTROL);
    // WRITE_ACL
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.WRITE_ACL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
    // ALL
    permissionEnumToPrivilegeNamesMap.put(RepositoryFilePermission.ALL, Privilege.JCR_ALL);

    // None of the following translate into a Privilege:
    // RepositoryFilePermission.EXECUTE (READ is used for both READ and EXECUTE)

    privilegeNameToPermissionEnumsMap = HashMultimap.create();
    // JCR_READ
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_READ, RepositoryFilePermission.READ);
    // JCR_WRITE
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_WRITE, RepositoryFilePermission.WRITE);
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_WRITE, RepositoryFilePermission.APPEND);
    // REP_WRITE (Jackrabbit's combination of Privilege.JCR_WRITE and Privilege.JCR_NODE_TYPE_MNGMT
    privilegeNameToPermissionEnumsMap.put(PrivilegeRegistry.REP_WRITE, RepositoryFilePermission.WRITE);
    privilegeNameToPermissionEnumsMap.put(PrivilegeRegistry.REP_WRITE, RepositoryFilePermission.APPEND);
    // JCR_REMOVE_NODE
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_REMOVE_NODE, RepositoryFilePermission.DELETE);
    // JCR_REMOVE_CHILD_NODES
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_REMOVE_CHILD_NODES, RepositoryFilePermission.DELETE_CHILD);
    // JCR_READ_ACCESS_CONTROL
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_READ_ACCESS_CONTROL, RepositoryFilePermission.READ_ACL);
    // JCR_MODIFY_ACCESS_CONTROL
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_MODIFY_ACCESS_CONTROL, RepositoryFilePermission.WRITE_ACL);
    // JCR_ALL
    privilegeNameToPermissionEnumsMap.put(Privilege.JCR_ALL, RepositoryFilePermission.ALL);

    // None of the following translate into a RepositoryFilePermission:
    // JCR_NODE_TYPE_MANAGEMENT
    // JCR_VERSION_MANAGEMENT
    // JCR_LOCK_MANAGEMENT
  }

}
