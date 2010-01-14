package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
import org.pentaho.commons.security.jackrabbit.IPentahoJackrabbitAccessControlList;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository.pcr.IRepositoryFileAclDao;
import org.pentaho.platform.repository.pcr.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository.pcr.jcr.PentahoJcrConstants;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

/**
 * Jackrabbit-based implementation of {@link IRepositoryFileAclDao}.
 * 
 * <p>
 * All mutating public methods require checkout and checkin calls since the act of simply calling 
 * {@code AccessControlManager.getApplicablePolicies()} (as is done in 
 * {@link #toAcl(SessionImpl, Serializable, boolean)}) will query that the node is allowed to have the "access 
 * controlled" mixin type added. If the node is checked in, this query will return false. See Jackrabbit's 
 * {@code ItemValidator.hasCondition()}.
 * </p>
 * 
 * @author mlowery
 */
public class JackrabbitRepositoryFileAclDao implements IRepositoryFileAclDao {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JackrabbitRepositoryFileAclDao.class);

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  private IPermissionConversionHelper permissionConversionHelper = new DefaultPermissionConversionHelper();

  // ~ Constructors ====================================================================================================

  public JackrabbitRepositoryFileAclDao(final JcrTemplate jcrTemplate) {
    super();
    this.jcrTemplate = jcrTemplate;
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   * 
   * This is a hack since this code must move lock step with any changes in access control on the server.
   */
  public synchronized List<RepositoryFileAcl.Ace> getEffectiveAces(final Serializable id) {
    Assert.notNull(id);
    RepositoryFileAcl acl = readAclById(id);
    while (acl.isEntriesInheriting()) {
      acl = readAclById(acl.getParentId());
    }
    return acl.getAces();
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions) {
    return (Boolean) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Assert.isInstanceOf(SessionImpl.class, session);
        SessionImpl jrSession = (SessionImpl) session;

        Privilege[] privs = permissionConversionHelper.pentahoPermissionsToJackrabbitPrivileges(jrSession, permissions);
        try {
          return jrSession.getAccessControlManager().hasPrivileges(absPath, privs);
        } catch (PathNotFoundException e) {
          // never throw an exception if the path does not exist; just return false
          return false;
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * In Jackrabbit 1.6 (and maybe 2.0), objects already have an AccessControlPolicy of type AccessControlList. It is 
   * empty by default with implicit read access for everyone.
   * </p>
   */
  private RepositoryFileAcl createAcl(final Serializable id) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node node = session.getNodeByUUID(id.toString());
        if (node == null) {
          throw new RepositoryException(String.format("node with id [%s] not found", id));
        }

        JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, node);

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
          jrPolicy.setOwner(jrSession.getPrincipalManager().getPrincipal(getUsername()));
          acMgr.setPolicy(absPath, acPolicy);
        }

        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, node,
            "[system] created ACL");

        return toAcl(jrSession, pentahoJcrConstants, id);
      }
    });
  }

  private AccessControlPolicy getAccessControlPolicy(final AccessControlManager acMgr, final String absPath)
      throws RepositoryException {
    AccessControlPolicy[] policies = acMgr.getPolicies(absPath);
    Assert.notEmpty(policies, "most likely due to calling readAclById before calling createAcl");
    return policies[0];
  }

  private String getUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }

  private RepositoryFileAcl toAcl(final SessionImpl jrSession, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable id) throws RepositoryException {

    Node node = jrSession.getNodeByUUID(id.toString());
    if (node == null) {
      throw new RepositoryException(String.format("node with id [%s] not found", id));
    }
    String absPath = node.getPath();
    AccessControlManager acMgr = jrSession.getAccessControlManager();
    AccessControlPolicy acPolicy = getAccessControlPolicy(acMgr, absPath);

    Serializable parentId = null;

    if (!node.getParent().isSame(jrSession.getRootNode())) {
      parentId = node.getParent().getUUID();
    }

    Assert.isInstanceOf(IPentahoJackrabbitAccessControlList.class, acPolicy);

    IPentahoJackrabbitAccessControlList acList = (IPentahoJackrabbitAccessControlList) acPolicy;

    RepositoryFileSid owner = null;
    Principal ownerPrincipal = acList.getOwner();
    if (ownerPrincipal instanceof Group) {
      owner = new RepositoryFileSid(ownerPrincipal.getName(), RepositoryFileSid.Type.ROLE);
    } else {
      owner = new RepositoryFileSid(ownerPrincipal.getName());
    }

    RepositoryFileAcl.Builder aclBuilder = new RepositoryFileAcl.Builder(id, parentId, owner);
    aclBuilder.entriesInheriting(acList.isEntriesInheriting());
    AccessControlEntry[] acEntries = acList.getAccessControlEntries();
    for (int i = 0; i < acEntries.length; i++) {
      Assert.isInstanceOf(JackrabbitAccessControlEntry.class, acEntries[i]);
      JackrabbitAccessControlEntry jrAce = (JackrabbitAccessControlEntry) acEntries[i];
      Principal principal = jrAce.getPrincipal();
      RepositoryFileSid sid = null;
      if (principal instanceof Group) {
        sid = new RepositoryFileSid(principal.getName(), RepositoryFileSid.Type.ROLE);
      } else {
        sid = new RepositoryFileSid(principal.getName());
      }
      logger.debug(String.format("principal class [%s]", principal.getClass().getName()));
      Privilege[] privileges = jrAce.getPrivileges();
      aclBuilder.ace(sid, permissionConversionHelper.jackrabbitPrivilegesToPentahoPermissions(jrSession, privileges));

    }
    return aclBuilder.build();

  }

  public void setPermissionConversionHelper(final IPermissionConversionHelper permissionConversionHelper) {
    Assert.notNull(permissionConversionHelper);
    this.permissionConversionHelper = permissionConversionHelper;
  }

  /**
   * Converts between {@code RepositoryFilePermission} and {@code Privilege} instances.
   */
  public static interface IPermissionConversionHelper {
    Privilege[] pentahoPermissionsToJackrabbitPrivileges(final SessionImpl jrSession,
        final EnumSet<RepositoryFilePermission> permission) throws RepositoryException;

    EnumSet<RepositoryFilePermission> jackrabbitPrivilegesToPentahoPermissions(final SessionImpl jrSession,
        final Privilege[] privileges) throws RepositoryException;
  }

  public void addPermission(final Serializable id, final RepositoryFileSid recipient,
      final EnumSet<RepositoryFilePermission> permission) {
    Assert.notNull(id);
    Assert.notNull(recipient);
    Assert.notNull(permission);
    RepositoryFileAcl acl = readAclById(id);
    Assert.notNull(acl);
    // TODO mlowery find an ACE with the recipient and update that rather than adding a new ACE
    RepositoryFileAcl updatedAcl = new RepositoryFileAcl.Builder(acl).ace(recipient, permission).build();
    updateAcl(updatedAcl);
    logger.debug("added ace: id=" + id + ", sid=" + recipient + ", permission=" + permission);
  }

  public RepositoryFileAcl createAcl(final Serializable id, final boolean entriesInheriting,
      final RepositoryFileSid owner, final RepositoryFilePermission allPermission) {
    RepositoryFileAcl acl = createAcl(id);
    RepositoryFileAcl.Builder aclBuilder = new RepositoryFileAcl.Builder(acl);
    aclBuilder.owner(owner);
    aclBuilder.entriesInheriting(entriesInheriting);
    if (!entriesInheriting) {
      setFullControl(id, owner, allPermission);
    }
    return updateAcl(aclBuilder.build());
  }

  public RepositoryFileAcl readAclById(final Serializable id) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;

        return toAcl(jrSession, pentahoJcrConstants, id);
      }
    });
  }

  public void setFullControl(Serializable id, RepositoryFileSid sid, RepositoryFilePermission permission) {
    addPermission(id, sid, EnumSet.of(permission));
  }

  public RepositoryFileAcl updateAcl(final RepositoryFileAcl acl) {
    return (RepositoryFileAcl) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Assert.isTrue(session instanceof SessionImpl);
        SessionImpl jrSession = (SessionImpl) session;
        Node node = session.getNodeByUUID(acl.getId().toString());
        if (node == null) {
          throw new RepositoryException(String.format("node with id [%s] not found", acl.getId()));
        }

        JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, node);

        String absPath = node.getPath();
        AccessControlManager acMgr = jrSession.getAccessControlManager();
        AccessControlPolicy acPolicy = getAccessControlPolicy(acMgr, absPath);
        Assert.isInstanceOf(JackrabbitAccessControlList.class, acPolicy);
        // cast to JackrabbitAccessControlList to get addEntry call (for isAllow parameter)
        IPentahoJackrabbitAccessControlList acList = (IPentahoJackrabbitAccessControlList) acPolicy;

        acList.setEntriesInheriting(acl.isEntriesInheriting());

        acList.setOwner(jrSession.getPrincipalManager().getPrincipal(acl.getOwner().getName()));

        // clear all entries
        AccessControlEntry[] acEntries = acList.getAccessControlEntries();
        for (int i = 0; i < acEntries.length; i++) {
          acList.removeAccessControlEntry(acEntries[i]);
        }
        // add entries
        for (RepositoryFileAcl.Ace ace : acl.getAces()) {
          Principal principal = null;

          principal = jrSession.getPrincipalManager().getPrincipal(ace.getSid().getName());
          acList.addEntry(principal, permissionConversionHelper.pentahoPermissionsToJackrabbitPrivileges(jrSession, ace
              .getPermissions()), true);
        }
        acMgr.setPolicy(absPath, acList);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, node,
            "[system] updated ACL");
        return readAclById(acl.getId());
      }
    });

  }

}
