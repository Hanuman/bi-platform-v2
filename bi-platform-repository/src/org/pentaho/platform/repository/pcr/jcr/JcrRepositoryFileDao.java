package org.pentaho.platform.repository.pcr.jcr;

import java.io.IOException;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IRepositoryFileData;
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

  private List<ITransformer<IRepositoryFileData>> transformers;

  private ILockTokenHelper lockTokenHelper = new DefaultLockTokenHelper();

  private IOwnerLookupHelper ownerLookupHelper = new DefaultOwnerLookupHelper();

  // ~ Constructors ====================================================================================================

  public JcrRepositoryFileDao(final JcrTemplate jcrTemplate, final List<ITransformer<IRepositoryFileData>> transformers) {
    super();
    Assert.notNull(jcrTemplate);
    Assert.notNull(transformers);
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
    this.nodeIdStrategy = new UuidNodeIdStrategy(jcrTemplate);
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
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, nodeIdStrategy, ownerLookupHelper,
            folderNode);
      }
    });
  }

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileData content, final String... versionMessageAndLabel) {
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
            parentFolder, file, content, findTransformer(getFileExtension(file.getName()), content.getClass()));
        session.save();
        if (file.isVersioned()) {
          JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
              fileNode, versionMessageAndLabel);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            parentFolder, "[system] added child file '" + file.getName() + "' to "
                + (parentFolder == null ? "/" : parentFolder.getAbsolutePath()));
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, nodeIdStrategy, ownerLookupHelper,
            fileNode);
      }
    });
  }

  /**
   * File names can contain more than one period but the very first period starting from the left will be used to find
   * the files
   * @param fileName
   * @return
   */
  private String getFileExtension(final String fileName) {
    final String DOT = "."; //$NON-NLS-1$
    Assert.hasText(fileName);
    Assert.isTrue(fileName.contains(DOT), "file names must have an extension");
    int firstDotIndex = fileName.indexOf(DOT);
    String extension = fileName.substring(firstDotIndex + 1);
    Assert.hasText(extension, "file names must have an extension");
    return extension;
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            file);
        JcrRepositoryFileUtils.updateFileNode(session, pentahoJcrConstants, nodeIdStrategy, file, content,
            findTransformer(getFileExtension(file.getName()), content.getClass()));
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, nodeIdStrategy,
            file, versionMessageAndLabel);
        return JcrRepositoryFileUtils.nodeIdToFile(session, pentahoJcrConstants, nodeIdStrategy, ownerLookupHelper,
            file.getId());
      }
    });
  }

  private ITransformer<IRepositoryFileData> findTransformer(final String extension,
      final Class<? extends IRepositoryFileData> clazz) {
    for (ITransformer<IRepositoryFileData> transformer : transformers) {
      if (transformer.supports(extension, clazz)) {
        return transformer;
      }
    }
    throw new IllegalArgumentException(String.format(
        "no transformer for the file extension [%s] and IRepositoryData type [%s] exists", extension, clazz.getName()));
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileData content, final String... versionMessageAndLabel) {
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
            ownerLookupHelper, (Node) fileNode) : null;
      }
    });

  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileData> T getContent(final RepositoryFile file, final Class<T> contentClass) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    return (T) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getContent(session, pentahoJcrConstants, nodeIdStrategy, file, findTransformer(
            getFileExtension(file.getName()), contentClass));
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
        return JcrRepositoryFileUtils.getChildren(session, pentahoJcrConstants, nodeIdStrategy, ownerLookupHelper,
            folder);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData content,
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
            ownerLookupHelper, file.getParentId());
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
        return JcrRepositoryFileUtils.getFileAtVersion(session, pentahoJcrConstants, nodeIdStrategy, ownerLookupHelper,
            versionSummary);
      }
    });
  }

  public void setLockTokenHelper(final ILockTokenHelper lockTokenHelper) {
    Assert.notNull(lockTokenHelper);
    this.lockTokenHelper = lockTokenHelper;
  }

  public void setOwnerLookupHelper(final IOwnerLookupHelper ownerLookupHelper) {
    Assert.notNull(ownerLookupHelper);
    this.ownerLookupHelper = ownerLookupHelper;
  }

}
