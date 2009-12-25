package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.jsr283.security.AccessControlEntry;
import org.apache.jackrabbit.api.jsr283.security.AccessControlList;
import org.apache.jackrabbit.api.jsr283.security.AccessControlManager;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicy;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicyIterator;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlList;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.IPentahoMutableAclService;
import org.pentaho.platform.repository.pcr.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository.pcr.jcr.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository.pcr.jcr.UuidNodeIdStrategy;
import org.pentaho.platform.repository.pcr.springsecurity.RepositoryFilePermission;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.AlreadyExistsException;
import org.springframework.security.acls.ChildrenExistException;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.MutableAclService;
import org.springframework.security.acls.NotFoundException;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;
import org.springframework.util.Assert;

/**
 * Jackrabbit-based implementation of {@link MutableAclService}.
 * 
 * <p>
 * All mutating public methods require checkout and checkin calls since the act of simply calling 
 * {@code AccessControlManager.getApplicablePolicies()} (as is done in 
 * {@link #toAcl(SessionImpl, ObjectIdentity, boolean)}) will query that the node is allowed to have the "access 
 * controlled" mixin type added. If the node is checked in, this query will return false. See Jackrabbit's 
 * {@code ItemValidator.hasCondition()}.
 * </p>
 * 
 * @author mlowery
 */
