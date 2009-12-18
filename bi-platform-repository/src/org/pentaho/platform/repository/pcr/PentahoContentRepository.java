package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IPentahoContentDao;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.springsecurity.RepositoryFilePermission;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.MutableAclService;
import org.springframework.security.acls.NotFoundException;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * Implementation of {@link IPentahoContentRepository} using an {@link IPentahoContentDao} and Spring 
 * Security's {@link MutableAclService}.
 * 
 * <p>
 * {@link #startup} and {@link #shutdown} should not be called directly. They should be called from a helper class.
 * </p>
 * 
 * TODO mlowery create helper class for startup and shutdown; helper would set the repo admin Authentication and then 
 * call startup on this; in a finally block, the Authentication would be set back to null
 * 
 * @author mlowery
 */
public class PentahoContentRepository implements IPentahoContentRepository {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoContentRepository.class);

  private static final String FOLDER_HOME = "home";

  private static final String FOLDER_PUBLIC = "public";

  private static final String FOLDER_ROOT = "pentaho";

  private static final String PATH_ROOT = RepositoryFile.SEPARATOR + FOLDER_ROOT;

  // ~ Instance fields =================================================================================================

  private IPentahoContentDao contentDao;

  private MutableAclService mutableAclService;

  /**
   * Jackrabbit repository super user.
   */
  private String repositoryAdminUsername;

  /**
   * Jackrabbit repository super user authority.
   */
  private String repositoryAdminAuthorityName;

  private boolean startedUp;

  /**
   * The name of the authority which is granted to all authenticated users, regardless of tenant.
   */
  private String commonAuthenticatedAuthorityName;

  private RepositoryAdminHelper repositoryAdminHelper = new RepositoryAdminHelper();

  private TenantAdminHelper tenantAdminHelper = new TenantAdminHelper();

  /**
   * Only used by RepositoryAdminHelper and TenantAdminHelper internal classes. (The enclosing class uses declarative
   * transactions.)
   */
  private TransactionTemplate txnTemplate;

  // ~ Constructors ====================================================================================================

  public PentahoContentRepository(final IPentahoContentDao contentDao, final MutableAclService mutableAclService,
      final TransactionTemplate txnTemplate, final String repositoryAdminUsername,
      final String repositoryAdminAuthorityName, final String regularUserAuthorityName) {
    super();
    Assert.notNull(contentDao);
    Assert.notNull(mutableAclService);
    Assert.notNull(txnTemplate);
    Assert.hasText(repositoryAdminUsername);
    Assert.hasText(repositoryAdminAuthorityName);
    Assert.hasText(regularUserAuthorityName);

    this.contentDao = contentDao;
    this.mutableAclService = mutableAclService;
    this.txnTemplate = txnTemplate;
    initTransactionTemplate();
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.repositoryAdminAuthorityName = repositoryAdminAuthorityName;
    this.commonAuthenticatedAuthorityName = regularUserAuthorityName;
  }

  // ~ Methods =========================================================================================================

  private void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * Throws an {@code IllegalStateException} if not started up.  Should be called from all public methods (except 
   * {@link #startup()}).
   */
  private void assertStartedUp() {
    Assert.state(startedUp, "startup must be called first");
  }

  private void internalAddPermission(final RepositoryFile file, final Sid recipient, final Permission permission,
      final boolean granting) {
    Assert.notNull(file);
    Assert.notNull(recipient);
    Assert.notNull(permission);

    Serializable fileId = file.getId();
    MutableAcl acl = internalCreateAcl(file, true);

    acl.insertAce(acl.getEntries().length, permission, recipient, granting);
    mutableAclService.updateAcl(acl);

    if (logger.isDebugEnabled()) {
      logger.debug("Added permission " + permission + " for Sid " + recipient + " content node " + fileId);
    }
  }

  private void internalAddPermission(final RepositoryFile file, final Sid recipient, final Permission permission) {
    internalAddPermission(file, recipient, permission, true);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final String absPath) {
    assertStartedUp();
    Assert.hasText(absPath);

    assertStartedUp();
    return contentDao.getFile(absPath);
  }

  private void internalSetOwner(final RepositoryFile file, final Sid owner) {
    Assert.notNull(file);
    Assert.notNull(owner);

    Serializable fileId = file.getId();
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, fileId);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
    acl.setOwner(owner);
    mutableAclService.updateAcl(acl);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createUserHomeFolderIfNecessary() {
    assertStartedUp();
    repositoryAdminHelper.createTenantRootFolderIfNecessary(internalGetTenantId(),
        internalGetTenantAdminAuthorityName(), internalGetTenantAuthenticatedAuthorityName());
    tenantAdminHelper.createInitialFoldersIfNecessary(internalGetTenantId(), internalGetTenantAdminAuthorityName(),
        internalGetTenantAuthenticatedAuthorityName());
    String username = internalGetUsername();
    return tenantAdminHelper.createUserHomeFolderIfNecessary(internalGetTenantRootFolderPath(), username);
  }

  private String internalGetTenantRootFolderPath() {
    return PATH_ROOT + RepositoryFile.SEPARATOR + internalGetTenantId();
  }

  private RepositoryFile internalGetTenantRootFolder() {
    return contentDao.getFile(internalGetTenantRootFolderPath());
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

  private void internalUpdateFile(final RepositoryFile file, final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.notNull(content);

    contentDao.updateFile(file, content);
  }

  private void internalSetFullControl(final RepositoryFile file, final Sid sid) {
    // TODO mlowery don't call addPermission as this is a connect to jcr per addPermission call
    internalAddPermission(file, sid, RepositoryFilePermission.APPEND);
    internalAddPermission(file, sid, RepositoryFilePermission.DELETE);
    internalAddPermission(file, sid, RepositoryFilePermission.DELETE_CHILD);
    // TODO uncomment this when custom privileges are supported
    //    internalAddPermission(file, sid, RepositoryFilePermission.EXECUTE);
    internalAddPermission(file, sid, RepositoryFilePermission.READ);
    internalAddPermission(file, sid, RepositoryFilePermission.READ_ACL);
    // TODO uncomment this when custom privileges are supported
    //    internalAddPermission(file, sid, RepositoryFilePermission.TAKE_OWNERSHIP);
    internalAddPermission(file, sid, RepositoryFilePermission.WRITE);
    internalAddPermission(file, sid, RepositoryFilePermission.WRITE_ACL);
  }

  private MutableAcl internalCreateAcl(final RepositoryFile file, final boolean inheritAces) {
    Assert.notNull(file);

    Serializable fileId = file.getId();
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, fileId);
    MutableAcl acl = mutableAclService.createAcl(oid);
    // link up parent (but only if this isn't the root node)
    if (file.getParentId() != null) {
      Acl newParent = mutableAclService.readAclById(new ObjectIdentityImpl(RepositoryFile.class, file.getParentId()));
      acl.setParent(newParent);
    }
    if (!inheritAces) {
      acl.setEntriesInheriting(false);
      internalSetFullControl(file, new PrincipalSid(internalGetUsername()));
    }
    return mutableAclService.updateAcl(acl);
  }

  /**
   * Returns the username of the current principal.
   * 
   * <p><strong>Only call this method if you are sure there is a non-null {@code Authentication}.</strong></p>
   * @return username
   */
  private String internalGetUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Assert.state(auth != null);

    if (auth.getPrincipal() instanceof UserDetails) {
      return ((UserDetails) auth.getPrincipal()).getUsername();
    } else {
      return auth.getPrincipal().toString();
    }
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
    Assert.isNull(SecurityContextHolder.getContext().getAuthentication(), "startup must be run before any users login");
    repositoryAdminHelper.createRepositoryRootFolderIfNecessary();
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
  public <T extends IRepositoryFileContent> T getContentForExecute(RepositoryFile file, Class<T> contentClass) {
    assertStartedUp();
    return getContentForRead(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileContent> T getContentForRead(RepositoryFile file, Class<T> contentClass) {
    assertStartedUp();
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return contentDao.getContent(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getChildren(final RepositoryFile folder) {
    assertStartedUp();
    Assert.notNull(folder);
    Assert.notNull(folder.getId());
    Assert.notNull(folder.isFolder());
    return contentDao.getChildren(folder);
  }

  /**
   * {@inheritDoc}
   */
  public void updateFile(RepositoryFile file, IRepositoryFileContent content) {
    assertStartedUp();
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    if (!file.isFolder()) {
      Assert.notNull(content);
      Assert.hasText(file.getContentType());
    }

    internalUpdateFile(file, content);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFile(RepositoryFile file) {
    assertStartedUp();
    Assert.notNull(file);
    Assert.notNull(file.getId());
    // acl deleted when file node is deleted
    contentDao.deleteFile(file);
  }

  public Acl getAcl(final RepositoryFile file) {
    Assert.notNull(file);
    return mutableAclService.readAclById(new ObjectIdentityImpl(RepositoryFile.class, file.getId()));
  }

  private String internalGetTenantId() {
    return "acme";
  }

  /**
   * @return name of authority granted to all authenticated users of the current tenant; must not be the same as
   * {@link #commonAuthenticatedAuthorityName}.
   */
  private String internalGetTenantAuthenticatedAuthorityName() {
    return "Acme_Authenticated";
  }

  /**
   * @return name of authority granted to the admin of the current tenant
   */
  private String internalGetTenantAdminAuthorityName() {
    return "Acme_Admin";
  }

  /**
   * 
   * <p>
   * Uses programmatic transaction management (via TransactionTemplate) in order to re-use private methods from enclosing
   * class. 
   * </p>
   */
  private class RepositoryAdminHelper {

    public RepositoryFile createRepositoryRootFolderIfNecessary() {
      Authentication origAuthn = SecurityContextHolder.getContext().getAuthentication();
      SecurityContextHolder.getContext().setAuthentication(createRepositoryAdminAuthentication());
      try {
        return (RepositoryFile) txnTemplate.execute(new TransactionCallback() {
          public Object doInTransaction(final TransactionStatus status) {
            RepositoryFile rootFolder = contentDao.getFile(PATH_ROOT);
            if (rootFolder == null) {
              // because this is running as the repo admin, the owner of this folder is the repo admin who also has full
              // control (no need to do a setOwner call)
              rootFolder = internalCreateFolder(null, new RepositoryFile.Builder(FOLDER_ROOT).folder(true).build(),
                  false);
              // allow all authenticated users to see the contents of this folder (and its ACL)
              internalAddPermission(rootFolder, new GrantedAuthoritySid(commonAuthenticatedAuthorityName),
                  RepositoryFilePermission.READ);
              internalAddPermission(rootFolder, new GrantedAuthoritySid(commonAuthenticatedAuthorityName),
                  RepositoryFilePermission.READ_ACL);
              // TODO uncomment this line
              //    internalAddPermission(rootFolder, new GrantedAuthoritySid(commonAuthorityName),
              //        RepositoryFilePermission.EXECUTE);
            }
            return rootFolder;
          }
        });
      } finally {
        SecurityContextHolder.getContext().setAuthentication(origAuthn);
      }

    }

    public RepositoryFile createTenantRootFolderIfNecessary(final String tenantRootFolderName,
        final String tenantAdminAuthorityName, final String tenantAuthenticatedAuthorityName) {
      Authentication origAuthn = SecurityContextHolder.getContext().getAuthentication();
      SecurityContextHolder.getContext().setAuthentication(createRepositoryAdminAuthentication());
      try {
        return (RepositoryFile) txnTemplate.execute(new TransactionCallback() {
          public Object doInTransaction(final TransactionStatus status) {
            RepositoryFile rootFolder = contentDao.getFile(PATH_ROOT);
            RepositoryFile tenantRootFolder = contentDao.getFile(rootFolder.getAbsolutePath()
                + RepositoryFile.SEPARATOR + tenantRootFolderName);
            if (tenantRootFolder == null) {
              tenantRootFolder = internalCreateFolder(rootFolder, new RepositoryFile.Builder(tenantRootFolderName)
                  .folder(true).build(), false);
              Sid ownerSid = new GrantedAuthoritySid(tenantAdminAuthorityName);
              internalSetOwner(tenantRootFolder, ownerSid);
              internalSetFullControl(tenantRootFolder, ownerSid);
              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                  RepositoryFilePermission.READ);
              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                  RepositoryFilePermission.READ_ACL);
            }
            return tenantRootFolder;
          }
        });
      } finally {
        SecurityContextHolder.getContext().setAuthentication(origAuthn);
      }
    }

    private Authentication createRepositoryAdminAuthentication() {
      final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[2];
      // necessary for AclAuthorizationStrategyImpl
      repositoryAdminAuthorities[0] = new GrantedAuthorityImpl(repositoryAdminAuthorityName);
      // necessary for unit test (Spring Security requires Authenticated role on all methods of PentahoContentRepository)
      repositoryAdminAuthorities[1] = new GrantedAuthorityImpl(commonAuthenticatedAuthorityName);
      final String password = "ignored";
      UserDetails repositoryAdminUserDetails = new User(repositoryAdminUsername, password, true, true, true, true,
          repositoryAdminAuthorities);
      Authentication repositoryAdminAuthentication = new UsernamePasswordAuthenticationToken(
          repositoryAdminUserDetails, password, repositoryAdminAuthorities);
      return repositoryAdminAuthentication;
    }

  }

  /**
   * 
   * <p>
   * Uses programmatic transaction management (via TransactionTemplate) in order to re-use private methods from enclosing
   * class. 
   * </p>
   */
  private class TenantAdminHelper {
    public void createInitialFoldersIfNecessary(final String tenantRootFolderName,
        final String tenantAdminAuthorityName, final String tenantAuthenticatedAuthorityName) {
      Authentication origAuthn = SecurityContextHolder.getContext().getAuthentication();
      SecurityContextHolder.getContext().setAuthentication(createTenantAdminAuthentication());
      try {
        txnTemplate.execute(new TransactionCallbackWithoutResult() {
          public void doInTransactionWithoutResult(final TransactionStatus status) {
            RepositoryFile tenantRootFolder = contentDao.getFile(PATH_ROOT + RepositoryFile.SEPARATOR
                + tenantRootFolderName);
            Assert.notNull(tenantRootFolder);
            if (contentDao.getFile(tenantRootFolder.getAbsolutePath() + RepositoryFile.SEPARATOR + FOLDER_PUBLIC) == null) {
              // public folder is versioned
              RepositoryFile tenantPublicFolder = internalCreateFolder(tenantRootFolder, new RepositoryFile.Builder(
                  FOLDER_PUBLIC).folder(true).versioned(true).build(), true);
              Sid ownerSid = new GrantedAuthoritySid(tenantAdminAuthorityName);
              internalSetOwner(tenantPublicFolder, ownerSid);
              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                  RepositoryFilePermission.READ);
              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                  RepositoryFilePermission.READ_ACL);
              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                  RepositoryFilePermission.APPEND);
              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                  RepositoryFilePermission.DELETE_CHILD);
              // TODO mlowery uncomment
              // internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
              //   RepositoryFilePermission.EXECUTE);
              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                  RepositoryFilePermission.WRITE);
              // TODO mlowery don't want to give write_acl access on the folder itself but also don't want a special
              // "createPublicFile" and "createPublicFolder" methods either
              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                  RepositoryFilePermission.WRITE_ACL);
              // inherits the ACEs from parent ACL
              RepositoryFile tenantHomeFolder = internalCreateFolder(tenantRootFolder, new RepositoryFile.Builder(
                  FOLDER_HOME).folder(true).build(), true);
              internalSetOwner(tenantHomeFolder, ownerSid);
            }
          }
        });
      } finally {
        SecurityContextHolder.getContext().setAuthentication(origAuthn);
      }
    }

    public RepositoryFile createUserHomeFolderIfNecessary(final String tenantRootFolderPath, final String username) {
      Authentication origAuthn = SecurityContextHolder.getContext().getAuthentication();
      SecurityContextHolder.getContext().setAuthentication(createTenantAdminAuthentication());
      try {
        return (RepositoryFile) txnTemplate.execute(new TransactionCallback() {
          public Object doInTransaction(final TransactionStatus status) {
            RepositoryFile userHomeFolder = contentDao.getFile(tenantRootFolderPath + RepositoryFile.SEPARATOR
                + FOLDER_HOME + RepositoryFile.SEPARATOR + username);
            if (userHomeFolder == null) {
              RepositoryFile tenantHomeFolder = contentDao.getFile(tenantRootFolderPath + RepositoryFile.SEPARATOR
                  + FOLDER_HOME);
              // user home folder is versioned
              userHomeFolder = internalCreateFolder(tenantHomeFolder, new RepositoryFile.Builder(username).folder(true)
                  .versioned(true).build(), false);
              Sid ownerSid = new PrincipalSid(username);
              internalSetOwner(userHomeFolder, ownerSid);
              internalSetFullControl(userHomeFolder, ownerSid);
            }
            return userHomeFolder;
          }
        });
      } finally {
        SecurityContextHolder.getContext().setAuthentication(origAuthn);
      }
    }

    private Authentication createTenantAdminAuthentication() {
      final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[2];
      // necessary for AclAuthorizationStrategyImpl
      repositoryAdminAuthorities[0] = new GrantedAuthorityImpl(repositoryAdminAuthorityName);
      // necessary for unit test (Spring Security requires Authenticated role on all methods of PentahoContentRepository)
      repositoryAdminAuthorities[1] = new GrantedAuthorityImpl(commonAuthenticatedAuthorityName);
      final String password = "ignored";
      UserDetails repositoryAdminUserDetails = new User(repositoryAdminUsername, password, true, true, true, true,
          repositoryAdminAuthorities);
      Authentication repositoryAdminAuthentication = new UsernamePasswordAuthenticationToken(
          repositoryAdminUserDetails, password, repositoryAdminAuthorities);
      return repositoryAdminAuthentication;
    }
  }

}
