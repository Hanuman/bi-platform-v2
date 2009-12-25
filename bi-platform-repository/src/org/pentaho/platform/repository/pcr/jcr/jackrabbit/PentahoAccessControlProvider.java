package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.version.VersionHistory;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.jsr283.security.AccessControlEntry;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.PropertyImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.observation.SynchronousEventListener;
import org.apache.jackrabbit.core.security.authorization.AbstractCompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.AccessControlConstants;
import org.apache.jackrabbit.core.security.authorization.AccessControlEntryImpl;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * <p>
 * To enable this class, edit {@code workspace.xml} and nest the following elements inside the {@code Workspace} element:
 * <pre>
 * {@code
 * <WorkspaceSecurity>
 *   <AccessControlProvider class="org.pentaho.platform.repository.pcr.jackrabbit.PentahoDefaultAccessControlProvider" />
 * </WorkspaceSecurity>
 * }
 * </pre>
 * </p>
 * 
 * @author mlowery
 */
public class PentahoAccessControlProvider extends AbstractPentahoAccessControlProvider {

  private static final Logger log = LoggerFactory.getLogger(PentahoAccessControlProvider.class);

  @Override
  protected CompiledPermissions doCompilePermissions(Set principals, SessionImpl systemSession,
      PentahoDefaultAccessControlEditor pentahoEditor, boolean listenToEvents) throws RepositoryException {
    if (isAdminOrSystem(principals)) {
      return getAdminPermissions();
    } else if (isReadOnly(principals)) {
      return getReadOnlyPermissions();
    } else {
      return new AclPermissions(principals, systemSession, pentahoEditor, listenToEvents);
    }

  }

  //------------------------------------------------< CompiledPermissions >---
  /**
   *
   */
  private class AclPermissions extends AbstractCompiledPermissions implements SynchronousEventListener {

    private final List<String> principalNames;

    private PentahoDefaultAccessControlEditor pentahoEditor;

    private SessionImpl systemSession;

    private AclPermissions(Set principals, SessionImpl systemSession, PentahoDefaultAccessControlEditor pentahoEditor,
        boolean listenToEvents) throws RepositoryException {
      this.pentahoEditor = pentahoEditor;
      this.systemSession = systemSession;
      principalNames = new ArrayList<String>(principals.size());
      for (Iterator it = principals.iterator(); it.hasNext();) {
        principalNames.add(((Principal) it.next()).getName());
      }

      if (listenToEvents) {
        /*
         Make sure this AclPermission recalculates the permissions if
         any ACL concerning it is modified. interesting events are:
         - new ACE-entry for any of the principals (NODE_ADDED)
         - changing ACE-entry for any of the principals (PROPERTY_CHANGED)
           > new permissions granted/denied
           >
         - removed ACE-entry for any of the principals (NODE_REMOVED)
        */
        int events = Event.PROPERTY_CHANGED | Event.NODE_ADDED | Event.NODE_REMOVED;
        String[] ntNames = new String[] { systemSession.getJCRName(AccessControlConstants.NT_REP_ACE),
            systemSession.getJCRName(AccessControlConstants.NT_REP_ACL) };
        systemSession.getWorkspace().getObservationManager().addEventListener(this, events,
            systemSession.getRootNode().getPath(), true, null, ntNames, true);
      }
    }

    //------------------------------------< AbstractCompiledPermissions >---

