package org.pentaho.platform.repository.pcr;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.pcr.springsecurity.RepositoryFilePermission;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;
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
 * <li>Wraps calls to {@code contentDao} and {@code mutableAclService} in transactions.</li>
 * <li>Uses programmatic transactions to keep the amount of declarative transaction XML to a minimum.</li>
 * </ul>
 * 
 * @author mlowery
 */
public class DefaultRepositoryEventHandler implements IPentahoContentRepository.IRepositoryEventHandler {

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

  private IRepositoryFileDao contentDao;

  private IPentahoMutableAclService mutableAclService;

  private boolean startedUp;

  // ~ Constructors ====================================================================================================

  public DefaultRepositoryEventHandler(final IRepositoryFileDao contentDao,
      final IPentahoMutableAclService mutableAclService, final TransactionTemplate txnTemplate,
      final String repositoryAdminUsername, final String repositoryAdminAuthorityName,
      final String commonAuthenticatedAuthorityName, final String tenantAuthenticatedAuthorityNameSuffix,
      final String tenantAdminAuthorityNameSuffix) {
    Assert.notNull(contentDao);
    Assert.notNull(mutableAclService);
    Assert.notNull(txnTemplate);
    Assert.hasText(repositoryAdminUsername);
    Assert.hasText(repositoryAdminAuthorityName);
    Assert.hasText(commonAuthenticatedAuthorityName);
    Assert.hasText(tenantAuthenticatedAuthorityNameSuffix);
    Assert.hasText(tenantAdminAuthorityNameSuffix);
    this.contentDao = contentDao;
    this.mutableAclService = mutableAclService;
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
          RepositoryFile rootFolder = contentDao.getFile(RepositoryPaths.getPentahoRootFolderPath());
          if (rootFolder == null) {
            // because this is running as the repo admin, the owner of this folder is the repo admin who also has full
            // control (no need to do a setOwner call)
            rootFolder = internalCreateFolder(null, new RepositoryFile.Builder(RepositoryPaths
                .getPentahoRootFolderName()).folder(true).build(), false, repositoryAdminUsername,
                "[system] create pentaho root folder");
            // allow all authenticated users to see the contents of this folder (and its ACL)
            internalAddPermission(rootFolder, new GrantedAuthoritySid(commonAuthenticatedAuthorityName),
                RepositoryFilePermission.READ);
            internalAddPermission(rootFolder, new GrantedAuthoritySid(commonAuthenticatedAuthorityName),
                RepositoryFilePermission.READ_ACL);
            // TODO uncomment this line
            //    internalAddPermission(rootFolder, new GrantedAuthoritySid(commonAuthorityName),
            //        RepositoryFilePermission.EXECUTE);
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
          RepositoryFile rootFolder = contentDao.getFile(RepositoryPaths.getPentahoRootFolderPath());
          RepositoryFile tenantRootFolder = contentDao.getFile(RepositoryPaths.getTenantRootFolderPath(tenantId));
          if (tenantRootFolder == null) {
            tenantRootFolder = internalCreateFolder(rootFolder, new RepositoryFile.Builder(tenantId).folder(true)
                .build(), false, tenantAdminAuthorityName, "[system] created tenant root folder");
            Sid ownerSid = new GrantedAuthoritySid(tenantAdminAuthorityName);
            internalSetOwner(tenantRootFolder, ownerSid);
            internalSetFullControl(tenantRootFolder, ownerSid);
            internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                RepositoryFilePermission.READ);
            internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                RepositoryFilePermission.READ_ACL);
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
          RepositoryFile tenantRootFolder = contentDao.getFile(RepositoryPaths.getTenantRootFolderPath(tenantId));
          Assert.notNull(tenantRootFolder);
          if (contentDao.getFile(RepositoryPaths.getTenantPublicFolderPath(tenantId)) == null) {
            // public folder is versioned
            RepositoryFile tenantPublicFolder = internalCreateFolder(tenantRootFolder, new RepositoryFile.Builder(
                RepositoryPaths.getTenantPublicFolderName()).folder(true).versioned(true).build(), true,
                tenantAdminAuthorityName, "[system] created tenant public folder");
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
            //              internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
            //                  RepositoryFilePermission.EXECUTE);
            internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                RepositoryFilePermission.WRITE);
            // TODO mlowery don't want to give write_acl access on the folder itself but also don't want a special
            // "createPublicFile" and "createPublicFolder" methods either
            internalAddPermission(tenantRootFolder, new GrantedAuthoritySid(tenantAuthenticatedAuthorityName),
                RepositoryFilePermission.WRITE_ACL);
            // inherits the ACEs from parent ACL
            RepositoryFile tenantHomeFolder = internalCreateFolder(tenantRootFolder, new RepositoryFile.Builder(
                RepositoryPaths.getTenantHomeFolderName()).folder(true).build(), true, tenantAdminAuthorityName,
                "[system] created tenant home folder");
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
          RepositoryFile userHomeFolder = contentDao.getFile(RepositoryPaths.getUserHomeFolderPath(tenantId, username));
          if (userHomeFolder == null) {
            RepositoryFile tenantHomeFolder = contentDao.getFile(RepositoryPaths.getTenantHomeFolderPath(tenantId));
            // user home folder is versioned
            userHomeFolder = internalCreateFolder(tenantHomeFolder, new RepositoryFile.Builder(username).folder(true)
                .versioned(true).build(), false, username, "[system] created user home folder");
            Sid ownerSid = new PrincipalSid(username);
            internalSetOwner(userHomeFolder, ownerSid);
            internalSetFullControl(userHomeFolder, ownerSid);
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

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final boolean inheritAces, final String ownerUsername, final String versionMessage) {
    Assert.notNull(file);

    RepositoryFile newFile = contentDao.createFolder(parentFolder, file, versionMessage);
    internalCreateAcl(newFile, inheritAces, ownerUsername);

    return newFile;
  }

  private void internalSetFullControl(final RepositoryFile file, final Sid sid) {
    Assert.notNull(file);
    Assert.notNull(sid);
    // TODO mlowery fix this null param
    mutableAclService.setFullControl(new ObjectIdentityImpl(RepositoryFile.class, file.getId()), sid, null);
  }

  private MutableAcl internalCreateAcl(final RepositoryFile file, final boolean entriesInheriting,
      final String ownerUsername) {
    Assert.notNull(file);

    ObjectIdentity parentOid = null;
    if (file.getParentId() != null) {
      parentOid = new ObjectIdentityImpl(RepositoryFile.class, file.getParentId());
    }
    return mutableAclService.createAndInitializeAcl(new ObjectIdentityImpl(RepositoryFile.class, file.getId()),
        parentOid, entriesInheriting, new PrincipalSid(ownerUsername));
  }

  private void internalAddPermission(final RepositoryFile file, final Sid recipient, final Permission permission,
      final boolean granting) {
    Assert.notNull(file);
    Assert.notNull(recipient);
    Assert.notNull(permission);

    mutableAclService.addPermission(new ObjectIdentityImpl(RepositoryFile.class, file.getId()), recipient, permission,
        granting);
  }

  private void internalAddPermission(final RepositoryFile file, final Sid recipient, final Permission permission) {
    internalAddPermission(file, recipient, permission, true);
  }

  private IPentahoSession createRepositoryAdminPentahoSession() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[2];
    // necessary for AclAuthorizationStrategyImpl
    repositoryAdminAuthorities[0] = new GrantedAuthorityImpl(repositoryAdminAuthorityName);
    // necessary for unit test (Spring Security requires Authenticated role on all methods of PentahoContentRepository)
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
