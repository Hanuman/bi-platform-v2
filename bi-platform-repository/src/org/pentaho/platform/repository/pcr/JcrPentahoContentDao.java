package org.pentaho.platform.repository.pcr;

import java.io.IOException;
import java.io.InputStream;
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
    Assert.isTrue(!file.getName().contains(RepositoryFile.SEPARATOR));
    Assert.isTrue(file.isFolder());
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node folderNode = JcrRepositoryFileUtils.toFolderNode(session, nodeIdStrategy, parentFolder, file);
        session.save();
        return JcrRepositoryFileUtils.fromNode(session, nodeIdStrategy, folderNode);
      }
    });
  }

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final InputStream data) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(data);
    Assert.hasText(file.getEncoding());
    Assert.hasText(file.getMimeType());
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node fileNode = JcrRepositoryFileUtils.toFileNode(session, nodeIdStrategy, parentFolder, file, data);
        session.save();
        return JcrRepositoryFileUtils.fromNode(session, nodeIdStrategy, fileNode);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file, final InputStream data) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalCreateFile(parentFolder, file, data);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file) {
    Assert.notNull(file);
    Assert.isTrue(file.isFolder());
    return internalCreateFolder(parentFolder, file);
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

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(jcrTemplate, "jcrTemplate required");
  }

  public void setNodeIdStrategy(final NodeIdStrategy nodeIdStrategy) {
    Assert.notNull(nodeIdStrategy);
    this.nodeIdStrategy = nodeIdStrategy;
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String absPath) {
    Assert.hasText(absPath);
    Assert.isTrue(absPath.startsWith(RepositoryFile.SEPARATOR));
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
        return fileNode != null ? JcrRepositoryFileUtils.fromNode(session, nodeIdStrategy, (Node) fileNode) : null;
      }
    });

  }

  /**
   * {@inheritDoc}
   */
  public InputStream getStream(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());

    return (InputStream) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        return JcrRepositoryFileUtils.getStream(session, nodeIdStrategy, file);
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
     * @return found node or {@code null}
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
     * Uses {@code session.getNodeByUUID}.
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
