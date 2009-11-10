package org.pentaho.platform.repository.pcr;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

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
        Node resourceNode = fileNode.getNode(PentahoJcrConstants.JCR_CONTENT);
        VersionHistory versionHistory = resourceNode.getVersionHistory();
        VersionIterator versionIterator = versionHistory.getAllVersions();
        int versionCount = 0;
        while (versionIterator.hasNext()) {
          Version version = versionIterator.nextVersion();
          versionCount++;
        }
        return versionCount;
      }
    });
  }

}
