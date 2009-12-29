package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.jsr283.security.AccessControlEntry;
import org.apache.jackrabbit.api.jsr283.security.AccessControlManager;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicy;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicyIterator;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlList;
import org.pentaho.platform.repository.pcr.IPentahoMutableAclService;
import org.pentaho.platform.repository.pcr.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository.pcr.jcr.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository.pcr.jcr.UuidNodeIdStrategy;
import org.pentaho.platform.repository.pcr.springsecurity.PentahoMutableAcl;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.AlreadyExistsException;
import org.springframework.security.acls.ChildrenExistException;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.MutableAclService;
import org.springframework.security.acls.NotFoundException;
import org.springframework.security.acls.Permission;
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

  private IAclHelper aclHelper = new DefaultAclHelper();

  private IPermissionConversionHelper permissionConversionHelper = new DefaultPermissionConversionHelper();

  // ~ Constructors ====================================================================================================

  public JackrabbitMutableAclService(final JcrTemplate jcrTemplate) {
    super();
    this.jcrTemplate = jcrTemplate;
    this.nodeIdStrategy = new UuidNodeIdStrategy(jcrTemplate);
  }

  // ~ Methods =========================================================================================================

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
          Assert.isInstanceOf(IPentahoJackrabbitAccessControlList.class, acPolicy);
          IPentahoJackrabbitAccessControlList jrPolicy = (IPentahoJackrabbitAccessControlList) acPolicy;
          // owner can never be null; give it a dummy value here until it has a "real" value
          jrPolicy.setOwner(jrSession.getPrincipalManager().getEveryone());
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
        IPentahoJackrabbitAccessControlList acList = (IPentahoJackrabbitAccessControlList) acPolicy;

        acList.setEntriesInheriting(acl.isEntriesInheriting());

        if (acl.getOwner() instanceof PrincipalSid) {
          acList.setOwner(jrSession.getPrincipalManager().getPrincipal(((PrincipalSid) acl.getOwner()).getPrincipal()));
        } else {
          acList.setOwner(jrSession.getPrincipalManager().getPrincipal(
              ((GrantedAuthoritySid) acl.getOwner()).getGrantedAuthority()));
        }

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
          } else {
            principal = jrSession.getPrincipalManager().getPrincipal(
                ((GrantedAuthoritySid) ssAce.getSid()).getGrantedAuthority());
          }
          acList.addEntry(principal, permissionConversionHelper
              .permissionToPrivileges(jrSession, ssAce.getPermission()), ssAce.isGranting());
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

  public ObjectIdentity[] findChildren(final ObjectIdentity parentIdentity) {
    throw new UnsupportedOperationException();
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

      Assert.isInstanceOf(IPentahoJackrabbitAccessControlList.class, acPolicy);

      IPentahoJackrabbitAccessControlList acList = (IPentahoJackrabbitAccessControlList) acPolicy;

      Sid owner = null;
      Principal ownerPrincipal = acList.getOwner();
      if (ownerPrincipal instanceof Group) {
        owner = new GrantedAuthoritySid(ownerPrincipal.getName());
      } else {
        owner = new PrincipalSid(ownerPrincipal.getName());
      }

      PentahoMutableAcl acl = new PentahoMutableAcl(objectIdentity, parentAcl, acList.isEntriesInheriting(), owner);
      AccessControlEntry[] acEntries = acList.getAccessControlEntries();
      for (int i = 0; i < acEntries.length; i++) {
        Assert.isInstanceOf(JackrabbitAccessControlEntry.class, acEntries[i]);
        JackrabbitAccessControlEntry jrAce = (JackrabbitAccessControlEntry) acEntries[i];
        Principal principal = jrAce.getPrincipal();
        // TODO principal sid??? need to be able to lookup principal name
        Sid sid = null;
        if (principal instanceof Group) {
          sid = new GrantedAuthoritySid(principal.getName());
        } else {
          sid = new PrincipalSid(principal.getName());
        }
        logger.debug(String.format("principal class [%s]", principal.getClass().getName()));
        Privilege[] privileges = jrAce.getPrivileges();
        acl
            .insertAce(i, permissionConversionHelper.privilegesToPermission(jrSession, privileges), sid, jrAce
                .isAllow());
      }
      return acl;
    } catch (RepositoryException e) {
      throw jcrTemplate.convertJcrAccessException(e);
    }
  }

  public void setNodeIdStrategy(final NodeIdStrategy nodeIdStrategy) {
    Assert.notNull(nodeIdStrategy);
    this.nodeIdStrategy = nodeIdStrategy;
  }

  private class DefaultAclHelper implements IAclHelper {

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
        final boolean entriesInheriting, final Sid owner, final Permission allPermission) {
      MutableAcl acl = createAcl(oid);
      // link up parent if parent not null
      if (parentOid != null) {
        Acl newParent = readAclById(parentOid);
        acl.setParent(newParent);
      }
      acl.setOwner(owner);
      if (!entriesInheriting) {
        acl.setEntriesInheriting(false);
        setFullControl(oid, owner, allPermission);
      }
      return updateAcl(acl);
    }

    public void setFullControl(final ObjectIdentity oid, final Sid sid, final Permission permission) {
      addPermission(oid, sid, permission, true);
    }

  }

  public void addPermission(final ObjectIdentity oid, final Sid recipient, final Permission permission,
      final boolean granting) {
    aclHelper.addPermission(oid, recipient, permission, granting);
  }

  public MutableAcl createAndInitializeAcl(final ObjectIdentity oid, final ObjectIdentity parentOid,
      final boolean entriesInheriting, final Sid owner, final Permission allPermission) {
    return aclHelper.createAndInitializeAcl(oid, parentOid, entriesInheriting, owner, allPermission);
  }

  public void setFullControl(final ObjectIdentity oid, final Sid sid, final Permission permission) {
    aclHelper.setFullControl(oid, sid, permission);
  }

  public void setAclHelper(final IAclHelper aclHelper) {
    Assert.notNull(aclHelper);
    this.aclHelper = aclHelper;
  }

  public static interface IAclHelper {
    void addPermission(final ObjectIdentity oid, final Sid recipient, final Permission permission,
        final boolean granting);

    MutableAcl createAndInitializeAcl(final ObjectIdentity oid, final ObjectIdentity parentOid,
        final boolean entriesInheriting, final Sid owner, final Permission allPermission);

    void setFullControl(final ObjectIdentity oid, final Sid sid, final Permission permission);
  }

  public void setPermissionConversionHelper(final IPermissionConversionHelper permissionConversionHelper) {
    Assert.notNull(permissionConversionHelper);
    this.permissionConversionHelper = permissionConversionHelper;
  }

  public static interface IPermissionConversionHelper {
    Privilege[] permissionToPrivileges(final SessionImpl jrSession, final Permission first) throws RepositoryException;

    Permission privilegesToPermission(final SessionImpl jrSession, final Privilege[] first) throws RepositoryException;
  }

}
