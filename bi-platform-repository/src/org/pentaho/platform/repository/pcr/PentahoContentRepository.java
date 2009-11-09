package org.pentaho.platform.repository.pcr;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IPentahoContentDao;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.springsecurity.AclServicePreparer;
import org.pentaho.platform.repository.pcr.springsecurity.RepositoryFilePermission;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
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
import org.springframework.util.Assert;

/**
 * Implementation of {@link IPentahoContentRepository} using an {@link IPentahoContentDao} and Spring 
 * Security's {@link MutableAclService}.
 * 
 * <p>
 * All <strong>public</strong> methods in this class should be protected via Spring Security with the exception of:
 * <ul>
 * <li>{@link #startup}</li>
 * <li>{@link #shutdown}</li>
 * </ul>
 * </p>
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

  private AclServicePreparer aclServicePreparer;

  private String systemUsername;

  private boolean startedUp;

  private String regularUserAuthorityName;

  // ~ Constructors ====================================================================================================

  public PentahoContentRepository(final IPentahoContentDao contentDao, final MutableAclService mutableAclService,
      final AclServicePreparer aclServicePreparer, final String systemUsername, final String regularUserAuthorityName) {
    super();
    Assert.notNull(contentDao);
    Assert.notNull(mutableAclService);
    Assert.notNull(aclServicePreparer);
    Assert.hasText(systemUsername);
    Assert.hasText(regularUserAuthorityName);

    this.contentDao = contentDao;
    this.mutableAclService = mutableAclService;
    this.aclServicePreparer = aclServicePreparer;
    this.systemUsername = systemUsername;
    this.regularUserAuthorityName = regularUserAuthorityName;
  }

  // ~ Methods =========================================================================================================

  /**
   * Creates initial folder structure including home and public folders. Assigns ACLs to those initial folders.
   */
  private void internalPrepareRepositoryIfNecessary() {
    Assert.isNull(SecurityContextHolder.getContext().getAuthentication(),
        "prepareRepositoryIfNecessary must be run before any users login");

    Authentication systemAuthentication = internalCreateSystemAuthentication();
    SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
    try {
      aclServicePreparer.prepareAclServiceIfNecessary();
      internalCreateInitialFoldersIfNecessary();
    } finally {
      SecurityContextHolder.getContext().setAuthentication(null);
    }
    startedUp = true;
  }

  /**
   * Creates "system" authentication.  Note that {@code RunAsManager} is not used here as it is only relevant when 
   * there is an already-authenticated user; there is no already-authenticated user here.
   * 
   * @return system authentication
   */
  private Authentication internalCreateSystemAuthentication() {
    final GrantedAuthority[] systemUserAuthorities = new GrantedAuthority[0];
    final String password = "ignored";
    UserDetails systemUserDetails = new User(systemUsername, password, true, true, true, true, systemUserAuthorities);
    Authentication systemAuthentication = new UsernamePasswordAuthenticationToken(systemUserDetails, password,
        systemUserAuthorities);
    return systemAuthentication;
  }

  /**
   * Throws an {@code IllegalStateException} if not started up.  Should be called from all public methods (except 
   * {@link #startup()}).
   */
  private void internalCheckStartedUp() {
    Assert.state(startedUp, "startup must be called first");
  }

  private void internalCreateInitialFoldersIfNecessary() {
    // check to see if this has already been run before
    if (contentDao.getFile(PATH_ROOT) != null) {
      return;
    }
    RepositoryFile rootFolder = internalCreateFolder(null,
        new RepositoryFile.Builder(FOLDER_ROOT).folder(true).build(), false);
    internalAddPermission(rootFolder, new GrantedAuthoritySid(regularUserAuthorityName), RepositoryFilePermission.READ);
    internalAddPermission(rootFolder, new GrantedAuthoritySid(regularUserAuthorityName),
        RepositoryFilePermission.EXECUTE);

    // inherits the ACEs from parent ACL
    internalCreateFolder(rootFolder, new RepositoryFile.Builder(FOLDER_PUBLIC).folder(true).build(), true);
    // inherits the ACEs from parent ACL
    internalCreateFolder(rootFolder, new RepositoryFile.Builder(FOLDER_HOME).folder(true).build(), true);
  }

  private void internalAddPermission(final RepositoryFile file, final Sid recipient, final Permission permission) {
    Assert.notNull(file);
    Assert.notNull(recipient);
    Assert.notNull(permission);

    Serializable fileId = file.getId();
    MutableAcl acl = internalCreateAclIfNecessary(file, true);

    acl.insertAce(acl.getEntries().length, permission, recipient, true);
    mutableAclService.updateAcl(acl);

    if (logger.isDebugEnabled()) {
      logger.debug("Added permission " + permission + " for Sid " + recipient + " content node " + fileId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final String absPath) {
    Assert.hasText(absPath);

    internalCheckStartedUp();
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
    internalCheckStartedUp();
    RepositoryFile homeFolder = contentDao.getFile(PATH_ROOT + RepositoryFile.SEPARATOR + FOLDER_HOME);
    RepositoryFile userHomeFolder = contentDao.getFile(homeFolder.getAbsolutePath() + RepositoryFile.SEPARATOR
        + internalGetUsername());
    if (userHomeFolder == null) {
      return internalCreateFolder(homeFolder, new RepositoryFile.Builder(internalGetUsername()).folder(true).build(),
          false);
    } else {
      return userHomeFolder;
    }
  }

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content, final boolean inheritAces) {
    Assert.notNull(file);
    Assert.notNull(content);

    RepositoryFile newFile = contentDao.createFile(parentFolder, file, content);
    internalCreateAclIfNecessary(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final boolean inheritAces) {
    Assert.notNull(file);

    RepositoryFile newFile = contentDao.createFolder(parentFolder, file);
    internalCreateAclIfNecessary(newFile, inheritAces);

    return newFile;
  }

  private void internalUpdateFile(final RepositoryFile file, final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.notNull(content);

    contentDao.updateFile(file, content);
  }

  private void internalSetFullControl(final RepositoryFile file, final Sid sid) {
    // TODO mlowery don't call addPermission as this is a connect to db per addPermission call
    internalAddPermission(file, sid, RepositoryFilePermission.APPEND);
    internalAddPermission(file, sid, RepositoryFilePermission.DELETE);
    internalAddPermission(file, sid, RepositoryFilePermission.DELETE_CHILD);
    internalAddPermission(file, sid, RepositoryFilePermission.EXECUTE);
    internalAddPermission(file, sid, RepositoryFilePermission.READ);
    internalAddPermission(file, sid, RepositoryFilePermission.READ_ACL);
    internalAddPermission(file, sid, RepositoryFilePermission.READ_ATTRIBUTES);
    internalAddPermission(file, sid, RepositoryFilePermission.TAKE_OWNERSHIP);
    internalAddPermission(file, sid, RepositoryFilePermission.WRITE);
    internalAddPermission(file, sid, RepositoryFilePermission.WRITE_ACL);
    internalAddPermission(file, sid, RepositoryFilePermission.WRITE_ATTRIBUTES);
  }

  private MutableAcl internalCreateAclIfNecessary(final RepositoryFile file, final boolean inheritAces) {
    Assert.notNull(file);

    MutableAcl acl;
    Serializable fileId = file.getId();
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, fileId);
    try {
      acl = (MutableAcl) mutableAclService.readAclById(oid);
    } catch (NotFoundException nfe) {
      // owner is set here to the currently authenticated user
      acl = mutableAclService.createAcl(oid);
      // link up parent (but only if this isn't the root node)
      if (file.getParentId() != null) {
        Acl newParent = mutableAclService.readAclById(new ObjectIdentityImpl(RepositoryFile.class, file.getParentId()));
        acl.setParent(newParent);
      }
    }
    if (!inheritAces) {
      acl.setEntriesInheriting(false);
      internalSetFullControl(file, new PrincipalSid(internalGetUsername()));
    }
    return acl;
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

  }

  /**
   * {@inheritDoc}
   */
  public synchronized void startup() {
    internalPrepareRepositoryIfNecessary();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    Assert.notNull(content);
    Assert.hasText(file.getMimeType());
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }

    return internalCreateFile(parentFolder, file, content, true);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file) {
    Assert.notNull(file);
    Assert.isTrue(file.isFolder());
    Assert.hasText(file.getName());
    if (parentFolder != null) {
      Assert.hasText(parentFolder.getName());
    }
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
    return getContentForRead(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileContent> T getContentForRead(RepositoryFile file, Class<T> contentClass) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return contentDao.getContent(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getChildren(final RepositoryFile folder) {
    Assert.notNull(folder);
    Assert.notNull(folder.getId());
    Assert.notNull(folder.isFolder());
    return contentDao.getChildren(folder);
  }

  /**
   * {@inheritDoc}
   */
  public void updateFile(RepositoryFile file, IRepositoryFileContent content) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    if (!file.isFolder()) {
      Assert.notNull(content);
      Assert.hasText(file.getMimeType());
    }

    internalUpdateFile(file, content);
  }

}
