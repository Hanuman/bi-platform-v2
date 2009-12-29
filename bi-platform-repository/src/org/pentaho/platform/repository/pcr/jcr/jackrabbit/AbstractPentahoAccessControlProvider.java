package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.jsr283.security.AccessControlList;
import org.apache.jackrabbit.api.jsr283.security.AccessControlManager;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicy;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.ItemImpl;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.authorization.AbstractAccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.AccessControlConstants;
import org.apache.jackrabbit.core.security.authorization.AccessControlEditor;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.UnmodifiableAccessControlList;
import org.apache.jackrabbit.core.security.principal.PrincipalImpl;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.name.PathFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy of {@code org.apache.jackrabbit.core.security.authorization.acl.ACLProvider} with the following changes:
 * 
 * <ul>
 * <li>
 * No readAllowed optimization. Superclass would pre-calculate a readAllowed flag by setting it to true if a single 
 * deny in ANY node along the path from leaf to root existed. Since this implementation works differently, that 
 * optimization is not possible. 
 * </li>
 * <li>
 * {@link #compilePermissions(Set)} method now delegates to 
 * {@link #doCompilePermissions(Set, SessionImpl, PentahoDefaultAccessControlEditor, boolean)} which is abstract. As a
 * consequence, this class is also abstract.
 * </li>
 * </ul>
 * 
 * @author mlowery
 */
public abstract class AbstractPentahoAccessControlProvider extends AbstractAccessControlProvider implements
    AccessControlConstants {

  /**
   * the default logger
   */
  private static final Logger log = LoggerFactory.getLogger(AbstractPentahoAccessControlProvider.class);

  /**
   * the system acl editor.
   */
  private PentahoDefaultAccessControlEditor systemEditor;

  /**
   * The node id of the root node
   */
  private NodeId rootNodeId;

  /**
   * Flag indicating whether or not this provider should be create the default
   * ACLs upon initialization.
   */
  private boolean initializedWithDefaults;

  //-------------------------------------------------< AccessControlUtils >---
  /**
   * @see AbstractAccessControlProvider#isAcItem(Path)
   */
  public boolean isAcItem(Path absPath) throws RepositoryException {
    Path.Element[] elems = absPath.getElements();
    for (int i = 0; i < elems.length; i++) {
      if (N_POLICY.equals(elems[i].getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test if the given node is itself a rep:ACL or a rep:ACE node.
   * @see AbstractAccessControlProvider#isAcItem(ItemImpl)
   */
  public boolean isAcItem(ItemImpl item) throws RepositoryException {
    NodeImpl n = ((item.isNode()) ? (NodeImpl) item : (NodeImpl) item.getParent());
    return n.isNodeType(NT_REP_ACL) || n.isNodeType(NT_REP_ACE);
  }

  //----------------------------------------------< AccessControlProvider >---
  /**
   * @see AccessControlProvider#init(Session, Map)
   */
  public void init(Session systemSession, Map configuration) throws RepositoryException {
    super.init(systemSession, configuration);

    // make sure the workspace of the given systemSession has a
    // minimal protection on the root node.
    NodeImpl root = (NodeImpl) session.getRootNode();
    rootNodeId = root.getNodeId();
    systemEditor = new PentahoDefaultAccessControlEditor(systemSession, this);
    initializedWithDefaults = !configuration.containsKey(PARAM_OMIT_DEFAULT_PERMISSIONS);
    if (initializedWithDefaults && !isAccessControlled(root)) {
      initRootACL(session, systemEditor);
    }
  }

  /**
   * @see AccessControlProvider#getEffectivePolicies(Path)
   * @param absPath
   */
  public AccessControlPolicy[] getEffectivePolicies(Path absPath) throws ItemNotFoundException, RepositoryException {
    checkInitialized();

    NodeImpl targetNode = (NodeImpl) session.getNode(session.getJCRPath(absPath));
    NodeImpl node = getNode(targetNode);
    List acls = new ArrayList();

    // collect all ACLs effective at node
    collectAcls(node, acls);
    // if no effective ACLs are present -> add a default, empty acl.
    if (acls.isEmpty()) {
      // no access control information can be retrieved for the specified
      // node, since neither the node nor any of its parents is access
      // controlled -> build a default policy.
      log.warn("No access controlled node present in item hierarchy starting from " + targetNode.getPath());
      acls.add(new UnmodifiableAccessControlList(Collections.EMPTY_LIST));
    }
    return (AccessControlList[]) acls.toArray(new AccessControlList[acls.size()]);
  }

  /**
   * @see AccessControlProvider#getEditor(Session)
   */
  public AccessControlEditor getEditor(Session session) {
    checkInitialized();
    return new PentahoDefaultAccessControlEditor(session, this);
  }

  /**
   * @see AccessControlProvider#compilePermissions(Set)
   */
  public CompiledPermissions compilePermissions(Set principals) throws RepositoryException {
    checkInitialized();
    return doCompilePermissions(principals, session, systemEditor, true);
  }

  protected abstract CompiledPermissions doCompilePermissions(Set principals, SessionImpl sessionImpl,
      PentahoDefaultAccessControlEditor pentahoEditor, boolean listenToEvents) throws RepositoryException;

  /**
   * @see AccessControlProvider#canAccessRoot(Set)
   */
  public boolean canAccessRoot(Set principals) throws RepositoryException {
    checkInitialized();
    if (isAdminOrSystem(principals)) {
      return true;
    } else {
      CompiledPermissions cp = doCompilePermissions(principals, session, systemEditor, false);
      return cp.grants(PathFactoryImpl.getInstance().getRootPath(), Permission.READ);
    }
  }

  //------------------------------------------------------------< private >---

  /**
   * Returns the given <code>targetNode</code> unless the node itself stores
   * access control information in which case it's nearest non-ac-parent is
   * searched and returned.
   *
   * @param targetNode The node for which AC information needs to be retrieved.
   * @return
   * @throws RepositoryException
   */
  public NodeImpl getNode(NodeImpl targetNode) throws RepositoryException {
    NodeImpl node;
    if (isAcItem(targetNode)) {
      if (targetNode.isNodeType(NT_REP_ACL)) {
        node = (NodeImpl) targetNode.getParent();
      } else {
        node = (NodeImpl) targetNode.getParent().getParent();
      }
    } else {
      node = targetNode;
    }
    return node;
  }

  /**
   * Recursively collects all ACLs that are effective on the specified node.
   *
   * @param node the Node to collect the ACLs for, which must NOT be part of the
   * structure defined by mix:AccessControllable.
   * @param acls List used to collect the effective acls.
   * @throws RepositoryException
   */
  private void collectAcls(NodeImpl node, List acls) throws RepositoryException {
    // if the given node is access-controlled, construct a new ACL and add
    // it to the list
    if (isAccessControlled(node)) {
      // build acl for the access controlled node
      NodeImpl aclNode = node.getNode(N_POLICY);
      AccessControlList acl = systemEditor.getACL(aclNode);
      acls.add(new UnmodifiableAccessControlList(acl));
    }
    // then, recursively look for access controlled parents up the hierarchy.
    if (!rootNodeId.equals(node.getId())) {
      NodeImpl parentNode = (NodeImpl) node.getParent();
      collectAcls(parentNode, acls);
    }
  }

  /**
   * Set-up minimal permissions for the workspace:
   *
   * <ul>
   * <li>adminstrators principal -> all privileges</li>
   * <li>everybody -> read privilege</li>
   * </ul>
   *
   * @param session to the workspace to set-up inital ACL to
   * @param editor for the specified session.
   * @throws RepositoryException If an error occurs.
   */
  protected void initRootACL(SessionImpl session, AccessControlEditor editor) throws RepositoryException {
    try {
      log.debug("Install initial ACL:...");
      String rootPath = session.getRootNode().getPath();
      AccessControlPolicy[] acls = editor.editAccessControlPolicies(rootPath);
      PentahoJackrabbitAccessControlList acl = (PentahoJackrabbitAccessControlList) acls[0];

      PrincipalManager pMgr = session.getPrincipalManager();
      AccessControlManager acMgr = session.getAccessControlManager();

      log.debug("... Privilege.ALL for administrators.");
      Principal administrators;
      String pName = SecurityConstants.ADMINISTRATORS_NAME;
      if (pMgr.hasPrincipal(pName)) {
        administrators = pMgr.getPrincipal(pName);
      } else {
        log.warn("Administrators principal group is missing.");
        administrators = new PrincipalImpl(pName);
      }
      Privilege[] privs = new Privilege[] { acMgr.privilegeFromName(Privilege.JCR_ALL) };
      acl.addAccessControlEntry(administrators, privs);

      Principal everyone = pMgr.getEveryone();
      log.debug("... Privilege.READ for everyone.");
      privs = new Privilege[] { acMgr.privilegeFromName(Privilege.JCR_READ) };
      acl.addAccessControlEntry(everyone, privs);

      acl.setEntriesInheriting(false);
      acl.setOwner(administrators);
      
      editor.setPolicy(rootPath, acl);
      session.save();

    } catch (RepositoryException e) {
      log.error("Failed to set-up minimal access control for root node of workspace "
          + session.getWorkspace().getName());
      session.getRootNode().refresh(false);
    }
  }

  /**
   * Test if the given node is access controlled. The node is access
   * controlled if it is of nodetype
   * {@link AccessControlConstants#NT_REP_ACCESS_CONTROLLABLE "rep:AccessControllable"}
   * and if it has a child node named
   * {@link AccessControlConstants#N_POLICY "rep:ACL"}.
   *
   * @param node
   * @return <code>true</code> if the node is access controlled;
   *         <code>false</code> otherwise.
   * @throws RepositoryException
   */
  static boolean isAccessControlled(NodeImpl node) throws RepositoryException {
    return node.isNodeType(NT_REP_ACCESS_CONTROLLABLE) && node.hasNode(N_POLICY);
  }

}
