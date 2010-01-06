package org.pentaho.platform.repository.pcr.jcr;

import java.io.IOException;
import java.io.Serializable;
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
  }

  // ~ Methods =========================================================================================================

  private RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile folder,
      final String... versionMessageAndLabel) {
    Assert.notNull(folder);
    Assert.hasText(folder.getName());
    Assert.isTrue(!folder.getName().contains(RepositoryFile.SEPARATOR));
    Assert.isTrue(folder.isFolder());

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
        Node folderNode = JcrRepositoryFileUtils.createFolderNode(session, pentahoJcrConstants, parentFolderId, folder);
        session.save();
        if (folder.isVersioned()) {
          JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, folderNode,
              versionMessageAndLabel);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId,
            "[system] added child folder '" + folder.getName() + "' to " + parentFolderId);
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, folderNode);
      }
    });
  }

  private RepositoryFile internalCreateFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.hasText(file.getName());
    Assert.isTrue(!file.isFolder());
    Assert.notNull(content);

    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
        Node fileNode = JcrRepositoryFileUtils.createFileNode(session, pentahoJcrConstants, parentFolderId, file,
            content, findTransformer(JcrRepositoryFileUtils.getFileExtension(file.getName()), content.getClass()));
        session.save();
        if (file.isVersioned()) {
          JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, fileNode,
              versionMessageAndLabel);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId,
            "[system] added child file '" + file.getName() + "' to "
                + (parentFolderId == null ? "root" : parentFolderId));
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, fileNode);
      }
    });
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
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, file.getId());
        JcrRepositoryFileUtils.updateFileNode(session, pentahoJcrConstants, file, content, findTransformer(
            JcrRepositoryFileUtils.getFileExtension(session, file.getId()), content.getClass()));
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, file.getId(),
            versionMessageAndLabel);
        return JcrRepositoryFileUtils.nodeIdToFile(session, pentahoJcrConstants, ownerLookupHelper, file.getId());
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
  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalCreateFile(parentFolderId, file, content, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile folder,
      final String... versionMessageAndLabel) {
    Assert.notNull(folder);
    Assert.isTrue(folder.isFolder());
    return internalCreateFolder(parentFolderId, folder, versionMessageAndLabel);
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(jcrTemplate, "jcrTemplate required");
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileById(final Serializable fileId) {
    return internalGetFileById(fileId, false);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileById(final Serializable fileId, final boolean loadMaps) {
    return internalGetFileById(fileId, loadMaps);
  }

  private RepositoryFile internalGetFileById(final Serializable fileId, final boolean loadMaps) {
    Assert.notNull(fileId);
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Node fileNode = session.getNodeByUUID(fileId.toString());
        return fileNode != null ? JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper,
            fileNode, loadMaps) : null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String absPath) {
    return internalGetFile(absPath, false);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String absPath, final boolean loadMaps) {
    return internalGetFile(absPath, loadMaps);
  }

  private RepositoryFile internalGetFile(final String absPath, final boolean loadMaps) {
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
        return fileNode != null ? JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper,
            (Node) fileNode, loadMaps) : null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileData> T getData(final Serializable fileId, final Serializable versionId,
      final Class<T> contentClass) {
    Assert.notNull(fileId);
    return (T) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getContent(session, pentahoJcrConstants, fileId, versionId, findTransformer(
            JcrRepositoryFileUtils.getFileExtension(session, fileId), contentClass));
      }
    });

  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getChildren(final Serializable folderId) {
    Assert.notNull(folderId);
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getChildren(session, pentahoJcrConstants, ownerLookupHelper, folderId);
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
  public void deleteFile(final Serializable fileId, final String... versionMessageAndLabel) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        RepositoryFile parentFolder = JcrRepositoryFileUtils.getFileById(session, pentahoJcrConstants,
            ownerLookupHelper, JcrRepositoryFileUtils.getParentId(session, fileId));
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolder
            .getId());
        JcrRepositoryFileUtils.deleteFile(session, pentahoJcrConstants, fileId, lockTokenHelper);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolder
            .getId(), versionMessageAndLabel);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void lockFile(final Serializable fileId, final String message) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.lockFile(session, pentahoJcrConstants, fileId, message, lockTokenHelper);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void unlockFile(final Serializable fileId) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.unlockFile(session, pentahoJcrConstants, fileId, lockTokenHelper);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public List<VersionSummary> getVersionSummaries(final Serializable fileId) {
    Assert.notNull(fileId);
    return (List<VersionSummary>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getVersionSummaries(session, pentahoJcrConstants, fileId);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    return (RepositoryFile) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getFileAtVersion(session, pentahoJcrConstants, ownerLookupHelper, fileId,
            versionId);
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