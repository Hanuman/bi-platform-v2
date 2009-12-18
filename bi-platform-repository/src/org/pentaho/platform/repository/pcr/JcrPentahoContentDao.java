package org.pentaho.platform.repository.pcr;

import java.io.IOException;
import java.util.List;

import javax.jcr.Item;
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

  private NodeIdStrategy nodeIdStrategy;

  private List<Transformer<IRepositoryFileContent>> transformers;

  // ~ Constructors ====================================================================================================

  public JcrPentahoContentDao(final JcrTemplate jcrTemplate,
      final List<Transformer<IRepositoryFileContent>> transformers) {
    super();
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
    this.nodeIdStrategy = new UuidNodeIdStrategy(jcrTemplate);
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
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, nodeIdStrategy, parentFolder);
        Node folderNode = JcrRepositoryFileUtils.createFolderNode(session, nodeIdStrategy, parentFolder, file);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, nodeIdStrategy, parentFolder);
        return JcrRepositoryFileUtils.fromFileNode(session, nodeIdStrategy, folderNode);
      }
    });
  }

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, nodeIdStrategy, parentFolder);
        Node fileNode = JcrRepositoryFileUtils.createFileNode(session, nodeIdStrategy, parentFolder, file, content,
            findTransformer(content.getContentType()));
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, nodeIdStrategy, parentFolder);
        return JcrRepositoryFileUtils.fromFileNode(session, nodeIdStrategy, fileNode);
      }
    });
  }

  private void internalUpdateFile(final RepositoryFile file, final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);
    Assert.hasText(file.getContentType());

    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, nodeIdStrategy, file);
        JcrRepositoryFileUtils.updateFileNode(session, nodeIdStrategy, file, content, findTransformer(file
            .getContentType()));
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, nodeIdStrategy, file);
        return null;
      }
    });
  }

  private Transformer<IRepositoryFileContent> findTransformer(final String contentType) {
    for (Transformer<IRepositoryFileContent> transformer : transformers) {
      if (transformer.supports(contentType)) {
        return transformer;
      }
    }
    throw new IllegalArgumentException(String.format("no transformer for this resource type [%s] exists", contentType));
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
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
        return fileNode != null ? JcrRepositoryFileUtils.fromFileNode(session, nodeIdStrategy, (Node) fileNode) : null;
      }
    });

  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileContent> T getContent(final RepositoryFile file, final Class<T> contentClass) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    return (T) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        return JcrRepositoryFileUtils.getContent(session, nodeIdStrategy, file, findTransformer(file.getContentType()));
      }
    });

  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
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

  /**
   * {@inheritDoc}
   */
  public void updateFile(final RepositoryFile file, final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    internalUpdateFile(file, content);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFile(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.notNull(file.getParentId());
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        RepositoryFile parentFolder = JcrRepositoryFileUtils.getFileById(session, nodeIdStrategy, file.getParentId());
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, nodeIdStrategy, parentFolder);
        JcrRepositoryFileUtils.deleteFile(session, nodeIdStrategy, file);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, nodeIdStrategy, parentFolder);
        return null;
      }
    });
  }

  /**
   * A pluggable method for reading and writing {@link IRepositoryFileContent} implementations.
   * 
   * @param <T> type which this transformer reads and writes
   * @author mlowery
   */
  public static interface Transformer<T extends IRepositoryFileContent> {

    /**
     * Returns {@code true} if this transformer can read and write content of the given type.
     * 
     * @param contentType content type to check 
     * @return {@code true} if this transformer can read and write content of the given type
     */
    boolean supports(final String contentType);

    /**
     * Transforms a JCR node subtree into an {@link IRepositoryFileContent}.
     * 
     * @param session JCR session
     * @param nodeIdStrategy node id strategy to use
     * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
     * @return an {@link IRepositoryFileContent} instance
     * @throws RepositoryException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    T fromContentNode(final Session session, final NodeIdStrategy nodeIdStrategy, final Node resourceNode)
        throws RepositoryException, IOException;

    /**
     * Creates a JCR node subtree representing the given {@code content}.
     * 
     * @param session JCR session
     * @param nodeIdStrategy node id strategy to use
     * @param content content to create
     * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
     * @return an {@link IRepositoryFileContent} instance
     * @throws RepositoryException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    void createContentNode(final Session session, final NodeIdStrategy nodeIdStrategy, final T content,
        final Node resourceNode) throws RepositoryException, IOException;

    /**
     * Updates a JCR node subtree representing the given {@code content}.
     * 
     * @param session JCR session
     * @param nodeIdStrategy node id strategy to use
     * @param content content to update
     * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
     * @return an {@link IRepositoryFileContent} instance
     * @throws RepositoryException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    void updateContentNode(final Session session, final NodeIdStrategy nodeIdStrategy, final T content,
        final Node resourceNode) throws RepositoryException, IOException;

  }

}
