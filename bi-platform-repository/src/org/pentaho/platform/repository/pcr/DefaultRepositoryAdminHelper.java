package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.pcr.PentahoContentRepository.IRepositoryAdminHelper;
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
 * Default {@link IRepositoryAdminHelper} implementation.
 * 
 * <ul>
 * <li>Runs as the repository admin.</li>
 * <li>Wraps calls to {@code contentDao} and {@code mutableAclService} in transactions.</li>
 * <li>Uses programmatic transactions to keep the amount of declarative transaction XML to a minimum.</li>
 * </ul>
 * 
 * @author mlowery
 */
public class DefaultRepositoryAdminHelper implements IRepositoryAdminHelper {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultRepositoryAdminHelper.class);

  private static final String FOLDER_HOME = "home"; //$NON-NLS-1$

  private static final String FOLDER_PUBLIC = "public"; //$NON-NLS-1$

  private static final String FOLDER_ROOT = "pentaho"; //$NON-NLS-1$

  private static final String PATH_ROOT = RepositoryFile.SEPARATOR + FOLDER_ROOT;

  private final String PATTERN_TENANT_ROOT_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}"; //$NON-NLS-1$

  private final String PATTERN_TENANT_HOME_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + FOLDER_HOME;

  private final String PATTERN_TENANT_PUBLIC_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + FOLDER_PUBLIC;

  private final String PATTERN_USER_HOME_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + FOLDER_HOME + RepositoryFile.SEPARATOR + "{1}"; //$NON-NLS-1$

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

  // ~ Constructors ====================================================================================================

  public DefaultRepositoryAdminHelper(final IRepositoryFileDao contentDao,
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

  public void createPentahoRootFolder() {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          RepositoryFile rootFolder = contentDao.getFile(getPentahoRootFolderPath());
          if (rootFolder == null) {
            // because this is running as the repo admin, the owner of this folder is the repo admin who also has full
            // control (no need to do a setOwner call)
            rootFolder = internalCreateFolder(null, new RepositoryFile.Builder(FOLDER_ROOT).folder(true).build(), false);
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

  public void createTenantRootFolder() {
    final String tenantId = internalGetTenantId();
    final String tenantAdminAuthorityName = internalGetTenantAdminAuthorityName();
    final String tenantAuthenticatedAuthorityName = internalGetTenantAuthenticatedAuthorityName();
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          RepositoryFile rootFolder = contentDao.getFile(getPentahoRootFolderPath());
          RepositoryFile tenantRootFolder = contentDao.getFile(getTenantRootFolderPath(tenantId));
          if (tenantRootFolder == null) {
            tenantRootFolder = internalCreateFolder(rootFolder, new RepositoryFile.Builder(tenantId).folder(true)
                .build(), false);
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

  public void createInitialTenantFolders() {
    final String tenantId = internalGetTenantId();
    final String tenantAdminAuthorityName = internalGetTenantAdminAuthorityName();
    final String tenantAuthenticatedAuthorityName = internalGetTenantAuthenticatedAuthorityName();

    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          RepositoryFile tenantRootFolder = contentDao.getFile(getTenantRootFolderPath(tenantId));
          Assert.notNull(tenantRootFolder);
          if (contentDao.getFile(getTenantPublicFolderPath(tenantId)) == null) {
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
                FOLDER_HOME).folder(true).build(), true);
            internalSetOwner(tenantHomeFolder, ownerSid);
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  public void createUserHomeFolder() {
    final String username = internalGetUsername();
    final String tenantId = internalGetTenantId();
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          RepositoryFile userHomeFolder = contentDao.getFile(getUserHomeFolderPath(username, tenantId));
          if (userHomeFolder == null) {
            RepositoryFile tenantHomeFolder = contentDao.getFile(getTenantHomeFolderPath(tenantId));
            // user home folder is versioned
            userHomeFolder = internalCreateFolder(tenantHomeFolder, new RepositoryFile.Builder(username).folder(true)
                .versioned(true).build(), false);
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

  public String getPentahoRootFolderPath() {
    return PATH_ROOT;
  }

  public String getTenantHomeFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_HOME_PATH, tenantId);
  }

  public String getTenantPublicFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_PUBLIC_PATH, tenantId);
  }

  public String getTenantRootFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_ROOT_PATH, tenantId);
  }

  public String getUserHomeFolderPath(final String username, final String tenantId) {
    return MessageFormat.format(PATTERN_USER_HOME_PATH, tenantId, username);
  }

  private void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final boolean inheritAces) {
    Assert.notNull(file);

    RepositoryFile newFile = contentDao.createFolder(parentFolder, file);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private void internalSetFullControl(final RepositoryFile file, final Sid sid) {
    Assert.notNull(file);
    Assert.notNull(sid);
    // TODO mlowery fix this null param
    mutableAclService.setFullControl(new ObjectIdentityImpl(RepositoryFile.class, file.getId()), sid, null);
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
   * @return name of authority granted to all authenticated users of the current tenant; must not be the same as
   * {@link #commonAuthenticatedAuthorityName}.
   */
  private String internalGetTenantAuthenticatedAuthorityName() {
    return internalGetTenantId() + tenantAuthenticatedAuthorityNameSuffix;
  }

  /**
   * @return name of authority granted to the admin of the current tenant
   */
  private String internalGetTenantAdminAuthorityName() {
    return internalGetTenantId() + tenantAdminAuthorityNameSuffix;
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

}
