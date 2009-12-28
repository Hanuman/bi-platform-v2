package org.pentaho.platform.repository.pcr;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.MutableAclService;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.util.Assert;

/**
 * Implementation of {@link IPentahoContentRepository} using an {@link IRepositoryFileDao} and Spring 
 * Security's {@link MutableAclService}.
 * 
 * @author mlowery
 */
public class PentahoContentRepository implements IPentahoContentRepository {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoContentRepository.class);

  // ~ Instance fields =================================================================================================

  private IRepositoryFileDao contentDao;

  private IPentahoMutableAclService mutableAclService;

  private IRepositoryEventHandler repositoryEventHandler;

  // ~ Constructors ====================================================================================================

  public PentahoContentRepository(final IRepositoryFileDao contentDao,
      final IPentahoMutableAclService mutableAclService, final IRepositoryEventHandler repositoryEventHandler) {
    super();
    Assert.notNull(contentDao);
    Assert.notNull(mutableAclService);
    Assert.notNull(repositoryEventHandler);
    this.contentDao = contentDao;
    this.mutableAclService = mutableAclService;
    this.repositoryEventHandler = repositoryEventHandler;
  }

  // ~ Methods =========================================================================================================

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
   * TODO mlowery figure this delegation out
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
  public synchronized Acl getAcl(final RepositoryFile file) {
    Assert.notNull(file);
    return mutableAclService.readAclById(new ObjectIdentityImpl(RepositoryFile.class, file.getId()));
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
  public synchronized RepositoryFile getFile(VersionSummary versionSummary) {
    Assert.notNull(versionSummary);
    Assert.notNull(versionSummary.getId());
    Assert.notNull(versionSummary.getVersionedFileId());
    return contentDao.getFile(versionSummary);
  }

  /**
   * Returns the username of the current user.
   */
  private String internalGetUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }

  /**
   * Returns the tenant ID of the current user.
   */
  private String internalGetTenantId() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
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

  private MutableAcl internalCreateAcl(final RepositoryFile file, final boolean entriesInheriting) {
    Assert.notNull(file);

    ObjectIdentity parentOid = null;
    if (file.getParentId() != null) {
      parentOid = new ObjectIdentityImpl(RepositoryFile.class, file.getParentId());
    }
    return mutableAclService.createAndInitializeAcl(new ObjectIdentityImpl(RepositoryFile.class, file.getId()),
        parentOid, entriesInheriting, new PrincipalSid(internalGetUsername()));
  }

}
