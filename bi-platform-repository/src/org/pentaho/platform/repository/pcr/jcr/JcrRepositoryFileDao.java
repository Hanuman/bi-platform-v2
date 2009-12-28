package org.pentaho.platform.repository.pcr.jcr;

import java.io.IOException;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.repository.pcr.IRepositoryFileDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

/**
 * CRUD operations against JCR. Note that there is no access control in this class (implicit or explicit).
 * 
 * @author mlowery
 */
public class JcrRepositoryFileDao implements IRepositoryFileDao, InitializingBean {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JcrRepositoryFileDao.class);

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  private NodeIdStrategy nodeIdStrategy;

  private List<Transformer<IRepositoryFileContent>> transformers;

  private ILockTokenHelper lockTokenHelper;

  // ~ Constructors ====================================================================================================

  public JcrRepositoryFileDao(final JcrTemplate jcrTemplate,
      final List<Transformer<IRepositoryFileContent>> transformers, final ILockTokenHelper lockTokenHelper) {
    super();
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
    this.nodeIdStrategy = new UuidNodeIdStrategy(jcrTemplate);
    this.lockTokenHelper = lockTokenHelper;
  }

  // ~ Methods =========================================================================================================

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile folder,
      final String... versionMessageAndLabel) {
    Assert.notNull(folder);
    Assert.hasText(folder.getName());
    Assert.isTrue(!folder.getName().contains(RepositoryFile.SEPARATOR));
    Assert.isTrue(folder.isFolder());
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder);
        Node folderNode = JcrRepositoryFileUtils.createFolderNode(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder, folder);
        session.save();
        if (folder.isVersioned()) {
          JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
              folderNode, versionMessageAndLabel);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder, "[system] added child folder '" + folder.getName() + "' to "
                + (parentFolder == null ? "/" : parentFolder.getAbsolutePath()));
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, nodeIdStrategy, folderNode);
      }
    });
  }

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder);
        Node fileNode = JcrRepositoryFileUtils.createFileNode(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder, file, content, findTransformer(content.getContentType()));
        session.save();
        if (file.isVersioned()) {
          JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
              fileNode, versionMessageAndLabel);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder, "[system] added child file '" + file.getName() + "' to "
                + (parentFolder == null ? "/" : parentFolder.getAbsolutePath()));
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, nodeIdStrategy, fileNode);
      }
    });
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileContent content,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);
    Assert.hasText(file.getContentType());

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            file);
        JcrRepositoryFileUtils.updateFileNode(session, pentahoJcrConstants, nodeIdStrategy, file, content,
            findTransformer(file.getContentType()));
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            file, versionMessageAndLabel);
        return JcrRepositoryFileUtils.nodeIdToFile(session, pentahoJcrConstants, nodeIdStrategy, file.getId());
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
      final IRepositoryFileContent content, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalCreateFile(parentFolder, file, content, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile folder,
      final String... versionMessageAndLabel) {
    Assert.notNull(folder);
    Assert.isTrue(folder.isFolder());
    return internalCreateFolder(parentFolder, folder, versionMessageAndLabel);
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
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Item fileNode;
        try {
          fileNode = session.getItem(absPath);
          // items are nodes or properties; this must be a node
          Assert.isTrue(fileNode.isNode());
        } catch (PathNotFoundException e) {
          fileNode = null;
        }
        return fileNode != null ? JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, nodeIdStrategy,
            (Node) fileNode) : null;
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
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getContent(session, pentahoJcrConstants, nodeIdStrategy, file,
            findTransformer(file.getContentType()));
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
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getChildren(session, pentahoJcrConstants, nodeIdStrategy, folder);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileContent content,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalUpdateFile(file, content, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFile(final RepositoryFile file, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.notNull(file.getParentId());
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        RepositoryFile parentFolder = JcrRepositoryFileUtils.getFileById(session, pentahoJcrConstants, nodeIdStrategy,
            file.getParentId());
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder);
        JcrRepositoryFileUtils.deleteFile(session, pentahoJcrConstants, nodeIdStrategy, file, lockTokenHelper);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder, versionMessageAndLabel);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void lockFile(final RepositoryFile file, final String message) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.lockFile(session, pentahoJcrConstants, nodeIdStrategy, file, message, lockTokenHelper);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void unlockFile(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.unlockFile(session, pentahoJcrConstants, nodeIdStrategy, file, lockTokenHelper);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public List<VersionSummary> getVersionSummaries(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return (List<VersionSummary>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getVersionSummaries(session, pentahoJcrConstants, nodeIdStrategy, file);
      }
    });
  }
  
  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final VersionSummary versionSummary) {
    Assert.notNull(versionSummary);
    Assert.notNull(versionSummary.getId());
    Assert.notNull(versionSummary.getVersionedFileId());
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getFileAtVersion(session, pentahoJcrConstants, nodeIdStrategy, versionSummary);
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
     * @param pentahoJcrConstants constants
     * @param nodeIdStrategy node id strategy to use
     * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
     * @return an {@link IRepositoryFileContent} instance
     * @throws RepositoryException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    T fromContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
        final NodeIdStrategy nodeIdStrategy, final Node resourceNode) throws RepositoryException, IOException;

    /**
     * Creates a JCR node subtree representing the given {@code content}.
     * 
     * @param session JCR session
     * @param pentahoJcrConstants constants
     * @param nodeIdStrategy node id strategy to use
     * @param content content to create
     * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
     * @return an {@link IRepositoryFileContent} instance
     * @throws RepositoryException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    void createContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
        final NodeIdStrategy nodeIdStrategy, final T content, final Node resourceNode) throws RepositoryException,
        IOException;

    /**
     * Updates a JCR node subtree representing the given {@code content}.
     * 
     * @param session JCR session
     * @param pentahoJcrConstants constants
     * @param nodeIdStrategy node id strategy to use
     * @param content content to update
     * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
     * @return an {@link IRepositoryFileContent} instance
     * @throws RepositoryException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    void updateContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
        final NodeIdStrategy nodeIdStrategy, final T content, final Node resourceNode) throws RepositoryException,
        IOException;

  }

  /**
   * Helper class that stores, retrieves, and removes lock tokens. In section 8.4.7 of the JSR-170 specification, it 
   * states, "the user must additionally ensure that a reference to the lock token is preserved separately so that it 
   * can later be attached to another session." This manual step is necessary when using open-scoped locks and this 
   * implementation uses open-scoped locks exclusively.
   * 
   * @author mlowery
   */
  public static interface ILockTokenHelper {
    /**
     * Stores a lock token associated with the session's user.
     * 
     * @param session session whose userID will be used
     * @param pentahoJcrConstants constants
     * @param lock recently created lock; can get the locked node and lock token from this object
     */
    void addLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants,
        final NodeIdStrategy nodeIdStrategy, final Lock lock) throws RepositoryException;

    /**
     * Returns all lock tokens belonging to the session's user. Lock tokens can then be added to the session by calling
     * {@code Session.addLockToken(token)}.
     * 
     * @param session session whose userID will be used
     * @param pentahoJcrConstants constants
     * @return list of tokens
     */
    List<String> getLockTokens(final Session session, final PentahoJcrConstants pentahoJcrConstants,
        final NodeIdStrategy nodeIdStrategy) throws RepositoryException;

    /**
     * Removes a lock token
     * 
     * @param session session whose userID will be used
     * @param pentahoJcrConstants constants
     * @param lock lock whose token is to be removed; can get the locked node and lock token from this object
     */
    void removeLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants,
        final NodeIdStrategy nodeIdStrategy, final Lock lock) throws RepositoryException;
  }

}
