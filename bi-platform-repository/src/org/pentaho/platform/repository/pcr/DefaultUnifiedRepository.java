package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.api.repository.RepositoryFileAcl.Ace;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link IUnifiedRepository}. Delegates to {@link IRepositoryFileDao} and 
 * {@link IRepositoryFileAclDao}.
 * 
 * @author mlowery
 */
public class DefaultUnifiedRepository implements IUnifiedRepository {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultUnifiedRepository.class);

  // ~ Instance fields =================================================================================================

  private IRepositoryFileDao repositoryFileDao;

  private IRepositoryFileAclDao repositoryFileAclDao;

  private IRepositoryLifecycleManager repositoryEventHandler;

  // ~ Constructors ====================================================================================================

  public DefaultUnifiedRepository(final IRepositoryFileDao contentDao, final IRepositoryFileAclDao aclDao,
      final IRepositoryLifecycleManager repositoryEventHandler) {
    super();
    Assert.notNull(contentDao);
    Assert.notNull(aclDao);
    Assert.notNull(repositoryEventHandler);
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = aclDao;
    this.repositoryEventHandler = repositoryEventHandler;
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public List<Ace> getEffectiveAces(final Serializable fileId) {
    return repositoryFileAclDao.getEffectiveAces(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions) {
    return repositoryFileAclDao.hasAccess(absPath, permissions);
  }

  /**
   * {@inheritDoc}
   */
  public IRepositoryLifecycleManager getRepositoryLifecycleManager() {
    return repositoryEventHandler;
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String absPath) {
    Assert.hasText(absPath);
    assertNotRoot(absPath);
    return repositoryFileDao.getFile(absPath, false);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileById(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getFileById(fileId, false);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String absPath, final boolean loadMaps) {
    Assert.hasText(absPath);
    assertNotRoot(absPath);
    return repositoryFileDao.getFile(absPath, loadMaps);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileById(final Serializable fileId, final boolean loadMaps) {
    Assert.notNull(fileId);
    return repositoryFileDao.getFileById(fileId, loadMaps);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    Assert.notNull(data);
    // external callers never allowed to create files at repo root
    Assert.notNull(parentFolderId);
    return internalCreateFile(parentFolderId, file, data, true, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(file.isFolder());
    Assert.hasText(file.getName());
    // external callers never allowed to create folders at repo root
    Assert.notNull(parentFolderId);
    return internalCreateFolder(parentFolderId, file, true, versionMessage);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Delegates to {@link #getStreamForRead(RepositoryFile)} but assumes that some external system (e.g. Spring Security)
   * is protecting this method with different authorization rules than {@link #getStreamForRead(RepositoryFile)}.
   * </p>
   * 
   * <p>
   * In a direct contradiction of the previous paragraph, this implementation is not currently protected by Spring
   * Security.
   * </p>
   * 
   * @see #getDataForRead(RepositoryFile, Class)
   */
  public <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId, final Class<T> dataClass) {
    return getDataForExecute(fileId, null, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId, final Serializable versionId,
      final Class<T> dataClass) {
    return getDataForRead(fileId, versionId, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId, final Class<T> dataClass) {
    return getDataForRead(fileId, null, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId, final Serializable versionId,
      final Class<T> dataClass) {
    Assert.notNull(fileId);
    return repositoryFileDao.getData(fileId, versionId, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getChildren(final Serializable folderId) {
    return getChildren(folderId, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getChildren(final Serializable folderId, final String filter) {
    Assert.notNull(folderId);
    return repositoryFileDao.getChildren(folderId, filter);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.notNull(data);

    return internalUpdateFile(file, data, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFile(final Serializable fileId, final boolean permanent, final String versionMessage) {
    Assert.notNull(fileId);
    if (permanent) {
      // fyi: acl deleted when file node is deleted
      repositoryFileDao.permanentlyDeleteFile(fileId, versionMessage);
    } else {
      repositoryFileDao.deleteFile(fileId, versionMessage);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFile(final Serializable fileId, final String versionMessage) {
    deleteFile(fileId, false, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles(final Serializable folderId) {
    return getDeletedFiles(folderId, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles(final Serializable folderId, final String filter) {
    Assert.notNull(folderId);
    return repositoryFileDao.getDeletedFiles(folderId, filter);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles() {
    return repositoryFileDao.getDeletedFiles();
  }

  /**
   * {@inheritDoc}
   */
  public void undeleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    repositoryFileDao.undeleteFile(fileId, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFileAcl getAcl(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileAclDao.getAcl(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public void lockFile(final Serializable fileId, final String message) {
    Assert.notNull(fileId);
    repositoryFileDao.lockFile(fileId, message);
  }

  /**
   * {@inheritDoc}
   */
  public void unlockFile(final Serializable fileId) {
    Assert.notNull(fileId);
    repositoryFileDao.unlockFile(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public VersionSummary getVersionSummary(Serializable fileId, Serializable versionId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getVersionSummary(fileId, versionId);
  }

  /**
   * {@inheritDoc}
   */
  public List<VersionSummary> getVersionSummaries(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getVersionSummaries(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    return repositoryFileDao.getFile(fileId, versionId);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFileAcl updateAcl(final RepositoryFileAcl acl) {
    Assert.notNull(acl);
    return repositoryFileAclDao.updateAcl(acl);
  }

  /**
   * {@inheritDoc}
   */
  public void moveFile(Serializable fileId, String destAbsPath, String versionMessage) {
    Assert.notNull(fileId);
    Assert.hasText(destAbsPath);
    repositoryFileDao.moveFile(fileId, destAbsPath, versionMessage);
  }

  /**
   * Returns the username of the current user.
   */
  private String internalGetUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }

  private RepositoryFile internalCreateFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final boolean inheritAces, final String versionMessage) {
    Assert.notNull(file);
    Assert.notNull(data);

    RepositoryFile newFile = repositoryFileDao.createFile(parentFolderId, file, data, versionMessage);
    internalCreateAcl(newFile.getId(), inheritAces);

    return newFile;
  }

  private RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile file,
      final boolean inheritAces, final String versionMessage) {
    Assert.notNull(file);

    RepositoryFile newFile = repositoryFileDao.createFolder(parentFolderId, file, versionMessage);
    internalCreateAcl(newFile.getId(), inheritAces);

    return newFile;
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.notNull(data);

    return repositoryFileDao.updateFile(file, data, versionMessage);
  }

  private RepositoryFileAcl internalCreateAcl(final Serializable fileId, final boolean entriesInheriting) {
    Assert.notNull(fileId);

    return repositoryFileAclDao.createAcl(fileId, entriesInheriting, new RepositoryFileSid(internalGetUsername()),
        RepositoryFilePermission.ALL);
  }
  
  protected void assertNotRoot(final String absPath) {
    Assert.isTrue(absPath != null && !absPath.trim().equals(RepositoryFile.SEPARATOR), "root folder is illegal");
  }

}