    /**
     * <p>
     * This algorithm is content-aware. In other words, it has knowledge of the types of nodes going into the 
     * repository. It understands nodes of type nt:folder, nt:file, and nt:linkedFile. It enforces access control by 
     * finding the nearest enclosing persisted nt:folder and uses the ACL for that node as the starting point. Why is it 
     * just a starting point and not the end? Because the nearest enclosing persisted node might not have any ACEs of 
     * its own, forcing us to traverse the tree heading for the root until we either hit the root or hit an nt:folder
     * that has a non-empty ACL. This is the ACL (and only this ACL) that is consulted to build the {@code Result}.
     * </p>
     * 
     * <p>Special behavior when dealing with version storage</p>
     * <p>
     * Sometimes the {@code absPath} starts with {@code /jcr:system/jcr:versionStorage}. In this case
     * a caller is attempting to do something involving versioning (e.g. applying a label to a version). Nodes in this
     * are not part of the nt:folder structure discussed above. In other words, we will never find an enclosing 
     * persisted folder. We will always hit the root--which has its own ACL which we do not want to use as it allows 
     * Permission.READ for all. So what do we do?
     * </p> 
     * 
     * <p>
     * We first see if
     * a persistent version history node is part of the {@code absPath}. If the version history is persistent, then we
     * find the node with which the version history is associated and use its ACL to build the result. Sometimes the
     * version history node is not yet persistent. This happens because the user hasn't committed the transaction in 
     * which a {@code checkin} takes place. In this case, we give READ and VERSION_MNGMT in the result. Why? Because the user
     * has already successfully checked in--and the Permission.VERSION_MNGMT was already checked on that node--why 
     * should we stop them now? What about the justification for providing read access? Well, if the caller is asking
     * to read a node that has not yet been persisted, then that node was created in the that user's session to begin
     * with.
     * </p>
     */
    protected Result buildResult(Path absPath) throws RepositoryException {
      String jcrPath = resolver.getJCRPath(absPath);
      log.debug("building result for jcrPath=" + jcrPath);

      NodeImpl nearestPersistedNode = findNearestPersistedNode(absPath);

      NodeImpl folderOrFileOrLinkedFileOrRootNode = null;

      if (jcrPath.startsWith("/jcr:system/jcr:versionStorage")) {
        VersionHistory versionHistory = findVersionHistoryNode(nearestPersistedNode);
        if (versionHistory == null) {
          int allows = Permission.READ | Permission.VERSION_MNGMT;
          return new Result(allows, 0, allows, 0);
        } else {
          folderOrFileOrLinkedFileOrRootNode = (NodeImpl) systemSession.getNodeByUUID(versionHistory
              .getVersionableUUID());
        }
      } else {
        folderOrFileOrLinkedFileOrRootNode = findFolderOrFileOrLinkedFileOrRootNode(nearestPersistedNode);
      }

      if (folderOrFileOrLinkedFileOrRootNode == null) {
        // should never get here
        throw new ItemNotFoundException("Item out of hierarchy.");
      }

      NodeImpl nodeWithAclToUse = folderOrFileOrLinkedFileOrRootNode;

      // now we have a folder, file, or linkedFile node whose ACL will serve as the starting ACL
      // simplifying assumption that an empty aces list means to inherit from parent; 
      // TODO fix above assumption
      NodeImpl aclNode;
      aclNode = nodeWithAclToUse.getNode(AccessControlConstants.N_POLICY);
      List<AccessControlEntry> aces = Arrays.asList(pentahoEditor.getACL(aclNode).getAccessControlEntries());
      while (aces.isEmpty() && !isRootNode(nodeWithAclToUse)) {
        nodeWithAclToUse = (NodeImpl) nodeWithAclToUse.getParent();
        aclNode = nodeWithAclToUse.getNode(AccessControlConstants.N_POLICY);
        aces = Arrays.asList(pentahoEditor.getACL(aclNode).getAccessControlEntries());
      }

      // if hit root and it has no ACEs, that is a problem as AbstractPentahoAccessControlProvider.initRootACL should
      // have written some ACEs
      if (aces.isEmpty() && isRootNode(nodeWithAclToUse)) {
        throw new RepositoryException("root node has no ACEs");
      }

      log.debug("building result using ACEs: " + aces.toString());

      int allows = Permission.NONE;

      // simplifying assumption is that we don't handle deny ACEs
      // TODO fix above assumption
      for (AccessControlEntry ace : aces) {
        Assert.isInstanceOf(AccessControlEntryImpl.class, ace);
        AccessControlEntryImpl jrAce = (AccessControlEntryImpl) ace;
        // if either there is an exact match on principal name or the ace uses the "everyone" principal
        if (principalNames.contains(ace.getPrincipal().getName())
            || EveryonePrincipal.getInstance().getName().equals(ace.getPrincipal().getName())) {
          if (jrAce.isAllow()) {
            allows |= jrAce.getPrivilegeBits();
          }
        }
      }

      // third arg is for when callers call Result.getPrivileges() as is done in AbstractCompiledPermissions.getPrivileges()
      return new Result(allows, 0, allows, 0);
    }

    private boolean isFolderOrFileOrLinkedFileNode(final Node node) throws RepositoryException {
      String nodeTypeName = node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString();
      return JcrConstants.NT_FOLDER.equals(nodeTypeName) || JcrConstants.NT_FILE.equals(nodeTypeName)
          || JcrConstants.NT_LINKEDFILE.equals(nodeTypeName);
    }

    private NodeImpl findNearestPersistedNode(final Path absPath) throws RepositoryException {
      NodeImpl nearestPersistedNode = null;
      String jcrPath = resolver.getJCRPath(absPath);

      // set node to node at jcrPath or if not yet persisted, set node to nearest persisted node
      if (systemSession.nodeExists(jcrPath)) {
        nearestPersistedNode = (NodeImpl) systemSession.getNode(jcrPath);
      } else {
        // path points non-existing node or property; find the nearest persisted node
        String parentPath = Text.getRelativeParent(jcrPath, 1);
        while (parentPath.length() > 0) {
          if (systemSession.nodeExists(parentPath)) {
            nearestPersistedNode = (NodeImpl) systemSession.getNode(parentPath);
            break;
          }
          parentPath = Text.getRelativeParent(parentPath, 1);
        }
      }
      return nearestPersistedNode;
    }

