package org.pentaho.platform.repository.pcr;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicy;
import org.apache.jackrabbit.api.jsr283.security.AccessControlPolicyIterator;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.apache.jackrabbit.core.SessionImpl;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

public class SimpleJcrTestUtils {

  public static void deleteItem(final JcrTemplate jcrTemplate, final String absPath) {
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Item item;
        try {
          item = session.getItem(absPath);
        } catch (PathNotFoundException e) {
          return null;
        }
        item.remove();
        session.save();
        return null;
      }
    });
  }

  public static String addNode(final JcrTemplate jcrTemplate, final String parentAbsPath, final String name,
      final String primaryNodeTypeName) {
    return (String) jcrTemplate.execute(new JcrCallback() {
      public String doInJcr(final Session session) throws RepositoryException {
        Node newNode;
        try {
          Item item = session.getItem(parentAbsPath);
          Assert.isTrue(item.isNode());
          Node parentNode = (Node) item;
          newNode = parentNode.addNode(name, primaryNodeTypeName);
          newNode.addMixin(PentahoJcrConstants.MIX_REFERENCEABLE);
        } catch (PathNotFoundException e) {
          return null;
        }
        session.save();
        return newNode.getUUID();
      }
    });
  }

  public static Item getItem(final JcrTemplate jcrTemplate, final String absPath) {
    return (Item) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Item item;
        try {
          item = session.getItem(absPath);
        } catch (PathNotFoundException e) {
          return null;
        }
        return item;
      }
    });
  }

  public static String getNodeId(final JcrTemplate jcrTemplate, final String absPath) {
    return (String) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Item item;
        try {
          item = session.getItem(absPath);
        } catch (PathNotFoundException e) {
          return null;
        }
        Assert.isTrue(item.isNode());
        return ((Node) item).getUUID();
      }
    });
  }

  public static int getVersionCount(final JcrTemplate jcrTemplate, final String absPath) {
    return (Integer) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Node fileNode = (Node) session.getItem(absPath);
        VersionHistory versionHistory = fileNode.getVersionHistory();
        VersionIterator versionIterator = versionHistory.getAllVersions();
        int versionCount = 0;
        while (versionIterator.hasNext()) {
          versionIterator.nextVersion();
          versionCount++;
        }
        return versionCount;
      }
    });
  }

  public static void printAccess(final JcrTemplate jcrTemplate, final String absPath) {
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {

        SessionImpl jrSession = (SessionImpl) session;
        AccessControlPolicy[] epols = jrSession.getAccessControlManager().getEffectivePolicies(absPath);
        AccessControlPolicy[] pols = jrSession.getAccessControlManager().getPolicies(absPath);
        AccessControlPolicyIterator apols = jrSession.getAccessControlManager().getApplicablePolicies(absPath);
        return null;
      }
    });
  }

  public static boolean hasPrivileges(final JcrTemplate jcrTemplate, final String absPath, final String... privNames) {
    return (Boolean) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Assert.notEmpty(privNames);
        SessionImpl jrSession = (SessionImpl) session;
        Privilege[] privs = new Privilege[privNames.length];
        for (int i = 0; i < privs.length; i++) {
          privs[i] = jrSession.getAccessControlManager().privilegeFromName(privNames[i]);
        }
        return jrSession.getAccessControlManager().hasPrivileges(absPath, privs);
      }
    });

  }

}
