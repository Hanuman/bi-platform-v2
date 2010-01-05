package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.api.repository.IRepositoryService;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.api.repository.RepositoryFileAcl.Ace;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link IRepositoryService}.
 * 
 * @author mlowery
 */
public class DefaultRepositoryService implements IRepositoryService {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultRepositoryService.class);

  // ~ Instance fields =================================================================================================

  private IRepositoryFileDao repositoryFileDao;

  private IRepositoryFileAclDao repositoryFileAclDao;

  private IRepositoryEventHandler repositoryEventHandler;

  // ~ Constructors ====================================================================================================

  public DefaultRepositoryService(final IRepositoryFileDao contentDao, final IRepositoryFileAclDao aclDao,
      final IRepositoryEventHandler repositoryEventHandler) {
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
  public synchronized List<Ace> getEffectiveAces(final Serializable fileId) {
    return repositoryFileAclDao.getEffectiveAces(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions) {
    return repositoryFileAclDao.hasAccess(absPath, permissions);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized IRepositoryEventHandler getRepositoryEventHandler() {
    return repositoryEventHandler;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final String absPath) {
    Assert.hasText(absPath);
    return repositoryFileDao.getFile(absPath, false);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFileById(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getFileById(fileId, false);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final String absPath, final boolean loadMaps) {
    Assert.hasText(absPath);
    return repositoryFileDao.getFile(absPath, loadMaps);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFileById(final Serializable fileId, final boolean loadMaps) {
    Assert.notNull(fileId);
    return repositoryFileDao.getFileById(fileId, loadMaps);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    Assert.notNull(data);
    // external callers never allowed to create files at repo root
    Assert.notNull(parentFolderId);
    return internalCreateFile(parentFolderId, file, data, true, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(file.isFolder());
    Assert.hasText(file.getName());
    // external callers never allowed to create folders at repo root
    Assert.notNull(parentFolderId);
    return internalCreateFolder(parentFolderId, file, true, versionMessageAndLabel);
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
  public synchronized <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId,
      final Class<T> dataClass) {
    return getDataForExecute(fileId, null, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass) {
    return getDataForRead(fileId, versionId, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId,
      final Class<T> dataClass) {
    return getDataForRead(fileId, null, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass) {
    Assert.notNull(fileId);
    return repositoryFileDao.getData(fileId, versionId, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<RepositoryFile> getChildren(final Serializable folderId) {
    Assert.notNull(folderId);
    return repositoryFileDao.getChildren(folderId);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(data);

    return internalUpdateFile(file, data, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void deleteFile(final Serializable fileId, final String... versionMessageAndLabel) {
    Assert.notNull(fileId);
    // acl deleted when file node is deleted
    repositoryFileDao.deleteFile(fileId, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFileAcl getAcl(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileAclDao.readAclById(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void lockFile(final Serializable fileId, final String message) {
    Assert.notNull(fileId);
    repositoryFileDao.lockFile(fileId, message);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void unlockFile(final Serializable fileId) {
    Assert.notNull(fileId);
    repositoryFileDao.unlockFile(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<VersionSummary> getVersionSummaries(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getVersionSummaries(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    return repositoryFileDao.getFile(fileId, versionId);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void updateAcl(final RepositoryFileAcl acl) {
    Assert.notNull(acl);
    repositoryFileAclDao.updateAcl(acl);
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
      final IRepositoryFileData data, final boolean inheritAces, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(data);

    RepositoryFile newFile = repositoryFileDao.createFile(parentFolderId, file, data, versionMessageAndLabel);
    internalCreateAcl(newFile.getId(), inheritAces);

    return newFile;
  }

  private RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile file,
      final boolean inheritAces, final String... versionMessageAndLabel) {
    Assert.notNull(file);

    RepositoryFile newFile = repositoryFileDao.createFolder(parentFolderId, file, versionMessageAndLabel);
    internalCreateAcl(newFile.getId(), inheritAces);

    return newFile;
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(data);

    return repositoryFileDao.updateFile(file, data, versionMessageAndLabel);
  }

  private RepositoryFileAcl internalCreateAcl(final Serializable fileId, final boolean entriesInheriting) {
    Assert.notNull(fileId);

    return repositoryFileAclDao.createAcl(fileId, entriesInheriting, new RepositoryFileSid(internalGetUsername()),
        RepositoryFilePermission.ALL);
  }

}