    private boolean isRootNode(final NodeImpl node) throws RepositoryException {
      return node.getId().equals(((NodeImpl) systemSession.getRootNode()).getNodeId());
    }

    private boolean isVersionHistory(final NodeImpl node) throws RepositoryException {
      return node.isNodeType(JcrConstants.NT_VERSIONHISTORY);
    }

    private VersionHistory findVersionHistoryNode(final NodeImpl node) throws RepositoryException {
      // also, if incoming path involves version history, find the versionHistory node then find the file or linked
      // file or folder that it is associated with and use that node's ACL
      NodeImpl currentNode = node;
      while (!isVersionHistory(currentNode) && !isRootNode(currentNode)) {
        currentNode = (NodeImpl) currentNode.getParent();
      }
      if (isRootNode(currentNode)) {
        return null;
      } else {
        return (VersionHistory) currentNode;
      }
    }

    private NodeImpl findFolderOrFileOrLinkedFileOrRootNode(final NodeImpl node) throws RepositoryException {
      // now we have a node; it may not be a folder, file, or linkedFile node so find the nearest enclosing folder,
      // file, or linkedFile node; that will be the ACL that we start with; also, stop if we hit the root node;
      NodeImpl currentNode = node;
      while (!isVersionHistory(currentNode) && !isFolderOrFileOrLinkedFileNode(currentNode) && !isRootNode(currentNode)) {
        currentNode = (NodeImpl) currentNode.getParent();
      }

      return currentNode;
    }

    //--------------------------------------------< CompiledPermissions >---
    /**
     * @see CompiledPermissions#close()
     */
    public void close() {
      try {
        systemSession.getWorkspace().getObservationManager().removeEventListener(this);
      } catch (RepositoryException e) {
        log.debug("Unable to unregister listener: ", e.getMessage());
      }
      super.close();
    }

    /**
     * Override to print some debug statements.
     */
    @Override
    public boolean grants(Path absPath, int permissions) throws RepositoryException {
      String jcrPath = resolver.getJCRPath(absPath);
      if (log.isDebugEnabled()) {
        log.debug("processing access control query for jcrPath=" + jcrPath + " and perm bits=" + permissions
            + " for principals=" + principalNames);
      }
      return super.grants(absPath, permissions);
    }

    //--------------------------------------------------< EventListener >---
    /**
     * @see EventListener#onEvent(EventIterator)
     */
    public synchronized void onEvent(EventIterator events) {
      // only invalidate cache if any of the events affects the
      // nodes defining permissions for principals compiled here.
      boolean clearCache = false;
      while (events.hasNext() && !clearCache) {
        try {
          Event ev = events.nextEvent();
          String path = ev.getPath();
          switch (ev.getType()) {
            case Event.NODE_ADDED:
              // test if the new node is an ACE node that affects
              // the permission of any of the principals listed in
              // principalNames.
              NodeImpl n = (NodeImpl) systemSession.getNode(path);
              if (n.isNodeType(AccessControlConstants.NT_REP_ACE)
                  && principalNames.contains(n.getProperty(AccessControlConstants.P_PRINCIPAL_NAME).getString())) {
                clearCache = true;
              }
              break;
            case Event.PROPERTY_REMOVED:
            case Event.NODE_REMOVED:
              // can't find out if the removed ACL/ACE node was
              // relevant for the principals
              clearCache = true;
              break;
            case Event.PROPERTY_ADDED:
            case Event.PROPERTY_CHANGED:
              // test if the added/changed prop belongs to an ACe
              // node and affects the permission of any of the
              // principals listed in principalNames.
              PropertyImpl p = (PropertyImpl) systemSession.getProperty(path);
              NodeImpl parent = (NodeImpl) p.getParent();
              if (parent.isNodeType(AccessControlConstants.NT_REP_ACE)) {
                String principalName = null;
                if (AccessControlConstants.P_PRIVILEGES.equals(p.getQName())) {
                  // test if principal-name sibling-prop matches
                  principalName = parent.getProperty(AccessControlConstants.P_PRINCIPAL_NAME).getString();
                } else if (AccessControlConstants.P_PRINCIPAL_NAME.equals(p.getQName())) {
                  // a new ace or an ace change its principal-name.
                  principalName = p.getString();
                }
                if (principalName != null && principalNames.contains(principalName)) {
                  clearCache = true;
                }
              }
              break;
            default:
              // illegal event-type: should never occur. ignore
          }
        } catch (RepositoryException e) {
          // should not get here
          log.warn("Internal error: ", e.getMessage());
        }
      }
      if (clearCache) {
        clearCache();
      }
    }
  }

}
