package org.pentaho.platform.repository.pcr;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.LockSummary;
import org.pentaho.platform.api.repository.RepositoryFile;
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

  private IRepositoryAdminHelper repositoryAdminHelper;

  private boolean startedUp;

  // ~ Constructors ====================================================================================================

  public PentahoContentRepository(final IRepositoryFileDao contentDao,
      final IPentahoMutableAclService mutableAclService, final IRepositoryAdminHelper repositoryAdminHelper) {
    super();
    Assert.notNull(contentDao);
    Assert.notNull(mutableAclService);
    Assert.notNull(repositoryAdminHelper);
    this.contentDao = contentDao;
    this.mutableAclService = mutableAclService;
    this.repositoryAdminHelper = repositoryAdminHelper;
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final String absPath) {
    assertStartedUp();
    Assert.hasText(absPath);

    assertStartedUp();
    return contentDao.getFile(absPath);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getOrCreateUserHomeFolder() {
    assertStartedUp();
    repositoryAdminHelper.createTenantRootFolder();
    repositoryAdminHelper.createInitialTenantFolders();
    repositoryAdminHelper.createUserHomeFolder();
    return getFile(repositoryAdminHelper.getUserHomeFolderPath(internalGetUsername(), internalGetTenantId()));
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void shutdown() {
    assertStartedUp();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void startup() {
    Assert.isNull(PentahoSessionHolder.getSession(), "startup must be run before any users login");
    repositoryAdminHelper.createPentahoRootFolder();
    startedUp = true;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content) {
    assertStartedUp();
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    Assert.notNull(content);
    // external callers never allowed to create files at repo root
    Assert.notNull(parentFolder);
    return internalCreateFile(parentFolder, file, content, true);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file) {
    assertStartedUp();
    Assert.notNull(file);
    Assert.isTrue(file.isFolder());
    Assert.hasText(file.getName());
    // external callers never allowed to create folders at repo root
    Assert.notNull(parentFolder);
    return internalCreateFolder(parentFolder, file, true);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Delegates to {@link #getStreamForRead(RepositoryFile)} but assumes that some external system (e.g. Spring Security)
   * is protecting this method with different authorization rules than {@link #getStreamForRead(RepositoryFile)}.
   * </p>
   * 
   * @see #getContentForRead(RepositoryFile, Class)
   */
  public synchronized <T extends IRepositoryFileContent> T getContentForExecute(RepositoryFile file,
      Class<T> contentClass) {
    assertStartedUp();
    return getContentForRead(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized <T extends IRepositoryFileContent> T getContentForRead(RepositoryFile file, Class<T> contentClass) {
    assertStartedUp();
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return contentDao.getContent(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<RepositoryFile> getChildren(final RepositoryFile folder) {
    assertStartedUp();
    Assert.notNull(folder);
    Assert.notNull(folder.getId());
    Assert.notNull(folder.isFolder());
    return contentDao.getChildren(folder);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile updateFile(RepositoryFile file, IRepositoryFileContent content) {
    assertStartedUp();
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    if (!file.isFolder()) {
      Assert.notNull(content);
      Assert.hasText(file.getContentType());
    }

    return internalUpdateFile(file, content);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void deleteFile(RepositoryFile file) {
    assertStartedUp();
    Assert.notNull(file);
    Assert.notNull(file.getId());
    // acl deleted when file node is deleted
    contentDao.deleteFile(file);
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
  public synchronized LockSummary getLockSummary(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    return contentDao.getLockSummary(file);
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

  public String getPentahoRootFolderPath() {
    return repositoryAdminHelper.getPentahoRootFolderPath();
  }

  public String getTenantHomeFolderPath() {
    return repositoryAdminHelper.getTenantHomeFolderPath(internalGetTenantId());
  }

  public String getTenantPublicFolderPath() {
    return repositoryAdminHelper.getTenantPublicFolderPath(internalGetTenantId());
  }

  public String getTenantRootFolderPath() {
    return repositoryAdminHelper.getTenantRootFolderPath(internalGetTenantId());
  }

  public String getUserHomeFolderPath() {
    return repositoryAdminHelper.getUserHomeFolderPath(internalGetUsername(), internalGetTenantId());
  }

  public static interface IRepositoryAdminHelper {
    String getPentahoRootFolderPath();

    String getTenantRootFolderPath(final String tenantId);

    String getTenantHomeFolderPath(final String tenantId);

    String getTenantPublicFolderPath(final String tenantId);

    String getUserHomeFolderPath(final String username, final String tenantId);

    void createPentahoRootFolder();

    void createTenantRootFolder();

    void createInitialTenantFolders();

    void createUserHomeFolder();
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

  /**
   * Throws an {@code IllegalStateException} if not started up.  Should be called from all public methods (except 
   * {@link #startup()}).
   */
  private void assertStartedUp() {
    Assert.state(startedUp, "startup must be called first");
  }

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content, final boolean inheritAces) {
    Assert.notNull(file);
    Assert.notNull(content);

    RepositoryFile newFile = contentDao.createFile(parentFolder, file, content);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final boolean inheritAces) {
    Assert.notNull(file);

    RepositoryFile newFile = contentDao.createFolder(parentFolder, file);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.notNull(content);

    return contentDao.updateFile(file, content);
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
