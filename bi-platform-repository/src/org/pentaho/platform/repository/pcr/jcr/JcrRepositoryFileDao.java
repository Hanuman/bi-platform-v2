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
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

/**
 * CRUD operations against JCR. Note that there is no access control in this class (implicit or explicit).
 * 
 * @author mlowery
 */
public class JcrRepositoryFileDao implements IRepositoryFileDao {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JcrRepositoryFileDao.class);

  // ~ Instance fields =================================================================================================

  private JcrTemplate jcrTemplate;

  private List<ITransformer<IRepositoryFileData>> transformers;

  private ILockTokenHelper lockTokenHelper;

  private IOwnerLookupHelper ownerLookupHelper;

  private IDeleteHelper deleteHelper;

  // ~ Constructors ====================================================================================================

  public JcrRepositoryFileDao(final JcrTemplate jcrTemplate,
      final List<ITransformer<IRepositoryFileData>> transformers, final IOwnerLookupHelper ownerLookupHelper,
      final ILockTokenHelper lockTokenHelper, final IDeleteHelper deleteHelper) {
    super();
    Assert.notNull(jcrTemplate);
    Assert.notNull(transformers);
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
    this.lockTokenHelper = lockTokenHelper;
    this.ownerLookupHelper = ownerLookupHelper;
    this.deleteHelper = deleteHelper;
  }

  // ~ Methods =========================================================================================================

  private RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile folder,
      final String versionMessage) {
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
              versionMessage);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId,
            "[system] added child folder '" + folder.getName() + "' to " + parentFolderId);
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, folderNode);
      }
    });
  }

  private RepositoryFile internalCreateFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final String versionMessage) {
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
              versionMessage);
        }
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId,
            "[system] added child file '" + file.getName() + "' to "
                + (parentFolderId == null ? "root" : parentFolderId));
        return JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper, fileNode);
      }
    });
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String versionMessage) {
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
            versionMessage);
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
      final IRepositoryFileData content, final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalCreateFile(parentFolderId, file, content, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile folder,
      final String versionMessage) {
    Assert.notNull(folder);
    Assert.isTrue(folder.isFolder());
    return internalCreateFolder(parentFolderId, folder, versionMessage);
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
  public List<RepositoryFile> getChildren(final Serializable folderId, final String filter) {
    Assert.notNull(folderId);
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getChildren(session, pentahoJcrConstants, ownerLookupHelper, folderId, filter);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    return internalUpdateFile(file, content, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public void lockFile(final Serializable fileId, final String message) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, fileId);
        JcrRepositoryFileUtils.lockFile(session, pentahoJcrConstants, fileId, message, lockTokenHelper);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, fileId,
            "[system] locked file with id=" + fileId);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   * 
   * TODO mlowery fix this hack so that locked files don't have to be checked out to remove custom lock fields
   */
  public void unlockFile(final Serializable fileId) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
//        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, fileId);
        JcrRepositoryFileUtils.unlockFile(session, pentahoJcrConstants, fileId, lockTokenHelper);
//        session.save();
//        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, fileId,
//            "[system] unlocked file with id=" + fileId);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
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

  /**
   * {@inheritDoc}
   */
  public void deleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Serializable parentFolderId = JcrRepositoryFileUtils.getParentId(session, fileId);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
        deleteHelper.deleteFile(session, pentahoJcrConstants, fileId);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId,
            versionMessage);
        return null;
      }
    });
  }
  
  /**
   * {@inheritDoc}
   */
  public void deleteFileAtVersion(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        Node fileToDeleteNode = session.getNodeByUUID(fileId.toString());
        fileToDeleteNode.getVersionHistory().removeVersion(versionId.toString());
        session.save();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getDeletedFiles(final Serializable folderId, final String filter) {
    Assert.notNull(folderId);
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return deleteHelper.getDeletedFiles(session, pentahoJcrConstants, folderId, filter);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<RepositoryFile> getDeletedFiles() {
    return (List<RepositoryFile>) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return deleteHelper.getDeletedFiles(session, pentahoJcrConstants);
      }
    });
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * No checkout needed as .trash is not versioned.
   * </p>
   */
  public void permanentlyDeleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        deleteHelper.permanentlyDeleteFile(session, pentahoJcrConstants, fileId);
        session.save();
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void undeleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        Serializable parentFolderId = deleteHelper.getOriginalParentFolderId(session, pentahoJcrConstants, fileId);
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
        deleteHelper.undeleteFile(session, pentahoJcrConstants, fileId);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId,
            versionMessage);
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void moveFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
    Assert.notNull(fileId);
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        String cleanDestAbsPath = destAbsPath;
        if (cleanDestAbsPath.endsWith(RepositoryFile.SEPARATOR)) {
          cleanDestAbsPath.substring(0, cleanDestAbsPath.length() - 1);
        }
        Node srcFileNode = session.getNodeByUUID(fileId.toString());
        Serializable srcParentFolderId = JcrRepositoryFileUtils.getParentId(session, fileId);
        boolean appendFileName = false;
        boolean destExists = true;
        Node destFileNode = null;
        Node destParentFolderNode = null;
        try {
          destFileNode = (Node) session.getItem(cleanDestAbsPath);
        } catch (PathNotFoundException e) {
          destExists = false;
        }
        if (destExists) {
          // make sure it's a file or folder
          Assert.isTrue(JcrRepositoryFileUtils.isSupportedNodeType(pentahoJcrConstants, destFileNode));
          // existing item; make sure src is not a folder if dest is a file
          Assert.isTrue(
              !(JcrRepositoryFileUtils.isPentahoFolder(pentahoJcrConstants, srcFileNode) && JcrRepositoryFileUtils
                  .isPentahoFile(pentahoJcrConstants, destFileNode)), "cannot overwrite file with folder");
          if (JcrRepositoryFileUtils.isPentahoFolder(pentahoJcrConstants, destFileNode)) {
            // existing item; caller is not renaming file, only moving it
            appendFileName = true;
            destParentFolderNode = destFileNode;
          } else {
            // get parent of existing dest item
            int lastSlashIndex = cleanDestAbsPath.lastIndexOf(RepositoryFile.SEPARATOR);
            Assert.isTrue(lastSlashIndex > 1, "illegal destination path");
            String absPathToDestParentFolder = cleanDestAbsPath.substring(0, lastSlashIndex);
            destParentFolderNode = (Node) session.getItem(absPathToDestParentFolder);
          }
        } else {
          // destination doesn't exist; go up one level to a folder that does exist
          int lastSlashIndex = cleanDestAbsPath.lastIndexOf(RepositoryFile.SEPARATOR);
          Assert.isTrue(lastSlashIndex > 1, "illegal destination path");
          String absPathToDestParentFolder = cleanDestAbsPath.substring(0, lastSlashIndex);
          try {
            destParentFolderNode = (Node) session.getItem(absPathToDestParentFolder);
          } catch (PathNotFoundException e1) {
            Assert.isTrue(false, "immediate parent folder of destination path must exist");
          }
          Assert.isTrue(JcrRepositoryFileUtils.isPentahoFolder(pentahoJcrConstants, destParentFolderNode),
              "immediate parent of destination path is not a folder");
        }
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants,
            srcParentFolderId);
        JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, pentahoJcrConstants,
            destParentFolderNode);
        session.move(srcFileNode.getPath(), appendFileName ? cleanDestAbsPath + RepositoryFile.SEPARATOR
            + srcFileNode.getName() : cleanDestAbsPath);
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants,
            destParentFolderNode, versionMessage);
        // if it's a move within the same folder, then the next checkin is unnecessary
        if (!destParentFolderNode.getUUID().equals(srcParentFolderId.toString())) {
          JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary(session, pentahoJcrConstants,
              srcParentFolderId, versionMessage);
        }
        return null;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public VersionSummary getVersionSummary(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    return (VersionSummary) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
        return JcrRepositoryFileUtils.getVersionSummary(session, pentahoJcrConstants, fileId, versionId);
      }
    });
  }

}