public class JackrabbitMutableAclService implements IPentahoMutableAclService {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JackrabbitMutableAclService.class);

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  private NodeIdStrategy nodeIdStrategy;

  private AclAuthorizationStrategy aclAuthorizationStrategy = new NoOpAclAuthorizationStrategy();

  private AuditLogger auditLogger;

  private Map<Permission, List<String>> permissionToPrivilegeNamesMap;

  private Map<String, List<Permission>> privilegeNameToPermissionsMap;

  private IAclHelper aclHelper = new AclHelper();

  /**
   * The name of the authority which is granted to all authenticated users, regardless of tenant.
   */
  private String commonAuthenticatedAuthorityName;

  // ~ Constructors ====================================================================================================

  public JackrabbitMutableAclService(final JcrTemplate jcrTemplate, final String commonAuthenticatedAuthorityName,
      final AuditLogger auditLogger, Map<Permission, List<String>> permissionToPrivilegeNamesMap,
      Map<String, List<Permission>> privilegeNameToPermissionsMap) {
    super();
    this.jcrTemplate = jcrTemplate;
    this.commonAuthenticatedAuthorityName = commonAuthenticatedAuthorityName;
    this.nodeIdStrategy = new UuidNodeIdStrategy(jcrTemplate);
    this.auditLogger = auditLogger;
    this.permissionToPrivilegeNamesMap = permissionToPrivilegeNamesMap;
    this.privilegeNameToPermissionsMap = privilegeNameToPermissionsMap;
  }

  // ~ Methods =========================================================================================================

  private Privilege getPrivilege(final String name, final SessionImpl jrSession) throws RepositoryException {
    return new PrivilegeRegistry(jrSession).getPrivilege(name);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * In Jackrabbit 1.6 (and maybe 2.0), objects already have an AccessControlPolicy of type AccessControlList. It is 
   * empty by default with implicit read access for everyone.
   * </p>
   */
  public MutableAcl createAcl(final ObjectIdentity objectIdentity) throws AlreadyExistsException {
    return (MutableAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node node = nodeIdStrategy.findNodeById(session, objectIdentity.getIdentifier());
        if (node == null) {
          throw new NotFoundException(String.format("node with id [%s] not found", objectIdentity.getIdentifier()));
        }

        JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            node);

        // TODO mlowery set owner to currently authenticated user
        Assert.isInstanceOf(SessionImpl.class, session);
        SessionImpl jrSession = (SessionImpl) session;

        String absPath = node.getPath();
        AccessControlManager acMgr = jrSession.getAccessControlManager();

        AccessControlPolicyIterator iter = acMgr.getApplicablePolicies(absPath);
        // acMrg.getApplicablePolicies returns non-empty iterator when there is no existing ACL on the node; if 
        // non-empty, call setPolicy on the absPath for the node; subsequent calls then use 
        // acMgr.getPolicies(absPath)[0]
        if (iter.hasNext()) {
          AccessControlPolicy acPolicy = iter.nextAccessControlPolicy();
          acMgr.setPolicy(absPath, acPolicy);
        }

        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            node, "[system] created ACL");

        Acl acl = toAcl(jrSession, pentahoJcrConstants, objectIdentity);
        return acl;
      }
    });
  }

  public void deleteAcl(final ObjectIdentity objectIdentity, final boolean deleteChildren)
      throws ChildrenExistException {
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;
        Node node = nodeIdStrategy.findNodeById(session, objectIdentity.getIdentifier());
        if (node == null) {
          throw new NotFoundException(String.format("node with id [%s] not found", objectIdentity.getIdentifier()));
        }

        JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            node);

        if (deleteChildren) {
          ObjectIdentity[] children = findChildren(objectIdentity);
          if (children != null) {
            for (int i = 0; i < children.length; i++) {
              deleteAcl(children[i], true);
            }
          }
        } else {

          ObjectIdentity[] children = findChildren(objectIdentity);
          if (children != null) {
            throw new ChildrenExistException("Cannot delete '" + objectIdentity + "' (has " + children.length
                + " children)");
          }
        }

        String absPath = node.getPath();
        AccessControlManager acMgr = jrSession.getAccessControlManager();
        AccessControlPolicy acPolicy = getAccessControlPolicy(acMgr, absPath);
        acMgr.removePolicy(absPath, acPolicy);
        jrSession.save();
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            node, "[system] deleted ACL");
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   * 
   */
  public MutableAcl updateAcl(final MutableAcl acl) throws NotFoundException {
    return (MutableAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;
        Node node = nodeIdStrategy.findNodeById(session, acl.getObjectIdentity().getIdentifier());
        if (node == null) {
          throw new NotFoundException(String.format("node with id [%s] not found", acl.getObjectIdentity()
              .getIdentifier()));
        }

        JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            node);

        String absPath = node.getPath();
        AccessControlManager acMgr = jrSession.getAccessControlManager();
        AccessControlPolicy acPolicy = getAccessControlPolicy(acMgr, absPath);
        Assert.isInstanceOf(JackrabbitAccessControlList.class, acPolicy);
        // cast to JackrabbitAccessControlList to get addEntry call (for isAllow parameter)
        JackrabbitAccessControlList acList = (JackrabbitAccessControlList) acPolicy;
        // clear all entries
        AccessControlEntry[] acEntries = acList.getAccessControlEntries();
        for (int i = 0; i < acEntries.length; i++) {
          acList.removeAccessControlEntry(acEntries[i]);
        }
        // add entries
        for (int i = 0; i < acl.getEntries().length; i++) {
          org.springframework.security.acls.AccessControlEntry ssAce = acl.getEntries()[i];
          Principal principal = null;

          if (ssAce.getSid() instanceof PrincipalSid) {
            principal = jrSession.getPrincipalManager().getPrincipal(((PrincipalSid) ssAce.getSid()).getPrincipal());
          } else if (ssAce.getSid() instanceof GrantedAuthoritySid) {
            principal = jrSession.getPrincipalManager().getPrincipal(
                ((GrantedAuthoritySid) ssAce.getSid()).getGrantedAuthority());
          } else {
            principal = jrSession.getPrincipalManager().getPrincipal(((JackrabbitSid) ssAce.getSid()).getName());
          }
          acList.addEntry(principal, permissionToPrivileges(ssAce.getPermission(), jrSession), ssAce.isGranting());
        }
        acMgr.setPolicy(absPath, acList);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            node, "[system] updated ACL");
        Acl readAcl = readAclById(acl.getObjectIdentity());
        Assert.isInstanceOf(MutableAcl.class, readAcl, "MutableAcl should be been returned");

        return readAcl;
      }
    });
  }

  private AccessControlPolicy getAccessControlPolicy(final AccessControlManager acMgr, final String absPath)
      throws RepositoryException {
    AccessControlPolicy[] policies = acMgr.getPolicies(absPath);
    Assert.notEmpty(policies, "most likely due to calling readAclById before calling createAcl");
    return policies[0];
  }

  private Privilege[] permissionToPrivileges(final Permission permission, final SessionImpl jrSession)
      throws RepositoryException {
    List<String> privilegeNames = permissionToPrivilegeNamesMap.get(permission);
    Assert.notEmpty(privilegeNames, String.format("no privilege names found for permission [%s]", permission));
    List<Privilege> privileges = new ArrayList<Privilege>(privilegeNames.size());
    for (String privilegeName : privilegeNames) {
      privileges.add(getPrivilege(privilegeName, jrSession));
    }
    return privileges.toArray(new Privilege[0]);
  }

  private Permission[] privilegeToPermissions(final Privilege privilege, final SessionImpl jrSession)
      throws RepositoryException {
    String privilegeName = privilege.getName();
    String extendedPrivilegeName = privilegeName;
    int colonIndex = privilegeName.indexOf(":");
    if (colonIndex != -1) {
      String prefix = privilegeName.substring(0, colonIndex);
      extendedPrivilegeName = "{" + jrSession.getNamespaceURI(prefix) + "}" + privilegeName.substring(colonIndex + 1);
    }
    List<Permission> permissions = privilegeNameToPermissionsMap.get(extendedPrivilegeName);
    // permissions here can be empty but not null
    Assert.notNull(permissions, String.format("no permissions found for privilege name [%s]", extendedPrivilegeName));
    return permissions.toArray(new Permission[0]);
  }

  public ObjectIdentity[] findChildren(final ObjectIdentity parentIdentity) {
    return (ObjectIdentity[]) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;
        Node node = nodeIdStrategy.findNodeById(jrSession, parentIdentity.getIdentifier());
        if (node == null) {
          throw new NotFoundException(String.format("node with id [%s] not found", parentIdentity.getIdentifier()));
        }
        RepositoryFile file = JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, nodeIdStrategy, node);
        if (file.isFolder()
            && !JcrRepositoryFileUtils.getChildren(session, pentahoJcrConstants, nodeIdStrategy, file).isEmpty()) {
          List<RepositoryFile> children = JcrRepositoryFileUtils.getChildren(session, pentahoJcrConstants,
              nodeIdStrategy, file);
          ObjectIdentity[] oids = new ObjectIdentity[children.size()];
          for (int i = 0; i < oids.length; i++) {
            oids[i] = new ObjectIdentityImpl(RepositoryFile.class, children.get(i).getId());
          }
          return oids;
        } else {
          return null; // per AclService interface contract
        }
      }
    });
  }

  public Acl readAclById(ObjectIdentity object, Sid[] sids) throws NotFoundException {
    Map<ObjectIdentity, Acl> map = readAclsById(new ObjectIdentity[] { object }, sids);
    Assert.isTrue(map.containsKey(object), "There should have been an Acl entry for ObjectIdentity " + object);

    return map.get(object);
  }

  public Acl readAclById(ObjectIdentity object) throws NotFoundException {
    return readAclById(object, null);
  }

  public Map<ObjectIdentity, Acl> readAclsById(ObjectIdentity[] objects) throws NotFoundException {
    return readAclsById(objects, null);
  }

  @SuppressWarnings("unchecked")
  public Map<ObjectIdentity, Acl> readAclsById(final ObjectIdentity[] objects, final Sid[] sids)
      throws NotFoundException {
    return (Map<ObjectIdentity, Acl>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Map<ObjectIdentity, Acl> map = new HashMap<ObjectIdentity, Acl>();
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;

        for (int i = 0; i < objects.length; i++) {
          map.put(objects[i], toAcl(jrSession, pentahoJcrConstants, objects[i]));
        }

        return map;
      }
    });

  }

  private boolean isReferenceable(final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException {
    Value[] mixinTypeNames = node.getProperty(pentahoJcrConstants.getJCR_MIXINTYPES()).getValues();
    for (Value v : mixinTypeNames) {
      if (pentahoJcrConstants.getMIX_REFERENCEABLE().equals(v.getString())) {
        return true;
      }
    }
    return false;
  }

  private Acl toAcl(final SessionImpl session, final PentahoJcrConstants pentahoJcrConstants,
      final ObjectIdentity objectIdentity) throws RepositoryException {
    return toAcl(session, pentahoJcrConstants, objectIdentity, true);
  }

  private Acl toAcl(final SessionImpl jrSession, final PentahoJcrConstants pentahoJcrConstants,
      final ObjectIdentity objectIdentity, final boolean fetchParent) throws RepositoryException {
    try {
      Node node = nodeIdStrategy.findNodeById(jrSession, objectIdentity.getIdentifier());
      if (node == null) {
        throw new NotFoundException(String.format("node with id [%s] not found", objectIdentity.getIdentifier()));
      }
      String absPath = node.getPath();
      AccessControlManager acMgr = jrSession.getAccessControlManager();
      AccessControlPolicy acPolicy = getAccessControlPolicy(acMgr, absPath);

      Acl parentAcl = null;
      if (fetchParent) {
        Node parentNode = node.getParent();
        // all Pentaho nodes are referenceable; if we hit one that is not, that is the root
        if (isReferenceable(pentahoJcrConstants, parentNode)) {
          parentAcl = toAcl(jrSession, pentahoJcrConstants, new ObjectIdentityImpl(objectIdentity.getClass(),
              nodeIdStrategy.getId(parentNode)), false);
        }
      }

      Serializable ignoredId = new String("ignored");
      Sid[] loadedSids = null;

      AclImpl acl = new AclImpl(objectIdentity, ignoredId, aclAuthorizationStrategy, auditLogger, parentAcl,
          loadedSids, true, new PrincipalSid("ignored"));
      Assert.isInstanceOf(AccessControlList.class, acPolicy);
      AccessControlList acList = (AccessControlList) acPolicy;
      AccessControlEntry[] acEntries = acList.getAccessControlEntries();
      for (int i = 0; i < acEntries.length; i++) {
        Assert.isInstanceOf(JackrabbitAccessControlEntry.class, acEntries[i]);
        JackrabbitAccessControlEntry jrAce = (JackrabbitAccessControlEntry) acEntries[i];
        Principal principal = jrAce.getPrincipal();
        logger.debug(String.format("principal class [%s]", principal.getClass().getName()));
        Privilege[] privileges = jrAce.getPrivileges();
        int aceIndex = 0;
        for (int j = 0; j < privileges.length; j++) {

          // TODO principal sid??? need to be able to lookup principal name
          Sid sid = null;
          if (principal instanceof UserPrincipal) {
            sid = new PrincipalSid(principal.getName());
          } else if (principal instanceof SpringSecurityGrantedAuthorityPrincipal) {
            sid = new GrantedAuthoritySid(principal.getName());
          } else if (principal instanceof AdminPrincipal) {
            sid = new PrincipalSid(principal.getName());
          } else {
            throw new RuntimeException("unknown principal type: " + principal.getClass().getName());
          }

          Permission[] permissions = privilegeToPermissions(privileges[j], jrSession);
          for (Permission permission : permissions) {
            acl.insertAce(aceIndex++, permission, sid, jrAce.isAllow());
          }
        }
      }
      return acl;
    } catch (RepositoryException e) {
      throw jcrTemplate.convertJcrAccessException(e);
    }
  }

  public static class JackrabbitSid implements Sid {
    private static final long serialVersionUID = 7653528032566695538L;

    private String name;

    public JackrabbitSid(String name) {
      super();
      this.name = name;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
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
      JackrabbitSid other = (JackrabbitSid) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "JackrabbitSid [" + name + "]";
    }

    public String getName() {
      return name;
    }

  }

  public void setNodeIdStrategy(final NodeIdStrategy nodeIdStrategy) {
    Assert.notNull(nodeIdStrategy);
    this.nodeIdStrategy = nodeIdStrategy;
  }

  /**
   * An {@link AclAuthorizationStrategy} that never throws an {@code AccessDeniedException}. 
   * 
   * <p>
   * {@code AclAuthorizationStrategy} is used by {@code AclImpl} for access control when modifying the ACL itself. This
   * implementation of {@code AclAuthorizationStrategy} is applicable when the datastore itself enforces ACL 
   * modifications, such as is done in Jackrabbit.
   * </p>
   *
   */
  private class NoOpAclAuthorizationStrategy implements AclAuthorizationStrategy {
    public void securityCheck(final Acl acl, final int changeType) {
    }
  }

  public static interface IAclHelper {
    void addPermission(final ObjectIdentity oid, final Sid recipient, final Permission permission,
        final boolean granting);

    MutableAcl createAndInitializeAcl(final ObjectIdentity oid, final ObjectIdentity parentOid,
        final boolean entriesInheriting, final Sid owner);

    void setFullControl(final ObjectIdentity oid, final Sid sid, final Permission permission);
  }

  private class AclHelper implements IAclHelper {

    public void addPermission(ObjectIdentity oid, Sid recipient, Permission permission, boolean granting) {
      Assert.notNull(oid);
      Assert.notNull(recipient);
      Assert.notNull(permission);
      MutableAcl acl = (MutableAcl) readAclById(oid);
      Assert.notNull(acl);
      acl.insertAce(acl.getEntries().length, permission, recipient, granting);
      updateAcl(acl);
      logger.debug("added ace: oid=" + oid + ", sid=" + recipient + ", permission=" + permission + ", granting="
          + granting);
    }

    public MutableAcl createAndInitializeAcl(final ObjectIdentity oid, final ObjectIdentity parentOid,
        final boolean entriesInheriting, final Sid owner) {
      MutableAcl acl = createAcl(oid);
      // link up parent if parent not null
      if (parentOid != null) {
        Acl newParent = readAclById(parentOid);
        acl.setParent(newParent);
      }
      if (!entriesInheriting) {
        acl.setEntriesInheriting(false);
        // TODO mlowery fix this null call
        setFullControl(oid, owner, null);
      }
      return updateAcl(acl);
    }

    public void setFullControl(final ObjectIdentity oid, final Sid sid, final Permission permission) {
      // TODO mlowery don't call addPermission as this is a connect to jcr per addPermission call
      // TODO mlowery don't import RepositoryFilePermission
      addPermission(oid, sid, RepositoryFilePermission.APPEND, true);
      addPermission(oid, sid, RepositoryFilePermission.DELETE, true);
      addPermission(oid, sid, RepositoryFilePermission.DELETE_CHILD, true);
      // TODO uncomment this when custom privileges are supported
      //    internalAddPermission(file, sid, RepositoryFilePermission.EXECUTE);
      addPermission(oid, sid, RepositoryFilePermission.READ, true);
      addPermission(oid, sid, RepositoryFilePermission.READ_ACL, true);
      // TODO uncomment this when custom privileges are supported
      //    internalAddPermission(file, sid, RepositoryFilePermission.TAKE_OWNERSHIP);
      addPermission(oid, sid, RepositoryFilePermission.WRITE, true);
      addPermission(oid, sid, RepositoryFilePermission.WRITE_ACL, true);
    }

  }

  public void addPermission(final ObjectIdentity oid, final Sid recipient, final Permission permission,
      final boolean granting) {
    aclHelper.addPermission(oid, recipient, permission, granting);
  }

  public MutableAcl createAndInitializeAcl(final ObjectIdentity oid, final ObjectIdentity parentOid,
      final boolean entriesInheriting, final Sid owner) {
    return aclHelper.createAndInitializeAcl(oid, parentOid, entriesInheriting, owner);
  }

  public void setFullControl(final ObjectIdentity oid, final Sid sid, final Permission permission) {
    aclHelper.setFullControl(oid, sid, permission);
  }

  public void setAclHelper(final IAclHelper aclHelper) {
    Assert.notNull(aclHelper);
    this.aclHelper = aclHelper;
  }

}
