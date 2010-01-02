package org.pentaho.platform.repository.pcr;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IRepositoryService;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
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

  public synchronized List<Ace> getEffectiveAces(final RepositoryFile file) {
    return repositoryFileAclDao.getEffectiveAces(file);
  }
  
  public synchronized boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions) {
    return repositoryFileAclDao.hasAccess(absPath, permissions);
  }

  public synchronized IRepositoryEventHandler getRepositoryEventHandler() {
    return repositoryEventHandler;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final String absPath) {
    Assert.hasText(absPath);

    return repositoryFileDao.getFile(absPath);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    Assert.notNull(content);
    // external callers never allowed to create files at repo root
    Assert.notNull(parentFolder);
    return internalCreateFile(parentFolder, file, content, true, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(file.isFolder());
    Assert.hasText(file.getName());
    // external callers never allowed to create folders at repo root
    Assert.notNull(parentFolder);
    return internalCreateFolder(parentFolder, file, true, versionMessageAndLabel);

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
   * @see #getContentForRead(RepositoryFile, Class)
   */
  public synchronized <T extends IRepositoryFileContent> T getContentForExecute(RepositoryFile file,
      Class<T> contentClass) {
    return getContentForRead(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized <T extends IRepositoryFileContent> T getContentForRead(RepositoryFile file, Class<T> contentClass) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return repositoryFileDao.getContent(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<RepositoryFile> getChildren(final RepositoryFile folder) {
    Assert.notNull(folder);
    Assert.notNull(folder.getId());
    Assert.notNull(folder.isFolder());
    return repositoryFileDao.getChildren(folder);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileContent content,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    if (!file.isFolder()) {
      Assert.notNull(content);
      Assert.hasText(file.getContentType());
    }

    return internalUpdateFile(file, content, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void deleteFile(final RepositoryFile file, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    // acl deleted when file node is deleted
    repositoryFileDao.deleteFile(file, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFileAcl getAcl(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return repositoryFileAclDao.readAclById(file.getId());
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void lockFile(final RepositoryFile file, final String message) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    repositoryFileDao.lockFile(file, message);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void unlockFile(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    repositoryFileDao.unlockFile(file);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<VersionSummary> getVersionSummaries(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return repositoryFileDao.getVersionSummaries(file);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final VersionSummary versionSummary) {
    Assert.notNull(versionSummary);
    Assert.notNull(versionSummary.getId());
    Assert.notNull(versionSummary.getVersionedFileId());
    return repositoryFileDao.getFile(versionSummary);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void setAcl(final RepositoryFileAcl acl) {
    Assert.notNull(acl);
    Assert.notNull(acl.getId());
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

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content, final boolean inheritAces, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(content);

    RepositoryFile newFile = repositoryFileDao.createFile(parentFolder, file, content, versionMessageAndLabel);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final boolean inheritAces, final String... versionMessageAndLabel) {
    Assert.notNull(file);

    RepositoryFile newFile = repositoryFileDao.createFolder(parentFolder, file, versionMessageAndLabel);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileContent content,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(content);

    return repositoryFileDao.updateFile(file, content, versionMessageAndLabel);
  }

  private RepositoryFileAcl internalCreateAcl(final RepositoryFile file, final boolean entriesInheriting) {
    Assert.notNull(file);

    return repositoryFileAclDao.createAcl(file.getId(), entriesInheriting, new RepositoryFileSid(internalGetUsername()),
        RepositoryFilePermission.ALL);
  }

}
