package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.util.EnumSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IRepositoryService;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.api.repository.IRepositoryService.IRepositoryEventHandler;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * Default {@link IRepositoryEventHandler} implementation.
 * 
 * <ul>
 * <li>Runs as the repository admin. (Actually method is called as a regular user and the context is switched.)</li>
 * <li>Wraps calls to {@code repositoryFileDao} and {@code repositoryFileAclDao} in transactions.</li>
 * <li>Uses programmatic transactions to keep the amount of declarative transaction XML to a minimum.</li>
 * </ul>
 * 
 * @author mlowery
 */
public class DefaultRepositoryEventHandler implements IRepositoryService.IRepositoryEventHandler {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultRepositoryEventHandler.class);

  // ~ Instance fields =================================================================================================

  /**
   * Repository super user.
   */
  private String repositoryAdminUsername;

  /**
   * Repository super user authority.
   */
  private String repositoryAdminAuthorityName;

  /**
   * The name of the authority which is granted to all authenticated users, regardless of tenant.
   */
  private String commonAuthenticatedAuthorityName;

  private String tenantAuthenticatedAuthorityNameSuffix;

  private String tenantAdminAuthorityNameSuffix;

  private TransactionTemplate txnTemplate;

  private IRepositoryFileDao repositoryFileDao;

  private IRepositoryFileAclDao repositoryFileAclDao;

  private boolean startedUp;

  // ~ Constructors ====================================================================================================

  public DefaultRepositoryEventHandler(final IRepositoryFileDao contentDao,
      final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
      final String repositoryAdminUsername, final String repositoryAdminAuthorityName,
      final String commonAuthenticatedAuthorityName, final String tenantAuthenticatedAuthorityNameSuffix,
      final String tenantAdminAuthorityNameSuffix) {
    Assert.notNull(contentDao);
    Assert.notNull(repositoryFileAclDao);
    Assert.notNull(txnTemplate);
    Assert.hasText(repositoryAdminUsername);
    Assert.hasText(repositoryAdminAuthorityName);
    Assert.hasText(commonAuthenticatedAuthorityName);
    Assert.hasText(tenantAuthenticatedAuthorityNameSuffix);
    Assert.hasText(tenantAdminAuthorityNameSuffix);
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.txnTemplate = txnTemplate;
    this.repositoryAdminAuthorityName = repositoryAdminAuthorityName;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.commonAuthenticatedAuthorityName = commonAuthenticatedAuthorityName;
    this.tenantAuthenticatedAuthorityNameSuffix = tenantAuthenticatedAuthorityNameSuffix;
    this.tenantAdminAuthorityNameSuffix = tenantAdminAuthorityNameSuffix;
    initTransactionTemplate();
  }

  // ~ Methods =========================================================================================================

  public synchronized void onNewTenant(final String tenantId) {
    assertStartedUp();
    createTenantRootFolder(tenantId);
    createInitialTenantFolders(tenantId);
  }

  public synchronized void onNewUser(final String tenantId, final String username) {
    assertStartedUp();
    createUserHomeFolder(tenantId, username);
  }

  public synchronized void onShutdown() {
    assertStartedUp();
  }

  public synchronized void onStartup() {
    createPentahoRootFolder();
    startedUp = true;
  }

  public synchronized void onNewTenant() {
    onNewTenant(internalGetTenantId());
  }

  public synchronized void onNewUser() {
    onNewUser(internalGetTenantId(), internalGetUsername());
  }

  /**
   * Throws an {@code IllegalStateException} if not started up.  Should be called from all public methods (except 
   * {@link #onStartup()}).
   */
  private void assertStartedUp() {
    Assert.state(startedUp, "startup must be called first");
  }

  private void createPentahoRootFolder() {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          RepositoryFile rootFolder = repositoryFileDao.getFile(RepositoryPaths.getPentahoRootFolderPath());
          if (rootFolder == null) {
            // because this is running as the repo admin, the owner of this folder is the repo admin who also has full
            // control (no need to do a setOwner call)
            rootFolder = internalCreateFolder(null, new RepositoryFile.Builder(RepositoryPaths
                .getPentahoRootFolderName()).folder(true).build(), false, repositoryAdminUsername,
                "[system] create pentaho root folder");
            // allow all authenticated users to see the contents of this folder (and its ACL)
            internalAddPermission(rootFolder.getId(), new RepositoryFileSid(commonAuthenticatedAuthorityName,
                RepositoryFileSid.Type.ROLE), EnumSet.of(RepositoryFilePermission.READ,
                RepositoryFilePermission.READ_ACL, RepositoryFilePermission.EXECUTE));
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }

  }

  private void createTenantRootFolder(final String tenantId) {
    final String tenantAdminAuthorityName = internalGetTenantAdminAuthorityName(tenantId);
    final String tenantAuthenticatedAuthorityName = internalGetTenantAuthenticatedAuthorityName(tenantId);
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          RepositoryFile rootFolder = repositoryFileDao.getFile(RepositoryPaths.getPentahoRootFolderPath());
          RepositoryFile tenantRootFolder = repositoryFileDao
              .getFile(RepositoryPaths.getTenantRootFolderPath(tenantId));
          if (tenantRootFolder == null) {
            tenantRootFolder = internalCreateFolder(rootFolder.getId(), new RepositoryFile.Builder(tenantId).folder(
                true).build(), false, tenantAdminAuthorityName, "[system] created tenant root folder");
            RepositoryFileSid ownerSid = new RepositoryFileSid(tenantAdminAuthorityName, RepositoryFileSid.Type.ROLE);
            internalSetOwner(tenantRootFolder, ownerSid);
            internalSetFullControl(tenantRootFolder.getId(), ownerSid);
            internalAddPermission(tenantRootFolder.getId(), new RepositoryFileSid(tenantAuthenticatedAuthorityName,
                RepositoryFileSid.Type.ROLE), EnumSet.of(RepositoryFilePermission.READ,
                RepositoryFilePermission.READ_ACL));
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  private void createInitialTenantFolders(final String tenantId) {
    final String tenantAdminAuthorityName = internalGetTenantAdminAuthorityName(tenantId);
    final String tenantAuthenticatedAuthorityName = internalGetTenantAuthenticatedAuthorityName(tenantId);

    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          RepositoryFile tenantRootFolder = repositoryFileDao
              .getFile(RepositoryPaths.getTenantRootFolderPath(tenantId));
          Assert.notNull(tenantRootFolder);
          if (repositoryFileDao.getFile(RepositoryPaths.getTenantPublicFolderPath(tenantId)) == null) {
            RepositoryFile tenantPublicFolder = internalCreateFolder(tenantRootFolder.getId(),
                new RepositoryFile.Builder(RepositoryPaths.getTenantPublicFolderName()).folder(true)
                    .build(), false, tenantAdminAuthorityName, "[system] created tenant public folder");
            RepositoryFileSid ownerSid = new RepositoryFileSid(tenantAdminAuthorityName, RepositoryFileSid.Type.ROLE);
            internalSetOwner(tenantPublicFolder, ownerSid);
            internalAddPermission(tenantPublicFolder.getId(), new RepositoryFileSid(tenantAuthenticatedAuthorityName,
                RepositoryFileSid.Type.ROLE),

            EnumSet.of(RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL,
                RepositoryFilePermission.APPEND, RepositoryFilePermission.DELETE_CHILD, RepositoryFilePermission.WRITE,
                RepositoryFilePermission.WRITE_ACL, RepositoryFilePermission.EXECUTE));

            // home folder inherits ACEs from parent ACL
            RepositoryFile tenantHomeFolder = internalCreateFolder(tenantRootFolder.getId(),
                new RepositoryFile.Builder(RepositoryPaths.getTenantHomeFolderName()).folder(true).build(), true,
                tenantAdminAuthorityName, "[system] created tenant home folder");
            internalSetOwner(tenantHomeFolder, ownerSid);
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  private void createUserHomeFolder(final String tenantId, final String username) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          RepositoryFile userHomeFolder = repositoryFileDao.getFile(RepositoryPaths.getUserHomeFolderPath(tenantId,
              username));
          if (userHomeFolder == null) {
            RepositoryFile tenantHomeFolder = repositoryFileDao.getFile(RepositoryPaths
                .getTenantHomeFolderPath(tenantId));
            userHomeFolder = internalCreateFolder(tenantHomeFolder.getId(), new RepositoryFile.Builder(username)
                .folder(true).build(), false, username, "[system] created user home folder");
            RepositoryFileSid ownerSid = new RepositoryFileSid(username);
            internalSetOwner(userHomeFolder, ownerSid);
            internalSetFullControl(userHomeFolder.getId(), ownerSid);
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  private void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  private RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile file,
      final boolean inheritAces, final String ownerUsername, final String versionMessage) {
    Assert.notNull(file);

    RepositoryFile newFile = repositoryFileDao.createFolder(parentFolderId, file, versionMessage);
    internalCreateAcl(newFile.getId(), inheritAces, ownerUsername);

    return newFile;
  }

  private void internalSetFullControl(final Serializable fileId, final RepositoryFileSid sid) {
    Assert.notNull(fileId);
    Assert.notNull(sid);
    repositoryFileAclDao.setFullControl(fileId, sid, RepositoryFilePermission.ALL);
  }

  private RepositoryFileAcl internalCreateAcl(final Serializable fileId, final boolean entriesInheriting,
      final String ownerUsername) {
    Assert.notNull(fileId);

    return repositoryFileAclDao.createAcl(fileId, entriesInheriting, new RepositoryFileSid(ownerUsername),
        RepositoryFilePermission.ALL);
  }

  private void internalAddPermission(final Serializable fileId, final RepositoryFileSid recipient,
      final EnumSet<RepositoryFilePermission> permissions) {
    Assert.notNull(fileId);
    Assert.notNull(recipient);
    Assert.notNull(permissions);
    Assert.notEmpty(permissions);

    repositoryFileAclDao.addPermission(fileId, recipient, permissions);
  }

  private IPentahoSession createRepositoryAdminPentahoSession() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[2];
    // necessary for AclAuthorizationStrategyImpl
    repositoryAdminAuthorities[0] = new GrantedAuthorityImpl(repositoryAdminAuthorityName);
    // necessary for unit test (Spring Security requires Authenticated role on all methods of DefaultRepositoryService)
    repositoryAdminAuthorities[1] = new GrantedAuthorityImpl(commonAuthenticatedAuthorityName);
    final String password = "ignored"; //$NON-NLS-1$
    UserDetails repositoryAdminUserDetails = new User(repositoryAdminUsername, password, true, true, true, true,
        repositoryAdminAuthorities);
    Authentication repositoryAdminAuthentication = new UsernamePasswordAuthenticationToken(repositoryAdminUserDetails,
        password, repositoryAdminAuthorities);
    SecurityHelper.setPrincipal(repositoryAdminAuthentication, pentahoSession);
    return pentahoSession;
  }

  /**
   * @return name of authority granted to all authenticated users of the given tenant; must not be the same as
   * {@link #commonAuthenticatedAuthorityName}.
   */
  private String internalGetTenantAuthenticatedAuthorityName(final String tenantId) {
    return tenantId + tenantAuthenticatedAuthorityNameSuffix;
  }

  /**
   * @return name of authority granted to the admin of the given tenant
   */
  private String internalGetTenantAdminAuthorityName(final String tenantId) {
    return tenantId + tenantAdminAuthorityNameSuffix;
  }

  private void internalSetOwner(final RepositoryFile file, final RepositoryFileSid owner) {
    Assert.notNull(file);
    Assert.notNull(owner);

    RepositoryFileAcl acl = repositoryFileAclDao.readAclById(file.getId());
    RepositoryFileAcl newAcl = new RepositoryFileAcl.Builder(acl).owner(owner).build();
    repositoryFileAclDao.updateAcl(newAcl);
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

}