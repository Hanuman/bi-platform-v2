package org.pentaho.platform.repository.pcr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.JcrConstants;
import org.pentaho.platform.api.repository.IPentahoContentDao;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

/**
 * CRUD operations against JCR. Note that there is no access control in this class (implicit or explicit).
 * 
 * @author mlowery
 */
public class JcrPentahoContentDao implements IPentahoContentDao, InitializingBean {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JcrPentahoContentDao.class);

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  private NodeIdStrategy nodeIdStrategy = new UuidNodeIdStrategy();

  // ~ Constructors ====================================================================================================

  public JcrPentahoContentDao(final JcrTemplate jcrTemplate) {
    super();
    this.jcrTemplate = jcrTemplate;
  }

  // ~ Methods =========================================================================================================

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile file) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.getName().contains(RepositoryFile.PATH_SEPARATOR));
    Assert.isTrue(file.isFolder());
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node parentFolderNode;
        if (parentFolder != null) {
          parentFolderNode = nodeIdStrategy.findNodeById(session, parentFolder.getId());
        } else {
          parentFolderNode = session.getRootNode();
        }
        Node folderNode = parentFolderNode.addNode(file.getName(), JcrConstants.NT_FOLDER);
        nodeIdStrategy.setId(folderNode, null);
        session.save();
        return JcrRepositoryFileUtils.fromNode(session, folderNode, nodeIdStrategy);
      }
    });
  }

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(file.getData());
    Assert.hasText(file.getEncoding());
    Assert.hasText(file.getMimeType());
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node parentFolderNode;
        if (parentFolder != null) {
          parentFolderNode = nodeIdStrategy.findNodeById(session, parentFolder.getId());
        } else {
          parentFolderNode = session.getRootNode();
        }
        Node fileNode = parentFolderNode.addNode(file.getName(), JcrConstants.NT_FILE);
        nodeIdStrategy.setId(fileNode, null);
        Node resourceNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
        resourceNode.setProperty(JcrConstants.JCR_ENCODING, file.getEncoding());
        resourceNode.setProperty(JcrConstants.JCR_MIMETYPE, file.getMimeType());
        resourceNode.setProperty(JcrConstants.JCR_DATA, new ByteArrayInputStream(file.getData()));
        // set created and last modified to same date when creating a new file
        resourceNode.setProperty(JcrConstants.JCR_LASTMODIFIED, fileNode.getProperty(JcrConstants.JCR_CREATED)
            .getDate());
        session.save();
        return JcrRepositoryFileUtils.fromNode(session, fileNode, nodeIdStrategy);
      }
    });
  }

  public RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file) {
    Assert.notNull(file);
    if (file.isFolder()) {
      return internalCreateFolder(parentFolder, file);
    } else {
      return internalCreateFile(parentFolder, file);
    }
  }

  public void removeFile(final RepositoryFile file) {
    Assert.notNull(file);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
        fileNode.remove();
        return null;
      }
    });
  }

  public boolean exists(final String absPath) {
    Assert.hasText(absPath);
    return (Boolean) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Item fileItem;
        try {
          fileItem = session.getItem(absPath);
        } catch (PathNotFoundException e) {
          return false;
        }
        Assert.isTrue(fileItem.isNode());
        return true;
      }
    });
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(jcrTemplate, "jcrTemplate required");
  }

  public void setNodeIdStrategy(final NodeIdStrategy nodeIdStrategy) {
    Assert.notNull(nodeIdStrategy);
    this.nodeIdStrategy = nodeIdStrategy;
  }

  public RepositoryFile getFile(final String absPath) {
    Assert.hasText(absPath);
    Assert.isTrue(absPath.startsWith(RepositoryFile.PATH_SEPARATOR));
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Item fileNode;
        try {
          fileNode = session.getItem(absPath);
          // items are nodes or properties; this must be a node
          Assert.isTrue(fileNode.isNode());
        } catch (PathNotFoundException e) {
          fileNode = null;
        }
        return fileNode != null ? JcrRepositoryFileUtils.fromNode(session, (Node) fileNode, nodeIdStrategy) : null;
      }
    });

  }

  /**
   * Allows for configurable node id getting, setting, and finding.
   * 
   * @author mlowery
   */
  public static interface NodeIdStrategy {

    /**
     * Returns the id of the given node.
     * @param node node from which to get id
     * @return id
     */
    Serializable getId(final Node node);

    /**
     * Sets the id of the given node.
     * @param node node for which to set id
     * @param id id to set
     */
    void setId(final Node node, final Serializable id);

    /**
     * Returns the node with the given id.
     * @param session session to use
     * @param id id of node to find
     * @return found node or <code>null</code>
     */
    Node findNodeById(final Session session, final Serializable id);
  }

  /**
   * {@link NodeIdStrategy} that uses node UUIDs.
   * 
   * @author mlowery
   */
  public class UuidNodeIdStrategy implements NodeIdStrategy {

    /**
     * Returns UUID of node. Obviously won't work on nodes that are not mix:referenceable.
     */
    public Serializable getId(final Node node) {
      try {
        return node.getUUID();
      } catch (RepositoryException e) {
        throw jcrTemplate.convertJcrAccessException(e);
      }
    }

    /**
     * Adds mix:referenceable to node if not already there. Node will get an actual id on save.
     */
    public void setId(final Node node, final Serializable ignored) {
      try {
        node.addMixin(JcrConstants.MIX_REFERENCEABLE);
      } catch (RepositoryException e) {
        throw jcrTemplate.convertJcrAccessException(e);
      }
    }

    /**
     * Uses session.getNodeByUUID.
     */
    public Node findNodeById(final Session session, final Serializable id) {
      try {
        return session.getNodeByUUID(id.toString());
      } catch (ItemNotFoundException e) {
        return null;
      } catch (RepositoryException e) {
        throw jcrTemplate.convertJcrAccessException(e);
      }
    }

  }

}
