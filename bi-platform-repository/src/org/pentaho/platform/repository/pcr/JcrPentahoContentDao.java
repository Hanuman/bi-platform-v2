package org.pentaho.platform.repository.pcr;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.pentaho.platform.api.repository.IPentahoContentDao;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
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

  private Map<String, Transformer> transformers;

  // ~ Constructors ====================================================================================================

  public JcrPentahoContentDao(final JcrTemplate jcrTemplate, final Map<String, Transformer> transformers) {
    super();
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
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
      final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);
    Assert.hasText(file.getMimeType());
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node fileNode = JcrRepositoryFileUtils.toFileNode(session, nodeIdStrategy, parentFolder, file, content,
            transformers.get(file.getMimeType()));
        session.save();
        RepositoryFile newFile = JcrRepositoryFileUtils.fromNode(session, nodeIdStrategy, fileNode);
        JcrRepositoryFileUtils.checkinIfNecessary(session, nodeIdStrategy, newFile);
        return newFile;
      }
    });
  }
  
  private void internalUpdateFile(final RepositoryFile file,
      final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);
    Assert.hasText(file.getMimeType());

    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.checkoutIfNecessary(session, nodeIdStrategy, file);
        JcrRepositoryFileUtils.updateFileNode(session, nodeIdStrategy, file, content,
            transformers.get(file.getMimeType()));
        session.save();
        JcrRepositoryFileUtils.checkinIfNecessary(session, nodeIdStrategy, file);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.isTrue(transformers.containsKey(file.getMimeType()), String.format(
        "no transformer for this MIME type [%s] exists", file.getMimeType()));
    Assert.isTrue(transformers.get(file.getMimeType()).supports(content.getClass()), String.format(
        "transformer for MIME type [%s] does not consume instances of type [%s]", file.getMimeType(), content
            .getClass().getName()));
    return internalCreateFile(parentFolder, file, content);
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
  public <T extends IRepositoryFileContent> T getContent(final RepositoryFile file, final Class<T> contentClass) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    Assert.isTrue(transformers.containsKey(file.getMimeType()), String.format(
        "no transformer for this MIME type [%s] exists", file.getMimeType()));
    Assert.isTrue(transformers.get(file.getMimeType()).supports(contentClass), String.format(
        "transformer for MIME type [%s] does not generate instances of type [%s]", file.getMimeType(), contentClass
            .getName()));
    return (T) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        return JcrRepositoryFileUtils.getContent(session, nodeIdStrategy, file, transformers.get(file.getMimeType()));
      }
    });

  }

  public List<RepositoryFile> getChildren(final RepositoryFile folder) {
    Assert.notNull(folder);
    Assert.notNull(folder.getId());
    Assert.notNull(folder.isFolder());
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        return JcrRepositoryFileUtils.getChildren(session, nodeIdStrategy, folder);
      }
    });
  }


  public void updateFile(RepositoryFile file, IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.isTrue(transformers.containsKey(file.getMimeType()), String.format(
        "no transformer for this MIME type [%s] exists", file.getMimeType()));
    Assert.isTrue(transformers.get(file.getMimeType()).supports(content.getClass()), String.format(
        "transformer for MIME type [%s] does not consume instances of type [%s]", file.getMimeType(), content
            .getClass().getName()));
    internalUpdateFile(file, content);
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
        node.addMixin(PentahoJcrConstants.MIX_REFERENCEABLE);
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

  public static interface Transformer<T extends IRepositoryFileContent> {
    <S extends IRepositoryFileContent> boolean supports(Class<S> clazz);

    T fromContentNode(final Session session, final NodeIdStrategy nodeIdStrategy, final Node node)
        throws RepositoryException, IOException;

    void createContentNode(final Session session, final NodeIdStrategy nodeIdStrategy, final T content,
        final Node resourceNode) throws RepositoryException, IOException;
    
    void updateContentNode(final Session session, final NodeIdStrategy nodeIdStrategy, final T content,
        final Node resourceNode) throws RepositoryException, IOException;

  }

}
