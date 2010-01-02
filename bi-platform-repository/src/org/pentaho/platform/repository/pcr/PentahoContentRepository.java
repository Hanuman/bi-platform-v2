package org.pentaho.platform.repository.pcr;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link IPentahoContentRepository}.
 * 
 * @author mlowery
 */
public class PentahoContentRepository implements IPentahoContentRepository {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoContentRepository.class);

  // ~ Instance fields =================================================================================================

  private IRepositoryFileDao contentDao;

  private IRepositoryFileAclDao mutableAclService;

  private IRepositoryEventHandler repositoryEventHandler;

  // ~ Constructors ====================================================================================================

  public PentahoContentRepository(final IRepositoryFileDao contentDao,
      final IRepositoryFileAclDao mutableAclService, final IRepositoryEventHandler repositoryEventHandler) {
    super();
    Assert.notNull(contentDao);
    Assert.notNull(mutableAclService);
    Assert.notNull(repositoryEventHandler);
    this.contentDao = contentDao;
    this.mutableAclService = mutableAclService;
    this.repositoryEventHandler = repositoryEventHandler;
  }

  // ~ Methods =========================================================================================================

  public synchronized boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions) {
    return mutableAclService.hasAccess(absPath, permissions);  
  }
  
  public synchronized IRepositoryEventHandler getRepositoryEventHandler() {
    return repositoryEventHandler;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final String absPath) {
    Assert.hasText(absPath);

    return contentDao.getFile(absPath);
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
    return contentDao.getContent(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<RepositoryFile> getChildren(final RepositoryFile folder) {
    Assert.notNull(folder);
    Assert.notNull(folder.getId());
    Assert.notNull(folder.isFolder());
    return contentDao.getChildren(folder);
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
    contentDao.deleteFile(file, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFileAcl getAcl(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return mutableAclService.readAclById(file.getId());
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void lockFile(final RepositoryFile file, final String message) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    contentDao.lockFile(file, message);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void unlockFile(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    contentDao.unlockFile(file);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<VersionSummary> getVersionSummaries(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return contentDao.getVersionSummaries(file);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final VersionSummary versionSummary) {
    Assert.notNull(versionSummary);
    Assert.notNull(versionSummary.getId());
    Assert.notNull(versionSummary.getVersionedFileId());
    return contentDao.getFile(versionSummary);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void setAcl(final RepositoryFileAcl acl) {
    Assert.notNull(acl);
    Assert.notNull(acl.getId());
    mutableAclService.updateAcl(acl);
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

    RepositoryFile newFile = contentDao.createFile(parentFolder, file, content, versionMessageAndLabel);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final boolean inheritAces, final String... versionMessageAndLabel) {
    Assert.notNull(file);

    RepositoryFile newFile = contentDao.createFolder(parentFolder, file, versionMessageAndLabel);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileContent content,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(content);

    return contentDao.updateFile(file, content, versionMessageAndLabel);
  }

  private RepositoryFileAcl internalCreateAcl(final RepositoryFile file, final boolean entriesInheriting) {
    Assert.notNull(file);

    return mutableAclService.createAcl(file.getId(), entriesInheriting, new RepositoryFileSid(internalGetUsername()),
        RepositoryFilePermission.ALL);
  }

}
